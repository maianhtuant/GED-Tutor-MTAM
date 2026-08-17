package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Generator for {@link MathProblemKind#FREE_FORM} templates.
 *
 * <p>Teachers write a template string using <code>{placeholder}</code> tokens,
 * e.g. <code>{a}x^2 + {b}x + {c} = 0</code>. Each unique placeholder is
 * assigned one random integer drawn from [minValue, maxValue]. The same
 * placeholder reused multiple times always receives the same value.
 *
 * <h3>Per-placeholder ranges: {@code {name:min-max}}</h3>
 * <p>A placeholder may optionally carry its own inclusive range, overriding
 * the template's global [minValue, maxValue] for that one name — e.g.
 * <code>{a:1-50}.{b:1-100}</code> draws <code>a</code> from 1–50 and
 * <code>b</code> from 1–100 independently. Only the <em>first</em> occurrence
 * of a given name needs (or uses) the range suffix; every later occurrence
 * of the same name — with or without a suffix — reuses the value already
 * assigned. A bare <code>{name}</code> with no suffix still falls back to
 * the template-level minValue/maxValue exactly as before.
 *
 * <h3>Special token: {@code {+-}}</h3>
 * <p>Writing <code>{+-}</code> anywhere in the template randomly inserts either
 * <code>+</code> or <code>−</code> each time a problem is generated.  A sign
 * variable <code>s</code> is automatically added to the formula context:
 * <ul>
 *   <li><code>s = 1</code>  when the rendered operator is <code>+</code></li>
 *   <li><code>s = -1</code> when the rendered operator is <code>−</code></li>
 * </ul>
 * This lets one answerFormula cover both cases.  Example:
 * <pre>
 *   template:      {a}x {+-} {b} = {c}
 *   answerFormula: (c - s * b) / a
 * </pre>
 * When <code>+</code> is chosen: question is <code>3x + 5 = 11</code>, answer = (11−5)/3 = 2<br>
 * When <code>−</code> is chosen: question is <code>3x − 5 = 1</code>,  answer = (1+5)/3 = 2
 *
 * <p>{@code parametersJson} shape:
 * <pre>{@code
 * {
 *   "template":      "{a}x {+-} {b} = {c}",
 *   "answerFormula": "(c - s * b) / a",
 *   "minValue":  1,
 *   "maxValue":  20
 * }
 * }</pre>
 */
@Service
public class FreeFormGenerator implements MathProblemGenerator {

    /**
     * Matches any {word} placeholder — letters, digits, underscores after the
     * first letter — with an optional {@code :min-max} range suffix, e.g.
     * {@code {a}} or {@code {a:1-50}}. Group 1 is the name; groups 2/3 are
     * the range bounds when present, else null.
     */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Za-z_]\\w*)(?::(\\d+)-(\\d+))?\\}");

    /** Special token that renders as + or − and exposes sign variable {@code s}. */
    private static final String SIGN_TOKEN = "{+-}";

    private static final Random RNG = new Random();

    @Override
    public MathProblemKind kind() {
        return MathProblemKind.FREE_FORM;
    }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);

        String rawTemplate = (cfg.template != null && !cfg.template.isBlank())
                ? cfg.template
                : "{a}";   // safe fallback

        int min = cfg.minValue;
        int max = Math.max(min + 1, cfg.maxValue);   // guard: max must be > min

        // ── Handle {+-} sign token ───────────────────────────────────────────
        // Resolve before scanning regular placeholders so 's' is available in formula.
        Map<String, Integer> assignments = new LinkedHashMap<>();
        String workingTemplate = rawTemplate;
        if (workingTemplate.contains(SIGN_TOKEN)) {
            boolean plus = RNG.nextBoolean();
            // s = 1 for "+", s = -1 for "-"
            assignments.put("s", plus ? 1 : -1);
            workingTemplate = workingTemplate.replace(SIGN_TOKEN, plus ? "+" : "−"); // − (minus sign)
        }

        // ── Collect + substitute {name} / {name:min-max} placeholders in one pass ──
        // Single-pass matcher substitution (rather than collect-then-string-replace)
        // so that a later bare {name} correctly reuses the value assigned to an
        // earlier {name:min-max} occurrence, and vice versa — a plain string
        // replace keyed on "{name}" would miss occurrences that carry a range
        // suffix.
        Matcher m = PLACEHOLDER.matcher(workingTemplate);
        StringBuilder exprBuilder = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            Integer value = assignments.get(name);
            if (value == null) {
                int lo = min, hi = max;
                if (m.group(2) != null && m.group(3) != null) {
                    int rangeLo = Integer.parseInt(m.group(2));
                    int rangeHi = Integer.parseInt(m.group(3));
                    lo = rangeLo;
                    hi = Math.max(rangeLo + 1, rangeHi); // guard: max must be > min
                }
                value = randomInRange(lo, hi);
                assignments.put(name, value);
            }
            m.appendReplacement(exprBuilder, Matcher.quoteReplacement(String.valueOf(value)));
        }
        m.appendTail(exprBuilder);
        String expression = exprBuilder.toString();

        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.0;

        // Compute expected answer from the formula if one is provided.
        List<Rational> answers = List.of();
        if (cfg.answerFormula != null && !cfg.answerFormula.isBlank()) {
            try {
                Rational result = RatExpr.eval(cfg.answerFormula, assignments);
                answers = List.of(result);
            } catch (Exception ignored) {
                // Bad formula — fall back to display-only
            }
        }

        return new GeneratedMathProblem(
                template.getId(),
                expression,
                com.gedtutor.dto.AnswerShape.SCALAR,
                answers,
                null,
                Set.of(),
                tol,
                template.getVideoUrl(),
                template.getDecimalPlaces(),
                template.isRoundAnswer()
        );
    }

    // ── Config POJO (deserialized from parametersJson) ───────────────────────

    public static final class Config {
        /**
         * LaTeX-friendly template string with {placeholder} tokens.
         * Use {+-} to randomly insert + or − and expose sign variable s (1 or -1).
         */
        public String template = "{a}";

        /**
         * Arithmetic formula for the correct answer, referencing the same
         * placeholder names as the template.  e.g. {@code (c - s * b) / a}.
         * Supports +, -, *, / and parentheses. Null or blank → display-only.
         * When {+-} is used, variable {@code s} is available (1 for +, -1 for -).
         */
        public String answerFormula;

        /** Minimum value for random number generation (inclusive). */
        public int minValue = 1;

        /** Maximum value for random number generation (inclusive). */
        public int maxValue = 20;
    }

    // ── Rational-arithmetic expression evaluator ─────────────────────────────

    /**
     * Evaluates a simple arithmetic expression using exact {@link Rational}
     * arithmetic. Supports variables, integer literals, +, -, *, / and
     * parentheses (no floating-point, no precision loss).
     *
     * <p>Grammar (whitespace ignored):
     * <pre>
     *   expr   → term  (('+' | '-') term)*
     *   term   → unary (('*' | '/') unary)*
     *   unary  → '-' primary | primary
     *   primary→ '(' expr ')' | INTEGER | IDENTIFIER
     * </pre>
     */
    private static final class RatExpr {

        private final String s;
        private final Map<String, Integer> vars;
        private int pos;

        private RatExpr(String s, Map<String, Integer> vars) {
            this.s = s.replaceAll("\\s+", "");
            this.vars = vars;
        }

        static Rational eval(String formula, Map<String, Integer> vars) {
            RatExpr p = new RatExpr(formula, vars);
            Rational r = p.parseExpr();
            if (p.pos < p.s.length())
                throw new IllegalArgumentException("Unexpected: " + p.s.charAt(p.pos));
            return r;
        }

        private Rational parseExpr() {
            Rational r = parseTerm();
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == '+') { pos++; r = add(r, parseTerm()); }
                else if (c == '-') { pos++; r = sub(r, parseTerm()); }
                else break;
            }
            return r;
        }

        private Rational parseTerm() {
            Rational r = parseUnary();
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == '*') { pos++; r = mul(r, parseUnary()); }
                else if (c == '/') { pos++; r = div(r, parseUnary()); }
                else break;
            }
            return r;
        }

        private Rational parseUnary() {
            if (pos < s.length() && s.charAt(pos) == '-') { pos++; return neg(parsePrimary()); }
            return parsePrimary();
        }

        private Rational parsePrimary() {
            if (pos >= s.length()) throw new IllegalArgumentException("Unexpected end of formula");
            char c = s.charAt(pos);
            if (c == '(') {
                pos++;
                Rational r = parseExpr();
                if (pos < s.length() && s.charAt(pos) == ')') pos++;
                return r;
            }
            if (Character.isDigit(c)) {
                int start = pos;
                while (pos < s.length() && Character.isDigit(s.charAt(pos))) pos++;
                return Rational.of(Long.parseLong(s.substring(start, pos)), 1);
            }
            if (Character.isLetter(c) || c == '_') {
                int start = pos;
                while (pos < s.length() && (Character.isLetterOrDigit(s.charAt(pos)) || s.charAt(pos) == '_')) pos++;
                String name = s.substring(start, pos);
                Integer val = vars.get(name);
                if (val == null) throw new IllegalArgumentException("Unknown variable: " + name);
                return Rational.of(val, 1);
            }
            throw new IllegalArgumentException("Unexpected character: " + c);
        }

        private static Rational add(Rational a, Rational b) {
            return Rational.of(a.numerator() * b.denominator() + b.numerator() * a.denominator(),
                               a.denominator() * b.denominator());
        }
        private static Rational sub(Rational a, Rational b) {
            return Rational.of(a.numerator() * b.denominator() - b.numerator() * a.denominator(),
                               a.denominator() * b.denominator());
        }
        private static Rational mul(Rational a, Rational b) {
            return Rational.of(a.numerator() * b.numerator(), a.denominator() * b.denominator());
        }
        private static Rational div(Rational a, Rational b) {
            if (b.numerator() == 0) throw new ArithmeticException("Division by zero in formula");
            return Rational.of(a.numerator() * b.denominator(), a.denominator() * b.numerator());
        }
        private static Rational neg(Rational a) {
            return Rational.of(-a.numerator(), a.denominator());
        }
    }
}

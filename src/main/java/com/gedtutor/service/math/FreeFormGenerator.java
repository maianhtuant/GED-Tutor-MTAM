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
 * <h3>Functions</h3>
 * <p>The answer formula supports a range of built-in functions, e.g.
 * <code>lcm(a, b)</code> or <code>sqrt(a)</code>:
 * <ul>
 *   <li><b>Number theory:</b> {@code gcd(...)}, {@code lcm(...)} — 1+ whole-number args</li>
 *   <li><b>General math:</b> {@code min(...)}, {@code max(...)} — 1+ args of any kind;
 *       {@code abs(x)}, {@code sqrt(x)}, {@code floor(x)}, {@code ceil(x)}, {@code round(x)} — 1 arg;
 *       {@code mod(a, b)}, {@code pow(base, exp)} — 2 args</li>
 *   <li><b>Trigonometry:</b> {@code sin(deg)}, {@code cos(deg)}, {@code tan(deg)} — degrees;
 *       {@code sinr(rad)}, {@code cosr(rad)}, {@code tanr(rad)} — radians</li>
 *   <li><b>Logs &amp; exponents:</b> {@code log(x)} (base 10), {@code ln(x)} (natural log), {@code exp(x)} (e^x)</li>
 *   <li><b>Combinatorics:</b> {@code fact(n)}, {@code nCr(n, r)}, {@code nPr(n, r)}</li>
 *   <li><b>Statistics:</b> {@code mean(...)}, {@code median(...)} — 1+ args</li>
 * </ul>
 * <p>Results that aren't naturally exact fractions (sqrt of a non-perfect-square,
 * trig, log, pow with a fractional exponent) are computed with double-precision
 * floating point and stored as a high-precision approximate fraction — plenty
 * of accuracy for the template's tolerancePercent/decimalPlaces grading.
 * Function names are matched case-insensitively (<code>nCr</code>, <code>NCR</code>,
 * <code>ncr</code> all work).
 *
 * <h3>Comparisons &amp; ternary</h3>
 * <p>Formulas can branch on a comparison using <code>&gt; &lt; &gt;= &lt;= == !=</code>
 * and the ternary operator, e.g. <code>a &gt; b ? a - b : b - a</code> (an absolute
 * difference, equivalent to <code>abs(a - b)</code>). Both branches are evaluated
 * before the condition picks one — the ternary does <em>not</em> short-circuit — so
 * avoid formulas where the untaken branch alone would divide by zero.
 * Example:
 * <pre>
 *   template:      Least Common Multiple of {a:2-20} and {b:2-5}
 *   answerFormula: lcm(a, b)
 * </pre>
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
         * placeholder names as the template.  e.g. {@code (c - s * b) / a}
         * or {@code lcm(a, b)}. Supports +, -, *, / and parentheses; the
         * functions gcd(), lcm(), min(), max(), abs(), sqrt(), floor(),
         * ceil(), round(), mod(), pow(), sin()/cos()/tan() (degrees),
         * sinr()/cosr()/tanr() (radians), log(), ln(), exp(), fact(),
         * nCr(), nPr(), mean(), median(); and comparisons/ternary
         * (a &gt; b ? x : y). Null or blank → display-only. When {+-} is
         * used, variable {@code s} is available (1 for +, -1 for -).
         */
        public String answerFormula;

        /** Minimum value for random number generation (inclusive). */
        public int minValue = 1;

        /** Maximum value for random number generation (inclusive). */
        public int maxValue = 20;
    }

    // ── Rational-arithmetic expression evaluator ─────────────────────────────

    /**
     * Evaluates an arithmetic expression using exact {@link Rational}
     * arithmetic where possible (no floating-point, no precision loss),
     * falling back to a high-precision double-based approximation for
     * functions whose results are inherently irrational (sqrt, trig, log).
     *
     * <p>Grammar (whitespace ignored):
     * <pre>
     *   ternary → compare ('?' ternary ':' ternary)?
     *   compare → expr (('>' | '<' | '>=' | '<=' | '==' | '!=') expr)?
     *   expr    → term  (('+' | '-') term)*
     *   term    → unary (('*' | '/') unary)*
     *   unary   → '-' primary | primary
     *   primary → '(' ternary ')' | INTEGER | IDENTIFIER ['(' ternary (',' ternary)* ')']
     * </pre>
     * <p>An IDENTIFIER immediately followed by '(' is a function call;
     * otherwise it's a variable lookup. A comparison evaluates to 1 (true)
     * or 0 (false), which the ternary operator then branches on — both
     * ternary branches are always evaluated (no short-circuiting).
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
            Rational r = p.parseTernary();
            if (p.pos < p.s.length())
                throw new IllegalArgumentException("Unexpected: " + p.s.charAt(p.pos));
            return r;
        }

        private static final Rational TRUE = Rational.of(1, 1);
        private static final Rational FALSE = Rational.of(0, 1);

        private Rational parseTernary() {
            Rational cond = parseCompare();
            if (pos < s.length() && s.charAt(pos) == '?') {
                pos++;
                Rational whenTrue = parseTernary();
                if (pos < s.length() && s.charAt(pos) == ':') pos++;
                else throw new IllegalArgumentException("Expected ':' in ternary expression");
                Rational whenFalse = parseTernary();
                return cond.numerator() != 0 ? whenTrue : whenFalse;
            }
            return cond;
        }

        private Rational parseCompare() {
            Rational left = parseExpr();
            if (pos >= s.length()) return left;
            char c = s.charAt(pos);
            String op = null;
            if ((c == '>' || c == '<') ) {
                if (pos + 1 < s.length() && s.charAt(pos + 1) == '=') { op = c + "="; pos += 2; }
                else { op = String.valueOf(c); pos += 1; }
            } else if (c == '=' && pos + 1 < s.length() && s.charAt(pos + 1) == '=') {
                op = "=="; pos += 2;
            } else if (c == '!' && pos + 1 < s.length() && s.charAt(pos + 1) == '=') {
                op = "!="; pos += 2;
            }
            if (op == null) return left;
            Rational right = parseExpr();
            int cmp = compare(left, right);
            boolean result = switch (op) {
                case ">" -> cmp > 0;
                case "<" -> cmp < 0;
                case ">=" -> cmp >= 0;
                case "<=" -> cmp <= 0;
                case "==" -> cmp == 0;
                case "!=" -> cmp != 0;
                default -> throw new IllegalStateException("Unreachable: " + op);
            };
            return result ? TRUE : FALSE;
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
                Rational r = parseTernary();
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
                if (pos < s.length() && s.charAt(pos) == '(') {
                    pos++; // consume '('
                    List<Rational> args = new ArrayList<>();
                    if (pos < s.length() && s.charAt(pos) != ')') {
                        args.add(parseTernary());
                        while (pos < s.length() && s.charAt(pos) == ',') {
                            pos++;
                            args.add(parseTernary());
                        }
                    }
                    if (pos < s.length() && s.charAt(pos) == ')') pos++;
                    else throw new IllegalArgumentException("Expected ')' after arguments to " + name + "(...)");
                    return callFunction(name, args);
                }
                Integer val = vars.get(name);
                if (val == null) throw new IllegalArgumentException("Unknown variable: " + name);
                return Rational.of(val, 1);
            }
            throw new IllegalArgumentException("Unexpected character: " + c);
        }

        /** Precision used when a function's result isn't an exact fraction (sqrt, trig, log, ...). */
        private static final long IRRATIONAL_SCALE = 100_000_000L; // 8 decimal digits

        /** Built-in functions available to answer formulas — see class javadoc for the full list. */
        private static Rational callFunction(String rawName, List<Rational> args) {
            String name = rawName.toLowerCase(Locale.ROOT);
            switch (name) {
                case "abs": {
                    requireArgCount(name, args, 1, 1);
                    Rational a = args.get(0);
                    return a.numerator() < 0 ? neg(a) : a;
                }
                case "min": {
                    requireArgCount(name, args, 1, Integer.MAX_VALUE);
                    Rational best = args.get(0);
                    for (int i = 1; i < args.size(); i++) {
                        if (compare(args.get(i), best) < 0) best = args.get(i);
                    }
                    return best;
                }
                case "max": {
                    requireArgCount(name, args, 1, Integer.MAX_VALUE);
                    Rational best = args.get(0);
                    for (int i = 1; i < args.size(); i++) {
                        if (compare(args.get(i), best) > 0) best = args.get(i);
                    }
                    return best;
                }
                case "gcd": {
                    requireArgCount(name, args, 1, Integer.MAX_VALUE);
                    long g = 0;
                    for (Rational r : args) g = gcdLong(g, requireInteger(name, r));
                    return Rational.of(g, 1);
                }
                case "lcm": {
                    requireArgCount(name, args, 1, Integer.MAX_VALUE);
                    long l = 1;
                    for (Rational r : args) {
                        long v = requireInteger(name, r);
                        if (v == 0) return Rational.of(0, 1);
                        long g = gcdLong(l, v);
                        l = Math.abs((l / g) * v);
                    }
                    return Rational.of(l, 1);
                }
                case "mod": {
                    requireArgCount(name, args, 2, 2);
                    long a = requireInteger(name, args.get(0));
                    long b = requireInteger(name, args.get(1));
                    if (b == 0) throw new ArithmeticException("mod() by zero");
                    return Rational.of(Math.floorMod(a, b), 1);
                }
                case "pow": {
                    requireArgCount(name, args, 2, 2);
                    Rational base = args.get(0);
                    Rational expArg = args.get(1);
                    if (expArg.denominator() == 1) {
                        long e = expArg.numerator();
                        if (Math.abs(e) > 1000)
                            throw new IllegalArgumentException("pow() exponent too large: " + e);
                        Rational result = Rational.of(1, 1);
                        for (long i = 0; i < Math.abs(e); i++) result = mul(result, base);
                        if (e < 0) {
                            if (result.numerator() == 0) throw new ArithmeticException("pow(): 0 raised to a negative power");
                            result = div(Rational.of(1, 1), result);
                        }
                        return result;
                    }
                    return fromDouble(Math.pow(base.toDouble(), expArg.toDouble()));
                }
                case "sqrt": {
                    requireArgCount(name, args, 1, 1);
                    Rational a = args.get(0);
                    if (a.numerator() < 0) throw new ArithmeticException("sqrt() of a negative number");
                    long sqrtNum = exactIntSqrt(a.numerator());
                    long sqrtDen = exactIntSqrt(a.denominator());
                    if (sqrtNum >= 0 && sqrtDen >= 0) return Rational.of(sqrtNum, sqrtDen);
                    return fromDouble(Math.sqrt(a.toDouble()));
                }
                case "floor": {
                    requireArgCount(name, args, 1, 1);
                    Rational a = args.get(0);
                    return Rational.of(Math.floorDiv(a.numerator(), a.denominator()), 1);
                }
                case "ceil": {
                    requireArgCount(name, args, 1, 1);
                    Rational a = args.get(0);
                    return Rational.of(-Math.floorDiv(-a.numerator(), a.denominator()), 1);
                }
                case "round": {
                    requireArgCount(name, args, 1, 1);
                    Rational a = args.get(0);
                    // round-half-up: floor(a + 1/2) = floor((2*numerator + denominator) / (2*denominator))
                    long doubledNumerator = 2 * a.numerator() + a.denominator();
                    return Rational.of(Math.floorDiv(doubledNumerator, 2 * a.denominator()), 1);
                }
                case "sin": {
                    requireArgCount(name, args, 1, 1);
                    return fromDouble(Math.sin(Math.toRadians(args.get(0).toDouble())));
                }
                case "cos": {
                    requireArgCount(name, args, 1, 1);
                    return fromDouble(Math.cos(Math.toRadians(args.get(0).toDouble())));
                }
                case "tan": {
                    requireArgCount(name, args, 1, 1);
                    return fromDouble(Math.tan(Math.toRadians(args.get(0).toDouble())));
                }
                case "sinr": {
                    requireArgCount(name, args, 1, 1);
                    return fromDouble(Math.sin(args.get(0).toDouble()));
                }
                case "cosr": {
                    requireArgCount(name, args, 1, 1);
                    return fromDouble(Math.cos(args.get(0).toDouble()));
                }
                case "tanr": {
                    requireArgCount(name, args, 1, 1);
                    return fromDouble(Math.tan(args.get(0).toDouble()));
                }
                case "log": {
                    requireArgCount(name, args, 1, 1);
                    double v = args.get(0).toDouble();
                    if (v <= 0) throw new ArithmeticException("log() of a non-positive number");
                    return fromDouble(Math.log10(v));
                }
                case "ln": {
                    requireArgCount(name, args, 1, 1);
                    double v = args.get(0).toDouble();
                    if (v <= 0) throw new ArithmeticException("ln() of a non-positive number");
                    return fromDouble(Math.log(v));
                }
                case "exp": {
                    requireArgCount(name, args, 1, 1);
                    return fromDouble(Math.exp(args.get(0).toDouble()));
                }
                case "fact": {
                    requireArgCount(name, args, 1, 1);
                    long n = requireInteger(name, args.get(0));
                    if (n < 0) throw new ArithmeticException("fact() of a negative number");
                    long result = 1;
                    for (long i = 2; i <= n; i++) result = Math.multiplyExact(result, i);
                    return Rational.of(result, 1);
                }
                case "ncr": {
                    requireArgCount(name, args, 2, 2);
                    long n = requireInteger(name, args.get(0));
                    long r = requireInteger(name, args.get(1));
                    return Rational.of(combinations(n, r), 1);
                }
                case "npr": {
                    requireArgCount(name, args, 2, 2);
                    long n = requireInteger(name, args.get(0));
                    long r = requireInteger(name, args.get(1));
                    return Rational.of(permutations(n, r), 1);
                }
                case "mean": {
                    requireArgCount(name, args, 1, Integer.MAX_VALUE);
                    Rational sum = Rational.of(0, 1);
                    for (Rational r : args) sum = add(sum, r);
                    return div(sum, Rational.of(args.size(), 1));
                }
                case "median": {
                    requireArgCount(name, args, 1, Integer.MAX_VALUE);
                    List<Rational> sorted = new ArrayList<>(args);
                    sorted.sort(RatExpr::compare);
                    int mid = sorted.size() / 2;
                    if (sorted.size() % 2 == 1) return sorted.get(mid);
                    return div(add(sorted.get(mid - 1), sorted.get(mid)), Rational.of(2, 1));
                }
                default:
                    throw new IllegalArgumentException("Unknown function: " + rawName + "(). Supported: gcd, lcm, min, max, "
                            + "abs, sqrt, floor, ceil, round, mod, pow, sin, cos, tan, sinr, cosr, tanr, "
                            + "log, ln, exp, fact, nCr, nPr, mean, median.");
            }
        }

        private static void requireArgCount(String fn, List<Rational> args, int min, int max) {
            if (args.size() < min || args.size() > max) {
                throw new IllegalArgumentException(fn + "() expects at least " + min
                        + (max == Integer.MAX_VALUE ? "" : " and at most " + max)
                        + " argument(s), got " + args.size());
            }
        }

        /** gcd()/lcm()/mod()/fact()/nCr()/nPr() only make sense on whole numbers. */
        private static long requireInteger(String fn, Rational r) {
            if (r.denominator() != 1) {
                throw new IllegalArgumentException(fn + "() requires whole-number arguments, got "
                        + r.numerator() + "/" + r.denominator());
            }
            return r.numerator();
        }

        private static long gcdLong(long a, long b) {
            a = Math.abs(a);
            b = Math.abs(b);
            while (b != 0) {
                long t = b;
                b = a % b;
                a = t;
            }
            return a;
        }

        private static long permutations(long n, long r) {
            if (n < 0 || r < 0 || r > n) throw new ArithmeticException("nPr() requires 0 <= r <= n");
            long result = 1;
            for (long i = 0; i < r; i++) result = Math.multiplyExact(result, n - i);
            return result;
        }

        private static long combinations(long n, long r) {
            if (n < 0 || r < 0 || r > n) throw new ArithmeticException("nCr() requires 0 <= r <= n");
            r = Math.min(r, n - r); // symmetry keeps the loop (and the numbers) small
            long result = 1;
            for (long i = 0; i < r; i++) {
                result = Math.multiplyExact(result, n - i);
                result /= (i + 1); // always exact at this point — standard nCr identity
            }
            return result;
        }

        /** Integer square root if {@code n} is a perfect square, else -1. */
        private static long exactIntSqrt(long n) {
            if (n < 0) return -1;
            long r = (long) Math.sqrt((double) n);
            for (long candidate = Math.max(0, r - 2); candidate <= r + 2; candidate++) {
                if (candidate * candidate == n) return candidate;
            }
            return -1;
        }

        /** Converts a double into a high-precision approximate Rational (see {@link #IRRATIONAL_SCALE}). */
        private static Rational fromDouble(double v) {
            if (Double.isNaN(v) || Double.isInfinite(v))
                throw new ArithmeticException("Result is not a finite number");
            return Rational.of(Math.round(v * IRRATIONAL_SCALE), IRRATIONAL_SCALE);
        }

        /** Cross-multiplication compare; relies on Rational normalizing denominators to be positive. */
        private static int compare(Rational a, Rational b) {
            long lhs = a.numerator() * b.denominator();
            long rhs = b.numerator() * a.denominator();
            return Long.compare(lhs, rhs);
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

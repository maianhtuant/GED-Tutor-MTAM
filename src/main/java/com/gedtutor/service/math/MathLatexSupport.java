package com.gedtutor.service.math;

/**
 * Shared helpers for building LaTeX math strings consumed by KaTeX in the browser.
 *
 * <p>Rules for callers:
 * <ul>
 *   <li>Prose (instructions, hints) must be wrapped in {@code \text{...}}.</li>
 *   <li>Return the entire question as a single LaTeX string; the renderer will
 *       call {@code katex.renderToString(str, {displayMode: true})}.</li>
 *   <li>Use {@link #frac(String, String)} instead of "/" wherever a stacked
 *       fraction is desired.</li>
 * </ul>
 */
public final class MathLatexSupport {

    private MathLatexSupport() {}

    // ── Prose wrappers ────────────────────────────────────────────────────────

    /** Wrap arbitrary text as a LaTeX prose span (no math). */
    public static String text(String s) {
        return "\\text{" + s + "}";
    }

    /** Small prose gap before a displayed expression. */
    public static String label(String prose) {
        return "\\text{" + prose + "}\\;";
    }

    // ── Basic building blocks ─────────────────────────────────────────────────

    /** \frac{num}{den} */
    public static String frac(String num, String den) {
        return "\\frac{" + num + "}{" + den + "}";
    }

    /** \frac{num}{den} from longs */
    public static String frac(long num, long den) {
        return frac(String.valueOf(num), String.valueOf(den));
    }

    /** \left( expr \right) */
    public static String paren(String expr) {
        return "\\left(" + expr + "\\right)";
    }

    /** x^{n} – braces included so multi-digit exponents render correctly. */
    public static String power(String base, int exp) {
        return base + "^{" + exp + "}";
    }

    // ── Signed-term helpers ───────────────────────────────────────────────────

    /**
     * Render a signed coefficient + variable as a LaTeX term:
     * {@code latexTerm(3, "x", false)} → {@code "3x"}
     * {@code latexTerm(-1, "x", false)} → {@code "-x"}
     * Used for the leading term of an expression.
     */
    public static String leadTerm(int coeff, String varPart) {
        if (coeff == 1)       return varPart;
        if (coeff == -1)      return "-" + varPart;
        return coeff + varPart;
    }

    /**
     * Render a subsequent term with explicit sign:
     * {@code signedTerm(3, "x")} → {@code " + 3x"}
     * {@code signedTerm(-3, "x")} → {@code " - 3x"}
     * {@code signedTerm(0, "x")} → {@code ""}
     */
    public static String signedTerm(int v, String varPart) {
        if (v == 0) return "";
        int abs = Math.abs(v);
        String term;
        if (varPart.isEmpty()) {
            term = String.valueOf(abs);
        } else if (abs == 1) {
            term = varPart;
        } else {
            term = abs + varPart;
        }
        return (v > 0 ? " + " : " - ") + term;
    }

    // ── Polynomial helpers ────────────────────────────────────────────────────

    /** Render {@code a·x^n} (leading term, suitable for LaTeX). */
    public static String polyLeadTerm(int a, int n) {
        String varPart = (n == 1) ? "x" : ("x^{" + n + "}");
        return leadTerm(a, varPart);
    }

    /** Render {@code ±a·x^n} as a subsequent (signed) term. */
    public static String polySignedTerm(int a, int n) {
        if (a == 0) return "";
        String varPart = (n == 1) ? "x" : ("x^{" + n + "}");
        return signedTerm(a, varPart);
    }

    /**
     * Render a quadratic {@code ax² + bx + c} in LaTeX.
     * The leading {@code a} coefficient is never 0.
     */
    public static String quadratic(int a, int b, int c) {
        return polyLeadTerm(a, 2) + polySignedTerm(b, 1) + signedTerm(c, "");
    }

    /**
     * Render a linear expression {@code ax + b} in LaTeX.
     */
    public static String linear(int a, int b) {
        return leadTerm(a, "x") + signedTerm(b, "");
    }

    // ── Calculus ──────────────────────────────────────────────────────────────

    /** \lim_{x \to c} */
    public static String lim(int c) {
        return "\\lim_{x \\to " + c + "}";
    }

    /** \int_{a}^{b} */
    public static String defInt(int a, int b) {
        return "\\int_{" + a + "}^{" + b + "}";
    }

    /** \iint_{R} */
    public static String doubleInt() {
        return "\\iint_{R}";
    }

    /** \frac{\partial f}{\partial var} */
    public static String partialDeriv(String funcName, String var) {
        return frac("\\partial " + funcName, "\\partial " + var);
    }

    /** \frac{d^{2}f}{dx^{2}} */
    public static String secondDeriv() {
        return frac("d^{2}f", "dx^{2}");
    }

    /** \frac{dy}{dx} */
    public static String dyOverDx() {
        return frac("dy", "dx");
    }

    /** \frac{dy}{dt} */
    public static String dyOverDt() {
        return frac("dy", "dt");
    }
}

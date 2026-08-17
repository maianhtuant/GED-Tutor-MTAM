package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Linear Algebra — Find the eigenvalues of a 2×2 matrix.
 *
 * <p>To guarantee clean integer eigenvalues, we construct the matrix
 * from its eigenvalues λ₁ and λ₂ and a random eigenvector basis:
 * <pre>
 *   A = P · diag(λ₁, λ₂) · P⁻¹
 * </pre>
 * We use P = [[1, 1],[0, 1]] (a simple upper-triangular basis), which gives:
 * <pre>
 *   A = [[λ₁, λ₁ − λ₂], [0, λ₂]]   (upper triangular — eigenvalues on diagonal)
 * </pre>
 * To make the problem non-trivial, we also generate a random off-diagonal entry b.
 * Then A = [[a, b],[0, d]] where a = λ₁, d = λ₂ — eigenvalues are still a and d.
 *
 * <p>For a fully general matrix, we use:
 * P = [[1, 1],[p, q]] with det(P) = q − p ≠ 0. Then:
 * <pre>
 *   A = P · D · P⁻¹   (computed below)
 * </pre>
 * Eigenvalues are always λ₁ and λ₂ (exact integers).
 *
 * <p>JSON config shape:
 * <pre>{@code {"eigenMin":-5,"eigenMax":5}}</pre>
 */
@Service
public class Eigenvalue2x2Generator implements MathProblemGenerator {

    public static final class Config {
        public int eigenMin = -5, eigenMax = 5;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.EIGENVALUE_2X2; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        // Pick two distinct integer eigenvalues (initialize to guarantee definite assignment)
        int lam1 = cfg.eigenMin;
        int lam2 = cfg.eigenMin + 1;
        for (int attempt = 0; attempt <= 20; attempt++) {
            lam1 = randomInRange(cfg.eigenMin, cfg.eigenMax);
            lam2 = randomInRange(cfg.eigenMin, cfg.eigenMax);
            if (lam1 != lam2) break;
            if (attempt == 20) lam2 = lam1 + 1; // ensure distinct on last try
        }

        // Build A = P · D · P⁻¹ with P = [[1, 1],[p, q]], det = q - p
        // Choose p, q such that det != 0
        int p = 0, q = 1;
        for (int attempt = 0; attempt <= 20; attempt++) {
            p = randomInRange(-3, 3);
            q = randomInRange(-3, 3);
            if (q != p) break;
            if (attempt == 20) { p = 0; q = 1; }
        }
        int detP = q - p; // det(P) = 1*q - 1*p = q - p

        // P⁻¹ = (1/detP) * [[q, -1],[-p, 1]]
        // A = P * D * P⁻¹
        // D = [[lam1, 0],[0, lam2]]
        // P*D = [[lam1, lam2],[p*lam1, q*lam2]]
        // A = (1/detP) * [[lam1, lam2],[p*lam1, q*lam2]] * [[q, -1],[-p, 1]]
        // A[0][0] = (lam1*q + lam2*(-p)) / detP = (lam1*q - lam2*p) / detP
        // A[0][1] = (lam1*(-1) + lam2*1) / detP = (lam2 - lam1) / detP
        // A[1][0] = (p*lam1*q + q*lam2*(-p)) / detP = pq(lam1 - lam2) / detP
        // A[1][1] = (p*lam1*(-1) + q*lam2*1) / detP = (q*lam2 - p*lam1) / detP
        long a11Num = (long) lam1 * q - (long) lam2 * p;
        long a12Num = (long) lam2 - lam1;
        long a21Num = (long) p * q * (lam1 - lam2);
        long a22Num = (long) q * lam2 - (long) p * lam1;

        // We need integer entries — only use if all entries divide evenly
        if (a11Num % detP == 0 && a12Num % detP == 0 && a21Num % detP == 0 && a22Num % detP == 0) {
            int a11 = (int)(a11Num / detP);
            int a12 = (int)(a12Num / detP);
            int a21 = (int)(a21Num / detP);
            int a22 = (int)(a22Num / detP);
            return buildProblem(template, a11, a12, a21, a22, lam1, lam2, tol);
        }

        // Fallback: upper-triangular matrix (eigenvalues on diagonal)
        int b = randomInRange(-4, 4);
        return buildProblem(template, lam1, b, 0, lam2, lam1, lam2, tol);
    }

    private GeneratedMathProblem buildProblem(MathProblemTemplate template,
                                               int a, int b, int c, int d,
                                               int lam1, int lam2, double tol) {
        List<Rational> answers = lam1 <= lam2
                ? List.of(Rational.of(lam1, 1), Rational.of(lam2, 1))
                : List.of(Rational.of(lam2, 1), Rational.of(lam1, 1));

        String question = MathLatexSupport.text("Find the eigenvalues of ")
                + "A = \\begin{pmatrix}" + a + " & " + b + " \\\\ " + c + " & " + d + "\\end{pmatrix}"
                + MathLatexSupport.text(". Enter both values, comma-separated.");

        return GeneratedMathProblem.unordered(
                template.getId(), question, answers, tol, template.getVideoUrl());
    }
}

package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Linear Algebra — Compute one entry of the product A · B for two 2×2 integer matrices.
 *
 * <p>The (i, j) entry of A·B is the dot product of row i of A with column j of B.
 * Asking for a single entry keeps the problem focused on understanding the
 * row-column multiplication rule before tackling full matrix products.
 *
 * <p>JSON config shape:
 * <pre>{@code {"min":-3,"max":3,"rowIndex":0,"colIndex":0}}</pre>
 * {@code rowIndex} and {@code colIndex} are 0-based (0 or 1 for 2×2 matrices).
 * Set both to -1 to pick a random entry each time.
 */
@Service
public class MatrixMultiplyEntryGenerator implements MathProblemGenerator {

    public static final class Config {
        public int min = -3, max = 3;
        public int rowIndex = -1; // -1 = random
        public int colIndex = -1; // -1 = random
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.MATRIX_MULTIPLY_ENTRY; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        // Generate two random 2×2 matrices stored row-major: [row0col0, row0col1, row1col0, row1col1]
        int[] A = new int[4];
        int[] B = new int[4];
        for (int i = 0; i < 4; i++) {
            A[i] = randomInRange(cfg.min, cfg.max);
            B[i] = randomInRange(cfg.min, cfg.max);
        }

        int row = (cfg.rowIndex < 0 || cfg.rowIndex > 1) ? randomInRange(0, 1) : cfg.rowIndex;
        int col = (cfg.colIndex < 0 || cfg.colIndex > 1) ? randomInRange(0, 1) : cfg.colIndex;

        // (A·B)[row][col] = A[row][0]*B[0][col] + A[row][1]*B[1][col]
        int aRow0 = A[row * 2];
        int aRow1 = A[row * 2 + 1];
        int bCol0 = B[col];          // row 0, col 'col'
        int bCol1 = B[2 + col];      // row 1, col 'col'
        long answer = (long) aRow0 * bCol0 + (long) aRow1 * bCol1;

        String rowLabel = (row == 0) ? "1^{st}" : "2^{nd}";
        String colLabel = (col == 0) ? "1^{st}" : "2^{nd}";
        String question = MathLatexSupport.text("Find the ") + rowLabel
                + MathLatexSupport.text(" row, ") + colLabel
                + MathLatexSupport.text(" column entry of ")
                + "A \\cdot B"
                + MathLatexSupport.text(" where ")
                + "A = " + matLatex(A)
                + MathLatexSupport.text(", ")
                + "B = " + matLatex(B)
                + MathLatexSupport.text(".");

        return GeneratedMathProblem.scalar(
                template.getId(), question, Rational.of(answer, 1), tol, template.getVideoUrl());
    }

    /** Format a 2×2 matrix as LaTeX pmatrix. */
    static String matLatex(int[] m) {
        return "\\begin{pmatrix}" + m[0] + " & " + m[1] + " \\\\ " + m[2] + " & " + m[3] + "\\end{pmatrix}";
    }

    /** Format a 2×2 matrix from row-major array as "[[a, b], [c, d]]" (legacy). */
    static String matStr(int[] m) {
        return "[[" + m[0] + ", " + m[1] + "], [" + m[2] + ", " + m[3] + "]]";
    }
}

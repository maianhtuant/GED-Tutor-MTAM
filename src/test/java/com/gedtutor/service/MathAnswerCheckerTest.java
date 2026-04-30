package com.gedtutor.service;

import com.gedtutor.dto.AnswerShape;
import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.MathAnswerResult;
import com.gedtutor.dto.Rational;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MathAnswerCheckerTest {

    private final MathAnswerChecker checker = new MathAnswerChecker();

    @Test
    void scalarIntegerAccepted() {
        GeneratedMathProblem p = scalar(5.0, 0.5);
        assertTrue(checker.check(p, "5").correct());
        assertTrue(checker.check(p, "5.0").correct());
    }

    @Test
    void scalarFractionAccepted() {
        GeneratedMathProblem p = scalar(2.5, 0.5);
        assertTrue(checker.check(p, "5/2").correct());
        assertTrue(checker.check(p, "2.5").correct());
    }

    @Test
    void unorderedAcceptsEitherOrder() {
        GeneratedMathProblem p = unordered(2.0, 3.0, 0.5);
        assertTrue(checker.check(p, "2, 3").correct());
        assertTrue(checker.check(p, "3, 2").correct());
        assertTrue(checker.check(p, "3 and 2").correct());
    }

    @Test
    void orderedRequiresCorrectOrder() {
        GeneratedMathProblem p = ordered(2.0, 3.0, 0.5);
        assertTrue(checker.check(p, "2, 3").correct());
        // Reversed should be wrong.
        assertFalse(checker.check(p, "3, 2").correct());
        // Parens accepted.
        assertTrue(checker.check(p, "(2, 3)").correct());
        // x = / y = prefixes accepted.
        assertTrue(checker.check(p, "x=2, y=3").correct());
    }

    @Test
    void wrongCountIsRejected() {
        GeneratedMathProblem p = unordered(2.0, 3.0, 0.5);
        MathAnswerResult r = checker.check(p, "2");
        assertFalse(r.correct());
    }

    @Test
    void specialAnswerAccepted() {
        GeneratedMathProblem p = special("no real solution",
                Set.of("no real solution", "no solution", "none"));
        assertTrue(checker.check(p, "no real solution").correct());
        assertTrue(checker.check(p, "No Solution").correct());
        assertTrue(checker.check(p, "NONE").correct());
        // Numeric answer should fail.
        assertFalse(checker.check(p, "1, 2").correct());
    }

    @Test
    void specialTokenRejectedForNumericProblem() {
        GeneratedMathProblem p = scalar(5.0, 0.5);
        assertFalse(checker.check(p, "no real solution").correct());
        assertFalse(checker.check(p, "undefined").correct());
    }

    @Test
    void undefinedAcceptedForVerticalSlope() {
        GeneratedMathProblem p = special("undefined",
                Set.of("undefined", "no slope", "vertical"));
        assertTrue(checker.check(p, "undefined").correct());
        assertTrue(checker.check(p, "Vertical").correct());
        assertFalse(checker.check(p, "0").correct());
    }

    @Test
    void tolerancePassesNearMatchesAndFailsFarOnes() {
        GeneratedMathProblem p = unordered(10.0/3.0, 5.0, 0.5);
        assertTrue(checker.check(p, "3.33, 5").correct());
        assertTrue(checker.check(p, "3.333, 5").correct());
        assertFalse(checker.check(p, "3.3, 5").correct());

        GeneratedMathProblem loose = unordered(10.0/3.0, 5.0, 2.0);
        assertTrue(checker.check(loose, "3.3, 5").correct());
    }

    @Test
    void zeroValueUsesAbsoluteFloor() {
        GeneratedMathProblem p = unordered(0.0, 5.0, 0.5);
        assertTrue(checker.check(p, "0, 5").correct());
        assertTrue(checker.check(p, "0.0, 5").correct());
    }

    @Test
    void garbageInputRejectedGracefully() {
        GeneratedMathProblem p = unordered(2.0, 3.0, 0.5);
        MathAnswerResult r = checker.check(p, "two, three");
        assertFalse(r.correct());
        assertTrue(r.message().toLowerCase().contains("number"));
    }

    @Test
    void parseOneNumber_handlesFractions() {
        assertEquals(2.5, MathAnswerChecker.parseOneNumber("5/2"), 1e-9);
        assertEquals(-3.0, MathAnswerChecker.parseOneNumber("-3"), 1e-9);
        assertEquals(10.0/3.0, MathAnswerChecker.parseOneNumber("10/3"), 1e-9);
    }

    // --- helpers ---

    private static GeneratedMathProblem scalar(double r, double tolPct) {
        return GeneratedMathProblem.scalar(1L, "stub", Rational.ofDouble(r), tolPct, null);
    }

    private static GeneratedMathProblem unordered(double r1, double r2, double tolPct) {
        return GeneratedMathProblem.unordered(1L, "stub",
                List.of(Rational.ofDouble(r1), Rational.ofDouble(r2)), tolPct, null);
    }

    private static GeneratedMathProblem ordered(double r1, double r2, double tolPct) {
        return GeneratedMathProblem.ordered(1L, "stub",
                List.of(Rational.ofDouble(r1), Rational.ofDouble(r2)), tolPct, null);
    }

    private static GeneratedMathProblem special(String special, Set<String> aliases) {
        return GeneratedMathProblem.special(1L, "stub", special, aliases, 0.5, null);
    }
}

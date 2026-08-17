package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import com.gedtutor.model.MathQuizAnswer;
import com.gedtutor.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MathQuizAnswerRepository extends JpaRepository<MathQuizAnswer, Long> {

    List<MathQuizAnswer> findByAttemptOrderByQuestionIndexAsc(QuizAttempt attempt);

    /** Delete all math-quiz answers belonging to any attempt for this homework. */
    @Modifying
    @Query("DELETE FROM MathQuizAnswer a WHERE a.attempt IN (SELECT att FROM QuizAttempt att WHERE att.homework = :homework)")
    void deleteByHomework(@Param("homework") Homework homework);
}

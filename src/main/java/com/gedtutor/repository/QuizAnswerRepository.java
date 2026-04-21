package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import com.gedtutor.model.QuizAnswer;
import com.gedtutor.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    @Query("SELECT a FROM QuizAnswer a JOIN FETCH a.question WHERE a.attempt = :attempt")
    List<QuizAnswer> findByAttemptWithQuestion(@Param("attempt") QuizAttempt attempt);

    /** Delete all quiz answers belonging to any attempt for this homework. */
    @Modifying
    @Query("DELETE FROM QuizAnswer a WHERE a.attempt IN (SELECT att FROM QuizAttempt att WHERE att.homework = :homework)")
    void deleteByHomework(@Param("homework") Homework homework);
}

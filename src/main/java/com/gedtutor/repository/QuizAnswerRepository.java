package com.gedtutor.repository;

import com.gedtutor.model.QuizAnswer;
import com.gedtutor.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    @Query("SELECT a FROM QuizAnswer a JOIN FETCH a.question WHERE a.attempt = :attempt")
    List<QuizAnswer> findByAttemptWithQuestion(@Param("attempt") QuizAttempt attempt);
}

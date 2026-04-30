package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import com.gedtutor.model.QuizAttempt;
import com.gedtutor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByStudentAndHomeworkOrderByAttemptNumberDesc(User student, Homework homework);
    long countByStudentAndHomework(User student, Homework homework);
    Optional<QuizAttempt> findTopByStudentAndHomeworkOrderByAttemptNumberDesc(User student, Homework homework);

    List<QuizAttempt> findByHomework(Homework homework);

    /** Delete all attempts for a homework (used after answers + element-collection rows are cleared). */
    @Modifying
    @Query("DELETE FROM QuizAttempt a WHERE a.homework = :homework")
    void deleteByHomework(@Param("homework") Homework homework);
}

package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import com.gedtutor.model.QuizAttempt;
import com.gedtutor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByStudentAndHomeworkOrderByAttemptNumberDesc(User student, Homework homework);
    long countByStudentAndHomework(User student, Homework homework);
    Optional<QuizAttempt> findTopByStudentAndHomeworkOrderByAttemptNumberDesc(User student, Homework homework);

    /** Used when an admin deletes a homework — load attempts so JPA can cascade
     *  their answers and quiz_attempt_questions rows before the homework FK is dropped. */
    List<QuizAttempt> findByHomework(Homework homework);
}

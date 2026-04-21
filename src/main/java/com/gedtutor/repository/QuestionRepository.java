package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import com.gedtutor.model.Question;
import com.gedtutor.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Fetch a single question eagerly with its choices and subject, so the
     * edit form can render after the transaction closes (open-in-view=off).
     */
    @Query("SELECT DISTINCT q FROM Question q " +
            "LEFT JOIN FETCH q.choices " +
            "LEFT JOIN FETCH q.subject " +
            "WHERE q.id = :id")
    Optional<Question> findByIdWithChoices(@Param("id") Long id);

    /**
     * Every question for a homework, via the homework_questions join table,
     * with choices pre-loaded so the quiz page can render after the
     * transaction closes (open-in-view is off).
     */
    @Query("SELECT DISTINCT q FROM Homework h JOIN h.questions q LEFT JOIN FETCH q.choices " +
            "WHERE h = :homework ORDER BY q.orderIndex ASC")
    List<Question> findByHomeworkWithChoices(@Param("homework") Homework homework);

    /**
     * Fetch a set of questions by id, eagerly loading their choices so the view
     * layer can iterate them after the transaction closes (open-in-view is off).
     * Used for "pick N from pool" quiz attempts.
     */
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.choices WHERE q.id IN :ids")
    List<Question> findByIdInWithChoices(@Param("ids") Collection<Long> ids);

    /** All questions in the subject bank. Used to randomly pick N when a
     *  homework is created with a poolSize target. */
    List<Question> findBySubject(Subject subject);

    /** All questions with the subject pre-loaded — used by the global
     *  /admin/questions list page. */
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.subject s " +
            "ORDER BY COALESCE(s.name, 'zzz') ASC, q.orderIndex ASC")
    List<Question> findAllWithSubject();
}

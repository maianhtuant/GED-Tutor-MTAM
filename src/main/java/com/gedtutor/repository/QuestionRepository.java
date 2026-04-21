package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import com.gedtutor.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.choices WHERE q.homework = :homework ORDER BY q.orderIndex ASC")
    List<Question> findByHomeworkWithChoices(@Param("homework") Homework homework);

    long countByHomework(Homework homework);
}

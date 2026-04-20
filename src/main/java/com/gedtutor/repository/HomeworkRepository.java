package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findByPublishedTrueOrderByCreatedAtDesc();
    List<Homework> findAllByOrderByCreatedAtDesc();
}

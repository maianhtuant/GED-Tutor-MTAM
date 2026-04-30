package com.gedtutor.repository;

import com.gedtutor.model.PracticeSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeSetRepository extends JpaRepository<PracticeSet, Long> {
    List<PracticeSet> findByActiveTrueOrderByCreatedAtDesc();
    List<PracticeSet> findAllByOrderByCreatedAtDesc();
}

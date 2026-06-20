package com.gedtutor.repository;

import com.gedtutor.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findAllByOrderByDisplayOrderAscNameAsc();

    List<Subject> findByActiveTrueOrderByDisplayOrderAscNameAsc();

    /** Root subjects only (no parent) — used for the top-level tab bar. */
    List<Subject> findByParentIsNullAndActiveTrueOrderByDisplayOrderAscNameAsc();

    /** Children of a given parent subject — used for the sub-tab row. */
    List<Subject> findByParentOrderByDisplayOrderAscNameAsc(Subject parent);

    Optional<Subject> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}

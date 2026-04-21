package com.gedtutor.repository;

import com.gedtutor.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findAllByOrderByDisplayOrderAscNameAsc();

    List<Subject> findByActiveTrueOrderByDisplayOrderAscNameAsc();

    Optional<Subject> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}

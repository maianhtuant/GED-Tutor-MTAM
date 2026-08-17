package com.gedtutor.repository;

import com.gedtutor.model.PracticeAttempt;
import com.gedtutor.model.PracticeSet;
import com.gedtutor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PracticeAttemptRepository extends JpaRepository<PracticeAttempt, Long> {

    List<PracticeAttempt> findByPracticeSet(PracticeSet practiceSet);

    List<PracticeAttempt> findByStudentOrderByStartedAtDesc(User student);

    /** Used by admin reporting — student/practiceSet joined so the summary
     *  can be built inside one transaction without lazy-loading later. */
    @Query("SELECT a FROM PracticeAttempt a JOIN FETCH a.student JOIN FETCH a.practiceSet " +
           "WHERE a.completedAt IS NOT NULL")
    List<PracticeAttempt> findAllCompletedWithStudentAndSet();

    /** Used during practice-set delete to clear dependent attempts. */
    @Modifying
    @Query("DELETE FROM PracticeAttempt a WHERE a.practiceSet = :practiceSet")
    void deleteByPracticeSet(@Param("practiceSet") PracticeSet practiceSet);
}

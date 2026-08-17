package com.gedtutor.repository;

import com.gedtutor.model.PracticeAnswer;
import com.gedtutor.model.PracticeAttempt;
import com.gedtutor.model.PracticeSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PracticeAnswerRepository extends JpaRepository<PracticeAnswer, Long> {

    List<PracticeAnswer> findByAttemptOrderByQuestionIndexAsc(PracticeAttempt attempt);

    /** Delete all practice answers belonging to any attempt for this practice set. */
    @Modifying
    @Query("DELETE FROM PracticeAnswer a WHERE a.attempt IN (SELECT att FROM PracticeAttempt att WHERE att.practiceSet = :practiceSet)")
    void deleteByPracticeSet(@Param("practiceSet") PracticeSet practiceSet);
}

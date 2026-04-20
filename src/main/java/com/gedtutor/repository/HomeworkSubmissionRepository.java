package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import com.gedtutor.model.HomeworkSubmission;
import com.gedtutor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {
    Optional<HomeworkSubmission> findByHomeworkAndStudent(Homework homework, User student);
    List<HomeworkSubmission> findByStudentOrderBySubmittedAtDesc(User student);

    @Query("SELECT s FROM HomeworkSubmission s JOIN FETCH s.student WHERE s.homework = :homework ORDER BY s.submittedAt DESC")
    List<HomeworkSubmission> findByHomeworkOrderBySubmittedAtDesc(@Param("homework") Homework homework);
}

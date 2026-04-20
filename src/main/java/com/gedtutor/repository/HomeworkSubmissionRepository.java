package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import com.gedtutor.model.HomeworkSubmission;
import com.gedtutor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {
    Optional<HomeworkSubmission> findByHomeworkAndStudent(Homework homework, User student);
    List<HomeworkSubmission> findByStudentOrderBySubmittedAtDesc(User student);
    List<HomeworkSubmission> findByHomeworkOrderBySubmittedAtDesc(Homework homework);
}

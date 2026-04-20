package com.gedtutor.service;

import com.gedtutor.dto.HomeworkForm;
import com.gedtutor.model.Homework;
import com.gedtutor.model.HomeworkSubmission;
import com.gedtutor.model.User;
import com.gedtutor.model.Video;
import com.gedtutor.repository.HomeworkRepository;
import com.gedtutor.repository.HomeworkSubmissionRepository;
import com.gedtutor.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    private final VideoRepository videoRepository;

    public HomeworkService(HomeworkRepository homeworkRepository,
                           HomeworkSubmissionRepository submissionRepository,
                           VideoRepository videoRepository) {
        this.homeworkRepository = homeworkRepository;
        this.submissionRepository = submissionRepository;
        this.videoRepository = videoRepository;
    }

    public List<Homework> listPublished() {
        return homeworkRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    public List<Homework> listAll() {
        return homeworkRepository.findAllByOrderByCreatedAtDesc();
    }

    public Homework findById(Long id) {
        return homeworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Homework not found: " + id));
    }

    @Transactional
    public Homework save(HomeworkForm form) {
        Homework hw = (form.getId() == null)
                ? new Homework()
                : findById(form.getId());
        hw.setTitle(form.getTitle());
        hw.setInstructions(form.getInstructions());
        hw.setSubject(form.getSubject());
        hw.setDueDate(form.getDueDate());
        hw.setPublished(form.isPublished());

        if (form.getVideoId() != null) {
            Video v = videoRepository.findById(form.getVideoId()).orElse(null);
            hw.setVideo(v);
        } else {
            hw.setVideo(null);
        }

        return homeworkRepository.save(hw);
    }

    @Transactional
    public void delete(Long id) {
        homeworkRepository.deleteById(id);
    }

    // === Submissions ===

    public Optional<HomeworkSubmission> findMySubmission(Homework hw, User student) {
        return submissionRepository.findByHomeworkAndStudent(hw, student);
    }

    public List<HomeworkSubmission> mySubmissions(User student) {
        return submissionRepository.findByStudentOrderBySubmittedAtDesc(student);
    }

    public List<HomeworkSubmission> submissionsFor(Homework hw) {
        return submissionRepository.findByHomeworkOrderBySubmittedAtDesc(hw);
    }

    @Transactional
    public HomeworkSubmission submit(Long homeworkId, User student, String answer) {
        Homework hw = findById(homeworkId);
        HomeworkSubmission sub = submissionRepository.findByHomeworkAndStudent(hw, student)
                .orElseGet(() -> {
                    HomeworkSubmission s = new HomeworkSubmission();
                    s.setHomework(hw);
                    s.setStudent(student);
                    s.setSubmittedAt(LocalDateTime.now());
                    return s;
                });
        sub.setAnswer(answer);
        sub.setSubmittedAt(LocalDateTime.now());
        // Reset grade when re-submitting
        sub.setGrade(null);
        sub.setFeedback(null);
        sub.setGradedAt(null);
        return submissionRepository.save(sub);
    }

    @Transactional
    public HomeworkSubmission grade(Long submissionId, Integer grade, String feedback) {
        HomeworkSubmission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));
        sub.setGrade(grade);
        sub.setFeedback(feedback);
        sub.setGradedAt(LocalDateTime.now());
        return submissionRepository.save(sub);
    }
}

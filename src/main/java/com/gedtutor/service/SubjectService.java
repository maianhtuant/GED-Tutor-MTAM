package com.gedtutor.service;

import com.gedtutor.dto.SubjectForm;
import com.gedtutor.model.Subject;
import com.gedtutor.repository.HomeworkRepository;
import com.gedtutor.repository.SubjectRepository;
import com.gedtutor.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final VideoRepository videoRepository;
    private final HomeworkRepository homeworkRepository;

    public SubjectService(SubjectRepository subjectRepository,
                          VideoRepository videoRepository,
                          HomeworkRepository homeworkRepository) {
        this.subjectRepository = subjectRepository;
        this.videoRepository = videoRepository;
        this.homeworkRepository = homeworkRepository;
    }

    public List<Subject> listAll() {
        return subjectRepository.findAllByOrderByDisplayOrderAscNameAsc();
    }

    public List<Subject> listActive() {
        return subjectRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    /** Root subjects only (parent == null) — for the top-level tab bar. */
    public List<Subject> listRoots() {
        return subjectRepository.findByParentIsNullAndActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    /** Children of the given parent subject — for the sub-tab row. */
    public List<Subject> listChildren(Subject parent) {
        return subjectRepository.findByParentOrderByDisplayOrderAscNameAsc(parent);
    }

    public Subject findById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + id));
    }

    @Transactional
    public Subject save(SubjectForm form) {
        Subject s = (form.getId() == null) ? new Subject() : findById(form.getId());

        // Enforce unique name (case-insensitive), allowing the same record to keep its own name
        subjectRepository.findByNameIgnoreCase(form.getName().trim())
                .filter(existing -> !existing.getId().equals(s.getId()))
                .ifPresent(e -> { throw new IllegalArgumentException(
                        "A subject named '" + form.getName().trim() + "' already exists."); });

        s.setName(form.getName().trim());
        s.setDescription(form.getDescription());
        s.setDisplayOrder(form.getDisplayOrder());
        s.setActive(form.isActive());
        return subjectRepository.save(s);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Subject s = findById(id);
        s.setActive(active);
        subjectRepository.save(s);
    }

    /**
     * Delete a subject. Refuses if any Video or Homework still references it —
     * callers should catch IllegalStateException and show a friendly message.
     */
    @Transactional
    public void delete(Long id) {
        Subject s = findById(id);
        long videoCount = videoRepository.countBySubject(s);
        long homeworkCount = homeworkRepository.countBySubject(s);
        if (videoCount > 0 || homeworkCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete '" + s.getName() + "': it is used by "
                            + videoCount + " video(s) and " + homeworkCount + " homework item(s). "
                            + "Reassign or delete those first, or deactivate this subject instead.");
        }
        subjectRepository.delete(s);
    }
}

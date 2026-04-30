package com.gedtutor.repository;

import com.gedtutor.model.Homework;
import com.gedtutor.model.Subject;
import com.gedtutor.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findByPublishedTrueOrderByCreatedAtDesc();
    List<Homework> findAllByOrderByCreatedAtDesc();

    long countBySubject(Subject subject);

    /** All homework assignments that reference this video lesson. */
    List<Homework> findByVideo(Video video);
}

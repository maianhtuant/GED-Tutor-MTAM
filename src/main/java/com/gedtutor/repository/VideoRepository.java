package com.gedtutor.repository;

import com.gedtutor.model.GedSubject;
import com.gedtutor.model.Video;
import com.gedtutor.model.VideoVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByVisibilityOrderByCreatedAtDesc(VideoVisibility visibility);
    List<Video> findByVisibilityAndSubjectOrderByCreatedAtDesc(VideoVisibility visibility, GedSubject subject);
    List<Video> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE Video v SET v.visibility = :visibility")
    void updateAllVisibility(VideoVisibility visibility);
}

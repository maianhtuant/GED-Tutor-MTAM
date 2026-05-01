package com.gedtutor.repository;

import com.gedtutor.model.Subject;
import com.gedtutor.model.Video;
import com.gedtutor.model.VideoVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByVisibilityOrderByCreatedAtDesc(VideoVisibility visibility);
    List<Video> findByVisibilityAndSubjectOrderByCreatedAtDesc(VideoVisibility visibility, Subject subject);
    List<Video> findAllByOrderByCreatedAtDesc();

    long countBySubject(Subject subject);

    /** Eagerly fetches mathTemplate (LAZY) so the video-detail page can read it outside the session. */
    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.mathTemplate WHERE v.id = :id")
    Optional<Video> findByIdWithTemplate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Video v SET v.visibility = :visibility")
    void updateAllVisibility(VideoVisibility visibility);
}

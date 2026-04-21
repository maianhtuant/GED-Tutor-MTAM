package com.gedtutor.service;

import com.gedtutor.dto.VideoForm;
import com.gedtutor.model.Subject;
import com.gedtutor.model.User;
import com.gedtutor.model.Video;
import com.gedtutor.model.VideoVisibility;
import com.gedtutor.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoUrlParser urlParser;
    private final SubjectService subjectService;

    public VideoService(VideoRepository videoRepository,
                        VideoUrlParser urlParser,
                        SubjectService subjectService) {
        this.videoRepository = videoRepository;
        this.urlParser = urlParser;
        this.subjectService = subjectService;
    }

    public List<Video> listPublic() {
        return videoRepository.findByVisibilityOrderByCreatedAtDesc(VideoVisibility.PUBLIC);
    }

    public List<Video> listPublicBySubject(Long subjectId) {
        if (subjectId == null) return listPublic();
        Subject subject = subjectService.findById(subjectId);
        return videoRepository.findByVisibilityAndSubjectOrderByCreatedAtDesc(VideoVisibility.PUBLIC, subject);
    }

    public List<Video> listAll() {
        return videoRepository.findAllByOrderByCreatedAtDesc();
    }

    public Video findById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + id));
    }

    @Transactional
    public Video create(VideoForm form, User uploader) {
        VideoUrlParser.Parsed parsed = urlParser.parse(form.getVideoUrl());
        Video v = new Video();
        v.setTitle(form.getTitle());
        v.setDescription(form.getDescription());
        v.setVideoUrl(form.getVideoUrl());
        v.setProvider(parsed.provider());
        v.setEmbedId(parsed.embedId());
        v.setSubject(subjectService.findById(form.getSubjectId()));
        v.setVisibility(form.getVisibility());
        v.setUploadedBy(uploader);
        return videoRepository.save(v);
    }

    @Transactional
    public Video update(Long id, VideoForm form) {
        Video v = findById(id);
        v.setTitle(form.getTitle());
        v.setDescription(form.getDescription());
        if (!v.getVideoUrl().equals(form.getVideoUrl())) {
            VideoUrlParser.Parsed parsed = urlParser.parse(form.getVideoUrl());
            v.setVideoUrl(form.getVideoUrl());
            v.setProvider(parsed.provider());
            v.setEmbedId(parsed.embedId());
        }
        v.setSubject(subjectService.findById(form.getSubjectId()));
        v.setVisibility(form.getVisibility());
        return videoRepository.save(v);
    }

    @Transactional
    public void setVisibility(Long id, VideoVisibility visibility) {
        Video v = findById(id);
        v.setVisibility(visibility);
        videoRepository.save(v);
    }

    @Transactional
    public void setAllPrivate() {
        videoRepository.updateAllVisibility(VideoVisibility.PRIVATE);
    }

    @Transactional
    public void delete(Long id) {
        videoRepository.deleteById(id);
    }

    public String embedUrl(Video v) {
        return urlParser.embedUrl(v.getProvider(), v.getEmbedId());
    }
}

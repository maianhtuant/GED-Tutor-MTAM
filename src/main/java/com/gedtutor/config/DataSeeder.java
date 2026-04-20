package com.gedtutor.config;

import com.gedtutor.model.*;
import com.gedtutor.repository.HomeworkRepository;
import com.gedtutor.repository.UserRepository;
import com.gedtutor.repository.VideoRepository;
import com.gedtutor.service.VideoUrlParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Seeds a default admin user, a demo student, and a few sample videos/homework on first run.
 * Safe to run repeatedly — it only inserts if the row is missing.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository users,
                           VideoRepository videos,
                           HomeworkRepository homework,
                           VideoUrlParser urlParser,
                           PasswordEncoder encoder) {
        return args -> {
            User admin = users.findByUsername("admin").orElseGet(() -> {
                User u = new User();
                u.setUsername("admin");
                u.setEmail("admin@gedtutor.local");
                u.setFullName("Site Admin");
                u.setPassword(encoder.encode("admin123"));
                u.setRole(Role.ADMIN);
                u.setEnabled(true);
                return users.save(u);
            });

            users.findByUsername("student").orElseGet(() -> {
                User u = new User();
                u.setUsername("student");
                u.setEmail("student@gedtutor.local");
                u.setFullName("Demo Student");
                u.setPassword(encoder.encode("student123"));
                u.setRole(Role.STUDENT);
                u.setEnabled(true);
                return users.save(u);
            });

            if (videos.count() == 0) {
                saveVideo(videos, urlParser, admin,
                        "Intro to GED Math",
                        "Overview of the GED Math test and key topics.",
                        "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                        GedSubject.MATH, VideoVisibility.PUBLIC);
                saveVideo(videos, urlParser, admin,
                        "Algebra Basics: Solving Linear Equations",
                        "Learn how to isolate variables step by step.",
                        "https://www.youtube.com/watch?v=NybHckSEQBI",
                        GedSubject.MATH, VideoVisibility.PUBLIC);
                saveVideo(videos, urlParser, admin,
                        "Reading Comprehension Strategies",
                        "Active reading techniques for the GED.",
                        "https://www.youtube.com/watch?v=5MgBikgcWnY",
                        GedSubject.LANGUAGE_ARTS, VideoVisibility.PUBLIC);
                saveVideo(videos, urlParser, admin,
                        "Cells and Energy (Biology)",
                        "Mitochondria, photosynthesis, and cellular respiration.",
                        "https://www.youtube.com/watch?v=URUJD5NEXC8",
                        GedSubject.SCIENCE, VideoVisibility.PUBLIC);
                saveVideo(videos, urlParser, admin,
                        "U.S. Constitution Overview",
                        "The branches of government and the Bill of Rights.",
                        "https://www.youtube.com/watch?v=mQ_pL9aYy6Y",
                        GedSubject.SOCIAL_STUDIES, VideoVisibility.PUBLIC);
            }

            if (homework.count() == 0) {
                Homework h1 = new Homework();
                h1.setTitle("Practice: Solve 10 Linear Equations");
                h1.setInstructions("Solve for x and paste the step-by-step work. Example: 3x + 5 = 20 → x = 5.");
                h1.setSubject(GedSubject.MATH);
                h1.setPublished(true);
                h1.setDueDate(LocalDateTime.now().plusDays(7));
                homework.save(h1);

                Homework h2 = new Homework();
                h2.setTitle("Short Essay: My Reading Goals");
                h2.setInstructions("Write a 200-word essay on your reading goals for the next month.");
                h2.setSubject(GedSubject.LANGUAGE_ARTS);
                h2.setPublished(true);
                homework.save(h2);
            }
        };
    }

    private void saveVideo(VideoRepository repo, VideoUrlParser parser, User admin,
                           String title, String desc, String url,
                           GedSubject subject, VideoVisibility visibility) {
        VideoUrlParser.Parsed p = parser.parse(url);
        Video v = new Video();
        v.setTitle(title);
        v.setDescription(desc);
        v.setVideoUrl(url);
        v.setProvider(p.provider());
        v.setEmbedId(p.embedId());
        v.setSubject(subject);
        v.setVisibility(visibility);
        v.setUploadedBy(admin);
        repo.save(v);
    }
}

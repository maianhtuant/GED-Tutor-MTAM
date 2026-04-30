package com.gedtutor.config;

import com.gedtutor.model.*;
import com.gedtutor.repository.HomeworkRepository;
import com.gedtutor.repository.MathProblemTemplateRepository;
import com.gedtutor.repository.SubjectRepository;
import com.gedtutor.repository.UserRepository;
import com.gedtutor.repository.VideoRepository;
import com.gedtutor.service.VideoUrlParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Seeds a default admin user, a demo student, the default subject list, and a few
 * sample videos/homework on first run. Safe to run repeatedly — it only inserts if
 * the row is missing.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository users,
                           VideoRepository videos,
                           HomeworkRepository homework,
                           SubjectRepository subjects,
                           MathProblemTemplateRepository mathTemplates,
                           VideoUrlParser urlParser,
                           PasswordEncoder encoder) {
        return args -> {
            // --- Users ---
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

            // --- Subjects (idempotent, case-insensitive lookup) ---
            Subject math = ensureSubject(subjects, "Math", "Arithmetic, algebra, geometry, and data analysis", 1);
            Subject science = ensureSubject(subjects, "Science", "Life, physical, and earth sciences", 2);
            Subject socialStudies = ensureSubject(subjects, "Social Studies", "Civics, U.S. history, economics, geography", 3);
            Subject languageArts = ensureSubject(subjects, "Language Arts", "Reading, writing, grammar, and composition", 4);
            ensureSubject(subjects, "ESL", "English as a Second Language support lessons", 5);

            // --- Videos ---
            if (videos.count() == 0) {
                saveVideo(videos, urlParser, admin,
                        "Intro to GED Math",
                        "Overview of the GED Math test and key topics.",
                        "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                        math, VideoVisibility.PUBLIC);
                saveVideo(videos, urlParser, admin,
                        "Algebra Basics: Solving Linear Equations",
                        "Learn how to isolate variables step by step.",
                        "https://www.youtube.com/watch?v=NybHckSEQBI",
                        math, VideoVisibility.PUBLIC);
                saveVideo(videos, urlParser, admin,
                        "Reading Comprehension Strategies",
                        "Active reading techniques for the GED.",
                        "https://www.youtube.com/watch?v=5MgBikgcWnY",
                        languageArts, VideoVisibility.PUBLIC);
                saveVideo(videos, urlParser, admin,
                        "Cells and Energy (Biology)",
                        "Mitochondria, photosynthesis, and cellular respiration.",
                        "https://www.youtube.com/watch?v=URUJD5NEXC8",
                        science, VideoVisibility.PUBLIC);
                saveVideo(videos, urlParser, admin,
                        "U.S. Constitution Overview",
                        "The branches of government and the Bill of Rights.",
                        "https://www.youtube.com/watch?v=mQ_pL9aYy6Y",
                        socialStudies, VideoVisibility.PUBLIC);
            }

            // --- Math problem templates (one per kind, idempotent) ---
            // The admin practice-set form lists everything in this table, so a
            // fresh DB needs at least one template per kind to fill the dropdown.
            ensureMathTemplate(mathTemplates, math, MathProblemKind.QUADRATIC,
                    "Quadratic equation", null, 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.LINEAR_EQUATION,
                    "Linear equation", null, 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.SYSTEM_OF_EQUATIONS,
                    "System of equations (2x2)", null, 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.SLOPE,
                    "Slope between two points", null, 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.FUNCTION_EVALUATION,
                    "Function evaluation — linear",  "{\"familyName\":\"LINEAR\"}", 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.FUNCTION_EVALUATION,
                    "Function evaluation — quadratic", "{\"familyName\":\"QUADRATIC\"}", 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.PERCENTAGE,
                    "Percentage problems", null, 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.PROPORTION,
                    "Proportion (a/b = c/x)", null, 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.MEAN_MEDIAN_MODE,
                    "Mean / median / mode", null, 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.PYTHAGOREAN,
                    "Pythagorean theorem (clean triples)", null, 0.5);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.PYTHAGOREAN,
                    "Pythagorean theorem (irrational allowed)",
                    "{\"allowIrrational\":true,\"legMin\":3,\"legMax\":12}", 1.0);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.AREA_PERIMETER,
                    "Area & perimeter", null, 1.0);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.VOLUME,
                    "Volume", null, 1.0);
            ensureMathTemplate(mathTemplates, math, MathProblemKind.SURFACE_AREA,
                    "Surface area", null, 1.0);

            // --- Homework ---
            if (homework.count() == 0) {
                Homework h1 = new Homework();
                h1.setTitle("Practice: Solve 10 Linear Equations");
                h1.setInstructions("Solve for x and paste the step-by-step work. Example: 3x + 5 = 20 → x = 5.");
                h1.setSubject(math);
                h1.setPublished(true);
                h1.setDueDate(LocalDateTime.now().plusDays(7));
                homework.save(h1);

                Homework h2 = new Homework();
                h2.setTitle("Short Essay: My Reading Goals");
                h2.setInstructions("Write a 200-word essay on your reading goals for the next month.");
                h2.setSubject(languageArts);
                h2.setPublished(true);
                homework.save(h2);
            }
        };
    }

    private Subject ensureSubject(SubjectRepository repo, String name, String description, int order) {
        return repo.findByNameIgnoreCase(name).orElseGet(() -> {
            Subject s = new Subject(name);
            s.setDescription(description);
            s.setDisplayOrder(order);
            s.setActive(true);
            return repo.save(s);
        });
    }

    /**
     * Insert a math problem template if no template with the same label
     * already exists. Idempotent — safe to run on every boot.
     */
    private void ensureMathTemplate(MathProblemTemplateRepository repo,
                                    Subject subject,
                                    MathProblemKind kind,
                                    String label,
                                    String parametersJson,
                                    double tolerancePercent) {
        boolean exists = repo.findByActiveTrueOrderByIdAsc().stream()
                .anyMatch(t -> label.equalsIgnoreCase(t.getLabel()));
        if (exists) return;
        MathProblemTemplate t = new MathProblemTemplate();
        t.setSubject(subject);
        t.setKind(kind);
        t.setLabel(label);
        t.setParametersJson(parametersJson);
        t.setTolerancePercent(tolerancePercent);
        t.setActive(true);
        repo.save(t);
    }

    private void saveVideo(VideoRepository repo, VideoUrlParser parser, User admin,
                           String title, String desc, String url,
                           Subject subject, VideoVisibility visibility) {
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

package com.gedtutor.config;

import com.gedtutor.model.*;
import com.gedtutor.repository.HomeworkRepository;
import com.gedtutor.repository.MathProblemTemplateRepository;
import com.gedtutor.repository.QuestionRepository;
import com.gedtutor.repository.SubjectRepository;
import com.gedtutor.repository.UserRepository;
import com.gedtutor.repository.VideoRepository;
import com.gedtutor.service.VideoUrlParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

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
                           QuestionRepository questions,
                           VideoUrlParser urlParser,
                           PasswordEncoder encoder,
                           PlatformTransactionManager txManager) {
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
            Subject math        = ensureSubject(subjects, "Math",          "Arithmetic, algebra, geometry, and data analysis", 1);
            Subject science     = ensureSubject(subjects, "Science",       "Life, physical, and earth sciences", 2);
            Subject socialStudies = ensureSubject(subjects, "Social Studies", "Civics, U.S. history, economics, geography", 3);
            Subject languageArts  = ensureSubject(subjects, "Language Arts",  "Reading, writing, grammar, and composition", 4);
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

            // --- Sample quiz questions (idempotent — only runs when bank is empty) ---
            if (questions.count() == 0) {

                // ── Math questions (10 questions covering linear equations) ──
                Question mq0 = makeFill(questions, math, 0,
                        "Solve for x: 2x + 4 = 10",
                        "3",
                        "Subtract 4 from both sides: 2x = 6, then divide by 2.");
                Question mq1 = makeFill(questions, math, 1,
                        "Solve for x: 3x - 6 = 9",
                        "5",
                        "Add 6: 3x = 15, divide by 3.");
                Question mq2 = makeFill(questions, math, 2,
                        "Solve for x: x + 7 = 15",
                        "8",
                        "Subtract 7 from both sides.");
                Question mq3 = makeFill(questions, math, 3,
                        "Solve for x: 5x = 25",
                        "5",
                        "Divide both sides by 5.");
                Question mq4 = makeFill(questions, math, 4,
                        "Solve for x: 4x - 8 = 0",
                        "2",
                        "Add 8: 4x = 8, divide by 4.");
                Question mq5 = makeFill(questions, math, 5,
                        "Solve for x: x/3 = 4",
                        "12",
                        "Multiply both sides by 3.");
                Question mq6 = makeMC(questions, math, 6,
                        "What is the value of x if 2x + 3 = 11?",
                        "4",
                        List.of("2", "3", "4", "5"),
                        "Subtract 3: 2x = 8, divide by 2.");
                Question mq7 = makeMC(questions, math, 7,
                        "Solve: 3x - 2 = 7",
                        "3",
                        List.of("1", "2", "3", "4"),
                        "Add 2: 3x = 9, divide by 3.");
                Question mq8 = makeTF(questions, math, 8,
                        "The solution to x + 5 = 8 is x = 3.",
                        "true",
                        "8 − 5 = 3 ✓");
                Question mq9 = makeTF(questions, math, 9,
                        "The solution to 2x = 14 is x = 6.",
                        "false",
                        "14 ÷ 2 = 7, not 6.");

                // ── Language Arts questions ──
                Question lq0 = makeTF(questions, languageArts, 0,
                        "A noun names a person, place, thing, or idea.",
                        "true", null);
                Question lq1 = makeMC(questions, languageArts, 1,
                        "Which word is an adjective in: 'The happy dog ran quickly.'",
                        "happy",
                        List.of("dog", "happy", "ran", "quickly"),
                        "'happy' describes the noun 'dog'.");
                Question lq2 = makeFill(questions, languageArts, 2,
                        "What is the past tense of the verb 'run'?",
                        "ran", null);
                Question lq3 = makeTF(questions, languageArts, 3,
                        "A sentence must have a subject and a predicate.",
                        "true", null);
                Question lq4 = makeMC(questions, languageArts, 4,
                        "Which sentence uses correct punctuation?",
                        "She said, \"Hello.\"",
                        List.of("She said, \"Hello.\"", "She said \"Hello\"", "She said; \"Hello.\"", "She said: Hello."),
                        "Dialogue uses a comma before the opening quotation mark.");

                // Linking questions touches homework.getQuestions(), a lazy collection —
                // needs an active Hibernate session (open-in-view is disabled), so run
                // it inside an explicit transaction rather than relying on the runner's
                // (nonexistent) one.
                new TransactionTemplate(txManager).executeWithoutResult(status -> {
                    // ── Link Math questions to h1 ──
                    homework.findAll().stream()
                            .filter(h -> h.getTitle().contains("Linear Equations"))
                            .findFirst()
                            .ifPresent(h -> {
                                h.getQuestions().addAll(List.of(mq0, mq1, mq2, mq3, mq4, mq5, mq6, mq7, mq8, mq9));
                                homework.save(h);
                            });

                    // ── Link Language Arts questions to h2 ──
                    homework.findAll().stream()
                            .filter(h -> h.getTitle().contains("Reading Goals"))
                            .findFirst()
                            .ifPresent(h -> {
                                h.getQuestions().addAll(List.of(lq0, lq1, lq2, lq3, lq4));
                                homework.save(h);
                            });
                });
            }
        };
    }

    // ── Question factory helpers ──────────────────────────────────────────────

    private Question makeFill(QuestionRepository repo, Subject subject, int order,
                               String text, String answer, String explanation) {
        Question q = new Question();
        q.setSubject(subject);
        q.setType(QuestionType.FILL_BLANK);
        q.setQuestionText(text);
        q.setCorrectAnswer(answer);
        q.setExplanation(explanation);
        q.setOrderIndex(order);
        return repo.save(q);
    }

    private Question makeTF(QuestionRepository repo, Subject subject, int order,
                             String text, String answer, String explanation) {
        Question q = new Question();
        q.setSubject(subject);
        q.setType(QuestionType.TRUE_FALSE);
        q.setQuestionText(text);
        q.setCorrectAnswer(answer);
        q.setExplanation(explanation);
        q.setOrderIndex(order);
        return repo.save(q);
    }

    private Question makeMC(QuestionRepository repo, Subject subject, int order,
                             String text, String answer, List<String> choices,
                             String explanation) {
        Question q = new Question();
        q.setSubject(subject);
        q.setType(QuestionType.MULTIPLE_CHOICE);
        q.setQuestionText(text);
        q.setCorrectAnswer(answer);
        q.setExplanation(explanation);
        q.setOrderIndex(order);
        for (int i = 0; i < choices.size(); i++) {
            q.getChoices().add(new QuestionChoice(q, choices.get(i), i));
        }
        return repo.save(q);
    }

    // ── Other helpers ─────────────────────────────────────────────────────────

    private Subject ensureSubject(SubjectRepository repo, String name, String description, int order) {
        return repo.findByNameIgnoreCase(name).orElseGet(() -> {
            Subject s = new Subject(name);
            s.setDescription(description);
            s.setDisplayOrder(order);
            s.setActive(true);
            return repo.save(s);
        });
    }

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

package com.gedtutor.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gedtutor.dto.FreeFormTemplateForm;
import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.QuadraticTemplateForm;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.model.Subject;
import com.gedtutor.repository.MathProblemTemplateRepository;
import com.gedtutor.repository.SubjectRepository;
import com.gedtutor.service.MathProblemService;
import com.gedtutor.service.math.FreeFormGenerator;
import com.gedtutor.service.math.QuadraticSolver;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin CRUD for math-problem templates (FREE_FORM and QUADRATIC kinds).
 */
@Controller
@RequestMapping("/admin/math-templates")
public class MathTemplateAdminController {

    /** Flat view model used in the list page to avoid JSON parsing in Thymeleaf. */
    public record TemplateRow(Long id, String kind, String label, String summary,
                              String subjectName) {}


    private final MathProblemTemplateRepository templateRepo;
    private final SubjectRepository subjectRepo;
    private final MathProblemService mathProblemService;
    private final ObjectMapper objectMapper;

    public MathTemplateAdminController(MathProblemTemplateRepository templateRepo,
                                       SubjectRepository subjectRepo,
                                       MathProblemService mathProblemService,
                                       ObjectMapper objectMapper) {
        this.templateRepo = templateRepo;
        this.subjectRepo = subjectRepo;
        this.mathProblemService = mathProblemService;
        this.objectMapper = objectMapper;
    }

    // ── List ─────────────────────────────────────────────────────────────────

    @GetMapping
    public String list(Model model) {
        List<TemplateRow> rows = templateRepo.findByActiveTrueOrderByIdAsc()
                .stream()
                .filter(t -> t.getKind() == MathProblemKind.FREE_FORM
                          || t.getKind() == MathProblemKind.QUADRATIC)
                .map(t -> {
                    String subjectName = t.getSubject() != null ? t.getSubject().getName() : "—";
                    String summary;
                    if (t.getKind() == MathProblemKind.QUADRATIC) {
                        QuadraticSolver.Config cfg = parseQuadraticConfig(t.getParametersJson());
                        summary = "a:[" + cfg.aMin + "," + cfg.aMax + "]  "
                                + "b:[" + cfg.bMin + "," + cfg.bMax + "]  "
                                + "c:[" + cfg.cMin + "," + cfg.cMax + "]";
                    } else {
                        FreeFormGenerator.Config cfg = parseConfig(t.getParametersJson());
                        summary = cfg.template + "  [" + cfg.minValue + "–" + cfg.maxValue + "]";
                    }
                    return new TemplateRow(t.getId(), t.getKind().name(), t.getLabel(),
                            summary, subjectName);
                })
                .collect(Collectors.toList());
        model.addAttribute("rows", rows);
        return "admin/math-templates";
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new FreeFormTemplateForm());
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("pageTitle", "New Math Template");
        return "admin/math-template-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") FreeFormTemplateForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("subjects", subjectRepo.findAll());
            model.addAttribute("pageTitle", "New Math Template");
            return "admin/math-template-form";
        }
        MathProblemTemplate saved = saveFromForm(new MathProblemTemplate(), form);
        ra.addFlashAttribute("message", "Template \"" + saved.getLabel() + "\" created.");
        return "redirect:/admin/math-templates";
    }

    // ── Edit ──────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        MathProblemTemplate t = findOrThrow(id);
        model.addAttribute("form", toForm(t));
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("pageTitle", "Edit: " + t.getLabel());
        return "admin/math-template-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") FreeFormTemplateForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("subjects", subjectRepo.findAll());
            model.addAttribute("pageTitle", "Edit Template");
            return "admin/math-template-form";
        }
        MathProblemTemplate existing = findOrThrow(id);
        saveFromForm(existing, form);
        ra.addFlashAttribute("message", "Template updated.");
        return "redirect:/admin/math-templates";
    }

    // ── Quadratic Create ─────────────────────────────────────────────────────

    @GetMapping("/new-quadratic")
    public String newQuadraticForm(Model model) {
        model.addAttribute("form", new QuadraticTemplateForm());
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("pageTitle", "New Quadratic Template");
        return "admin/quadratic-template-form";
    }

    @PostMapping("/new-quadratic")
    public String createQuadratic(@Valid @ModelAttribute("form") QuadraticTemplateForm form,
                                  BindingResult binding,
                                  Model model,
                                  RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("subjects", subjectRepo.findAll());
            model.addAttribute("pageTitle", "New Quadratic Template");
            return "admin/quadratic-template-form";
        }
        MathProblemTemplate saved = saveQuadraticFromForm(new MathProblemTemplate(), form);
        ra.addFlashAttribute("message", "Quadratic template \"" + saved.getLabel() + "\" created.");
        return "redirect:/admin/math-templates";
    }

    // ── Quadratic Edit ────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit-quadratic")
    public String editQuadraticForm(@PathVariable Long id, Model model) {
        MathProblemTemplate t = findOrThrow(id);
        model.addAttribute("form", toQuadraticForm(t));
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("pageTitle", "Edit: " + t.getLabel());
        return "admin/quadratic-template-form";
    }

    @PostMapping("/{id}/edit-quadratic")
    public String updateQuadratic(@PathVariable Long id,
                                  @Valid @ModelAttribute("form") QuadraticTemplateForm form,
                                  BindingResult binding,
                                  Model model,
                                  RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("subjects", subjectRepo.findAll());
            model.addAttribute("pageTitle", "Edit Quadratic Template");
            return "admin/quadratic-template-form";
        }
        saveQuadraticFromForm(findOrThrow(id), form);
        ra.addFlashAttribute("message", "Quadratic template updated.");
        return "redirect:/admin/math-templates";
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        MathProblemTemplate t = findOrThrow(id);
        templateRepo.delete(t);
        ra.addFlashAttribute("message", "Template deleted.");
        return "redirect:/admin/math-templates";
    }

    // ── Preview / Generate ────────────────────────────────────────────────────

    @GetMapping("/{id}/preview")
    public String previewGet(@PathVariable Long id, Model model) {
        return addPreviewModel(id, model);
    }

    @PostMapping("/{id}/preview")
    public String previewPost(@PathVariable Long id, Model model) {
        return addPreviewModel(id, model);
    }

    private String addPreviewModel(Long id, Model model) {
        MathProblemTemplate t = findOrThrow(id);
        GeneratedMathProblem generated = mathProblemService.generate(t);
        FreeFormGenerator.Config cfg = parseConfig(t.getParametersJson());

        model.addAttribute("template", t);
        model.addAttribute("generated", generated);
        model.addAttribute("config", cfg);
        return "admin/math-template-preview";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MathProblemTemplate findOrThrow(Long id) {
        return templateRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));
    }

    private MathProblemTemplate saveFromForm(MathProblemTemplate t, FreeFormTemplateForm form) {
        t.setKind(MathProblemKind.FREE_FORM);
        t.setLabel(form.getLabel());
        t.setVideoUrl(form.getVideoUrl());
        t.setActive(form.isActive());

        if (form.getSubjectId() != null) {
            subjectRepo.findById(form.getSubjectId()).ifPresent(t::setSubject);
        } else {
            t.setSubject(null);
        }

        // Serialize generator config into parametersJson
        FreeFormGenerator.Config cfg = new FreeFormGenerator.Config();
        cfg.template      = form.getTemplate();
        cfg.answerFormula = (form.getAnswerFormula() != null && !form.getAnswerFormula().isBlank())
                            ? form.getAnswerFormula().trim() : null;
        cfg.minValue      = form.getMinValue();
        cfg.maxValue      = form.getMaxValue();

        try {
            t.setParametersJson(objectMapper.writeValueAsString(cfg));
        } catch (JsonProcessingException e) {
            t.setParametersJson("{\"template\":\"" + form.getTemplate() + "\","
                    + "\"minValue\":" + form.getMinValue() + ","
                    + "\"maxValue\":" + form.getMaxValue() + "}");
        }
        return templateRepo.save(t);
    }

    /** Populate a form DTO from a saved entity (for the edit page). */
    private FreeFormTemplateForm toForm(MathProblemTemplate t) {
        FreeFormTemplateForm form = new FreeFormTemplateForm();
        form.setId(t.getId());
        form.setLabel(t.getLabel());
        form.setVideoUrl(t.getVideoUrl());
        form.setActive(t.isActive());
        form.setSubjectId(t.getSubject() != null ? t.getSubject().getId() : null);

        FreeFormGenerator.Config cfg = parseConfig(t.getParametersJson());
        form.setTemplate(cfg.template);
        form.setAnswerFormula(cfg.answerFormula);
        form.setMinValue(cfg.minValue);
        form.setMaxValue(cfg.maxValue);
        return form;
    }

    private FreeFormGenerator.Config parseConfig(String json) {
        if (json == null || json.isBlank()) return new FreeFormGenerator.Config();
        try {
            return objectMapper.readValue(json, FreeFormGenerator.Config.class);
        } catch (Exception e) {
            return new FreeFormGenerator.Config();
        }
    }

    private QuadraticSolver.Config parseQuadraticConfig(String json) {
        if (json == null || json.isBlank()) return new QuadraticSolver.Config();
        try {
            return objectMapper.readValue(json, QuadraticSolver.Config.class);
        } catch (Exception e) {
            return new QuadraticSolver.Config();
        }
    }

    private MathProblemTemplate saveQuadraticFromForm(MathProblemTemplate t,
                                                      QuadraticTemplateForm form) {
        t.setKind(MathProblemKind.QUADRATIC);
        t.setLabel(form.getLabel());
        t.setVideoUrl(form.getVideoUrl());
        t.setActive(form.isActive());
        if (form.getSubjectId() != null) {
            subjectRepo.findById(form.getSubjectId()).ifPresent(t::setSubject);
        } else {
            t.setSubject(null);
        }
        QuadraticSolver.Config cfg = new QuadraticSolver.Config();
        cfg.aMin = form.getAMin();  cfg.aMax = form.getAMax();
        cfg.bMin = form.getBMin();  cfg.bMax = form.getBMax();
        cfg.cMin = form.getCMin();  cfg.cMax = form.getCMax();
        try {
            t.setParametersJson(objectMapper.writeValueAsString(cfg));
        } catch (JsonProcessingException e) {
            t.setParametersJson("{}");
        }
        return templateRepo.save(t);
    }

    private QuadraticTemplateForm toQuadraticForm(MathProblemTemplate t) {
        QuadraticTemplateForm form = new QuadraticTemplateForm();
        form.setId(t.getId());
        form.setLabel(t.getLabel());
        form.setVideoUrl(t.getVideoUrl());
        form.setActive(t.isActive());
        form.setSubjectId(t.getSubject() != null ? t.getSubject().getId() : null);
        QuadraticSolver.Config cfg = parseQuadraticConfig(t.getParametersJson());
        form.setAMin(cfg.aMin);  form.setAMax(cfg.aMax);
        form.setBMin(cfg.bMin);  form.setBMax(cfg.bMax);
        form.setCMin(cfg.cMin);  form.setCMax(cfg.cMax);
        return form;
    }
}

package com.gedtutor.controller;

import com.gedtutor.dto.PracticeSetForm;
import com.gedtutor.model.PracticeSet;
import com.gedtutor.model.Subject;
import com.gedtutor.repository.MathProblemTemplateRepository;
import com.gedtutor.repository.SubjectRepository;
import com.gedtutor.service.PracticeSetService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/practice-sets")
public class PracticeSetAdminController {

    private final PracticeSetService service;
    private final MathProblemTemplateRepository templateRepo;
    private final SubjectRepository subjectRepo;

    public PracticeSetAdminController(PracticeSetService service,
                                      MathProblemTemplateRepository templateRepo,
                                      SubjectRepository subjectRepo) {
        this.service = service;
        this.templateRepo = templateRepo;
        this.subjectRepo = subjectRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("sets", service.listAll());
        return "admin/practice-sets";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PracticeSetForm());
        }
        // No `set` model attribute on the create flow; the template handles that.
        addCommonFormAttributes(model, null);
        return "admin/practice-set-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") PracticeSetForm form,
                         BindingResult binding,
                         @RequestParam(value = "starterTemplateId", required = false) java.util.List<Long> starterTemplateIds,
                         @RequestParam(value = "starterQuestionCount", required = false) java.util.List<Integer> starterCounts,
                         Model model,
                         RedirectAttributes ra) {
        if (binding.hasErrors()) {
            addCommonFormAttributes(model, null);
            return "admin/practice-set-form";
        }
        PracticeSet set = new PracticeSet();
        applyForm(form, set);
        PracticeSet saved = service.save(set);

        // Optional starter items: parallel arrays from the inline form rows.
        if (starterTemplateIds != null && starterCounts != null) {
            int n = Math.min(starterTemplateIds.size(), starterCounts.size());
            for (int i = 0; i < n; i++) {
                Long tid = starterTemplateIds.get(i);
                Integer count = starterCounts.get(i);
                if (tid == null || count == null || count <= 0) continue;
                service.addItem(saved.getId(), tid, count);
            }
        }
        ra.addFlashAttribute("message", "Practice set created.");
        return "redirect:/admin/practice-sets/" + saved.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PracticeSet set = service.findById(id);
        model.addAttribute("form", toForm(set));
        addCommonFormAttributes(model, set);
        return "admin/practice-set-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") PracticeSetForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra) {
        PracticeSet set = service.findById(id);
        if (binding.hasErrors()) {
            addCommonFormAttributes(model, set);
            return "admin/practice-set-form";
        }
        applyForm(form, set);
        service.save(set);
        ra.addFlashAttribute("message", "Practice set updated.");
        return "redirect:/admin/practice-sets/" + id + "/edit";
    }

    /**
     * One-click "Math quiz with N random questions across all kinds".
     * Creates a Practice Set with items that distribute the requested
     * total evenly across every active math template.
     */
    @PostMapping("/quick-mix")
    public String createQuickMix(@RequestParam(defaultValue = "40") int totalQuestions,
                                 @RequestParam(required = false) String title,
                                 RedirectAttributes ra) {
        var set = service.createQuickMix(title, null, Math.max(1, totalQuestions), null);
        ra.addFlashAttribute("message",
                "Created math quiz with " + totalQuestions
                        + " questions across " + set.getItems().size() + " templates.");
        return "redirect:/admin/practice-sets/" + set.getId() + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("message", "Practice set deleted.");
        return "redirect:/admin/practice-sets";
    }

    // ----- items -----

    @PostMapping("/{id}/items")
    public String addItem(@PathVariable Long id,
                          @RequestParam Long templateId,
                          @RequestParam int questionCount,
                          RedirectAttributes ra) {
        service.addItem(id, templateId, questionCount);
        ra.addFlashAttribute("message", "Item added.");
        return "redirect:/admin/practice-sets/" + id + "/edit";
    }

    @PostMapping("/{id}/items/{itemId}/delete")
    public String removeItem(@PathVariable Long id, @PathVariable Long itemId, RedirectAttributes ra) {
        service.removeItem(itemId);
        ra.addFlashAttribute("message", "Item removed.");
        return "redirect:/admin/practice-sets/" + id + "/edit";
    }

    @PostMapping("/{id}/items/{itemId}/move")
    public String moveItem(@PathVariable Long id,
                           @PathVariable Long itemId,
                           @RequestParam int delta) {
        service.moveItem(itemId, delta);
        return "redirect:/admin/practice-sets/" + id + "/edit";
    }

    // ----- helpers -----

    private void addCommonFormAttributes(Model model, PracticeSet set) {
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("templates", templateRepo.findByActiveTrueOrderByIdAsc());
        // The template needs the `set` (with items) only on the edit flow. On create, `set` is absent.
        if (set != null) {
            model.addAttribute("set", set);
        }
    }

    private static PracticeSetForm toForm(PracticeSet set) {
        PracticeSetForm form = new PracticeSetForm();
        form.setId(set.getId());
        form.setTitle(set.getTitle());
        form.setDescription(set.getDescription());
        form.setSubjectId(set.getSubject() != null ? set.getSubject().getId() : null);
        form.setActive(set.isActive());
        return form;
    }

    private void applyForm(PracticeSetForm form, PracticeSet set) {
        set.setTitle(form.getTitle());
        set.setDescription(form.getDescription());
        set.setActive(form.isActive());
        if (form.getSubjectId() == null) {
            set.setSubject(null);
        } else {
            Subject s = subjectRepo.findById(form.getSubjectId()).orElseThrow(
                    () -> new EntityNotFoundException("Subject not found: " + form.getSubjectId()));
            set.setSubject(s);
        }
    }
}

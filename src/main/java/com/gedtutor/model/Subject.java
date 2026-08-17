package com.gedtutor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Admin-managed subject / category (e.g. Math, Science, ESL).
 * Videos and Homework reference a Subject via foreign key so that
 * admins can add, rename, or retire subjects at runtime.
 *
 * <p>Subjects support one level of nesting via the {@code parent} relation.
 * Root subjects (parent == null) appear in the top tab bar; child subjects
 * appear as a sub-tab row when their parent tab is selected.
 */
@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name (e.g. "Math", "ESL"). Must be unique. */
    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60, unique = true)
    private String name;

    /** Optional short description shown on the admin page. */
    @Size(max = 500)
    @Column(length = 500)
    private String description;

    /** Lower numbers show first in dropdowns / lists. */
    @Column(nullable = false)
    private int displayOrder = 0;

    /** Inactive subjects are hidden from new content dropdowns but still show for existing content. */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Optional parent subject — enables one level of subject grouping.
     * Root subjects (parent == null) appear as top-level tabs.
     * Child subjects appear as sub-tabs under their parent.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Subject parent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Subject() {}

    public Subject(String name) {
        this.name = name;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Subject getParent() { return parent; }
    public void setParent(Subject parent) { this.parent = parent; }

    public boolean isChild() { return parent != null; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name;
    }
}

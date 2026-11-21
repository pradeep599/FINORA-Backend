package com.finpulse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "author_name", nullable = false, length = 120)
    private String authorName;

    @Column(name = "author_role", length = 80)
    private String authorRole = "Finora Member";

    @Column(nullable = false)
    private Integer rating; // 1 to 5

    @Column(nullable = false, length = 50)
    private String category; // UI/UX, AI Insights, Budgeting, Overall

    @Column(nullable = false, length = 600)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Feedback() {}

    public Feedback(User user, String authorName, String authorRole, Integer rating, String category, String message) {
        this.user = user;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.rating = rating;
        this.category = category;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

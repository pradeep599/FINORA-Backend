package com.finpulse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(nullable = false, length = 20)
    private String type;
    @Column(nullable = false, length = 20)
    private String color = "#4F46E5";
    @Column(nullable = false, length = 50)
    private String icon = "bi-tag";

    public Category() {}
    public Category(User user, String name, String type, String color, String icon) {
        this.user = user; this.name = name; this.type = type; this.color = color; this.icon = icon;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}

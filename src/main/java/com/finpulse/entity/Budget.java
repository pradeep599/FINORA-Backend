package com.finpulse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_budget_user_cat_month_year", columnNames = {"user_id", "category_id", "budget_month", "budget_year"})
})
public class Budget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @Column(name = "monthly_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimit;
    @Column(name = "budget_month", nullable = false)
    private Integer month;
    @Column(name = "budget_year", nullable = false)
    private Integer year;

    public Budget() {}
    public Budget(User user, Category category, BigDecimal monthlyLimit, Integer month, Integer year) {
        this.user = user; this.category = category; this.monthlyLimit = monthlyLimit; this.month = month; this.year = year;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}

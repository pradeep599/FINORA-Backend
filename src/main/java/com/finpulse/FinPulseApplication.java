package com.finpulse;

import com.finpulse.entity.*;
import com.finpulse.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class FinPulseApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinPulseApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(UserRepository userRepo, CategoryRepository catRepo,
                               AccountRepository accRepo, TransactionRepository txRepo,
                               BudgetRepository budgetRepo, FeedbackRepository feedbackRepo,
                               PasswordEncoder encoder) {
        return args -> {
            if (catRepo.count() == 0) {
                catRepo.saveAll(List.of(
                    new Category(null, "Food & Dining", "EXPENSE", "#F59E0B", "bi-cup-hot"),
                    new Category(null, "Groceries", "EXPENSE", "#10B981", "bi-cart3"),
                    new Category(null, "Housing & Rent", "EXPENSE", "#4F46E5", "bi-house-door"),
                    new Category(null, "Transport", "EXPENSE", "#06B6D4", "bi-car-front"),
                    new Category(null, "Shopping", "EXPENSE", "#EC4899", "bi-bag"),
                    new Category(null, "Utilities & Bills", "EXPENSE", "#6366F1", "bi-lightning"),
                    new Category(null, "Entertainment", "EXPENSE", "#8B5CF6", "bi-film"),
                    new Category(null, "Health & Fitness", "EXPENSE", "#EF4444", "bi-heart-pulse"),
                    new Category(null, "Salary", "INCOME", "#10B981", "bi-briefcase"),
                    new Category(null, "Freelance", "INCOME", "#0EA5E9", "bi-laptop"),
                    new Category(null, "Investments", "INCOME", "#14B8A6", "bi-graph-up-arrow")
                ));
            }
            if (!userRepo.existsByEmail("demo@finpulse.ai")) {
                User demo = userRepo.save(new User("Aarav Sharma", "demo@finpulse.ai", encoder.encode("Demo@123"), "INR"));
                Account hdfc = accRepo.save(new Account(demo, "HDFC Salary Account", "BANK", new BigDecimal("148500")));
                Account icici = accRepo.save(new Account(demo, "ICICI Coral Credit", "CREDIT", new BigDecimal("22400")));
                Account upi = accRepo.save(new Account(demo, "UPI Lite Wallet", "WALLET", new BigDecimal("6800")));

                Category salary = catRepo.findFirstByNameIgnoreCase("Salary").orElseThrow();
                Category freelance = catRepo.findFirstByNameIgnoreCase("Freelance").orElseThrow();
                Category food = catRepo.findFirstByNameIgnoreCase("Food & Dining").orElseThrow();
                Category groceries = catRepo.findFirstByNameIgnoreCase("Groceries").orElseThrow();
                Category rent = catRepo.findFirstByNameIgnoreCase("Housing & Rent").orElseThrow();
                Category transport = catRepo.findFirstByNameIgnoreCase("Transport").orElseThrow();
                Category shopping = catRepo.findFirstByNameIgnoreCase("Shopping").orElseThrow();
                Category utilities = catRepo.findFirstByNameIgnoreCase("Utilities & Bills").orElseThrow();
                Category entertainment = catRepo.findFirstByNameIgnoreCase("Entertainment").orElseThrow();

                LocalDate today = LocalDate.now();
                saveTx(txRepo, demo, hdfc, salary, "95000", "INCOME", "Monthly Tech Lead Salary", today.withDayOfMonth(1), false);
                saveTx(txRepo, demo, hdfc, freelance, "18000", "INCOME", "UI Architecture Consulting", today.withDayOfMonth(5), false);
                saveTx(txRepo, demo, hdfc, rent, "24000", "EXPENSE", "Apartment Monthly Rent", today.withDayOfMonth(2), false);
                saveTx(txRepo, demo, hdfc, utilities, "3450", "EXPENSE", "Electricity & Jio Fiber Broadband", today.withDayOfMonth(4), true);
                saveTx(txRepo, demo, icici, food, "1480", "EXPENSE", "Swiggy Weekend Dinner", today.minusDays(2), true);
                saveTx(txRepo, demo, upi, food, "420", "EXPENSE", "Starbucks Cold Brew Coffee", today.minusDays(1), true);
                saveTx(txRepo, demo, icici, food, "2650", "EXPENSE", "Team Dinner at Indiranagar Cafe", today.minusDays(5), true);
                saveTx(txRepo, demo, upi, groceries, "3850", "EXPENSE", "BigBasket Weekly Fresh Groceries", today.minusDays(3), true);
                saveTx(txRepo, demo, upi, transport, "890", "EXPENSE", "Uber Airport & Metro Rides", today.minusDays(4), true);
                saveTx(txRepo, demo, icici, shopping, "6499", "EXPENSE", "Amazon Mechanical Keyboard", today.minusDays(6), true);
                saveTx(txRepo, demo, icici, entertainment, "1199", "EXPENSE", "Netflix & Spotify Subscriptions", today.minusDays(7), true);

                LocalDate prev = today.minusMonths(1);
                saveTx(txRepo, demo, hdfc, salary, "95000", "INCOME", "Previous Month Salary", prev.withDayOfMonth(1), false);
                saveTx(txRepo, demo, hdfc, rent, "24000", "EXPENSE", "Apartment Rent", prev.withDayOfMonth(2), false);
                saveTx(txRepo, demo, icici, food, "8200", "EXPENSE", "Dining & Food Delivery", prev.withDayOfMonth(12), true);
                saveTx(txRepo, demo, icici, shopping, "11500", "EXPENSE", "Festive Shopping", prev.withDayOfMonth(18), true);

                int m = today.getMonthValue(), y = today.getYear();
                budgetRepo.saveAll(List.of(
                    new Budget(demo, food, new BigDecimal("6000"), m, y),
                    new Budget(demo, groceries, new BigDecimal("8000"), m, y),
                    new Budget(demo, shopping, new BigDecimal("7000"), m, y),
                    new Budget(demo, transport, new BigDecimal("3500"), m, y),
                    new Budget(demo, utilities, new BigDecimal("5000"), m, y)
                ));
            }
            if (feedbackRepo.count() == 0) {
                feedbackRepo.saveAll(List.of(
                    new Feedback(null, "Ananya Verma", "Senior Product Designer", 5, "UI/UX",
                        "Finally a finance app that feels calm instead of stressful! The AI auto-categorizer saves me 20 minutes every week."),
                    new Feedback(null, "Rohan Kulkarni", "Software Engineer", 5, "AI Insights",
                        "The Weekend Spending Spike alert caught how much I was losing on late-night deliveries. Saved ₹5,200 in my first month."),
                    new Feedback(null, "Meera Nair", "Freelance Consultant", 4, "Budgeting",
                        "One-click AI recommended budgets plus instant CSV/PDF reports made my quarterly tax prep effortless.")
                ));
            }
        };
    }

    private void saveTx(TransactionRepository repo, User u, Account a, Category c, String amt,
                        String type, String desc, LocalDate date, boolean ai) {
        Transaction t = new Transaction();
        t.setUser(u); t.setAccount(a); t.setCategory(c);
        t.setAmount(new BigDecimal(amt)); t.setType(type);
        t.setDescription(desc); t.setTransactionDate(date); t.setAiCategorized(ai);
        repo.save(t);
    }
}

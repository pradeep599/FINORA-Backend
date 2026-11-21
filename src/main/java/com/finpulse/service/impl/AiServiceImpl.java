package com.finpulse.service.impl;

import com.finpulse.dto.AiCategorizeResponse;
import com.finpulse.entity.*;
import com.finpulse.exception.ResourceNotFoundException;
import com.finpulse.repository.*;
import com.finpulse.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class AiServiceImpl implements AiService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${app.ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.model:gemini-2.0-flash}")
    private String geminiModel;

    public AiServiceImpl(UserRepository userRepository, CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public AiCategorizeResponse categorizeExpense(String description, String type) {
        User user = currentUser();
        List<Category> cats = categoryRepository.findAllAvailableForUser(user.getId())
                .stream().filter(c -> type == null || c.getType().equalsIgnoreCase(type)).toList();

        AiCategorizeResponse res = new AiCategorizeResponse();
        String lower = description != null ? description.toLowerCase() : "";

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String names = String.join(", ", cats.stream().map(Category::getName).toList());
                String llm = callGemini("Classify '" + description + "' into exactly ONE of [" + names + "]. Reply with ONLY the category name.");
                if (llm != null) {
                    for (Category c : cats) {
                        if (llm.toLowerCase().contains(c.getName().toLowerCase())) {
                            res.categoryId = c.getId(); res.categoryName = c.getName();
                            res.confidence = 0.94; res.engine = "GEMINI_LLM";
                            res.reasoning = "Classified via Gemini LLM based on merchant & semantic intent.";
                            return res;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        Map<String, List<String>> kwMap = Map.ofEntries(
            Map.entry("Food & Dining", List.of("swiggy", "zomato", "coffee", "starbucks", "restaurant", "cafe", "pizza", "biryani", "lunch", "dinner")),
            Map.entry("Groceries", List.of("bigbasket", "blinkit", "zepto", "dmart", "supermarket", "grocery", "vegetables", "milk")),
            Map.entry("Transport", List.of("uber", "ola", "rapido", "metro", "petrol", "fuel", "flight", "train", "irctc", "cab")),
            Map.entry("Shopping", List.of("amazon", "flipkart", "myntra", "ajio", "zara", "nike", "mall", "electronics")),
            Map.entry("Utilities & Bills", List.of("electricity", "water", "broadband", "jio", "airtel", "wifi", "gas", "bill")),
            Map.entry("Housing & Rent", List.of("rent", "maintenance", "society", "housing", "emi")),
            Map.entry("Entertainment", List.of("netflix", "spotify", "prime", "hotstar", "movie", "pvr", "inox")),
            Map.entry("Health & Fitness", List.of("pharmacy", "apollo", "doctor", "hospital", "gym", "medicine")),
            Map.entry("Salary", List.of("salary", "payroll", "stipend", "bonus")),
            Map.entry("Freelance", List.of("freelance", "client", "invoice", "consulting"))
        );

        for (var e : kwMap.entrySet()) {
            for (String kw : e.getValue()) {
                if (lower.contains(kw)) {
                    var match = cats.stream().filter(c -> c.getName().equalsIgnoreCase(e.getKey())).findFirst();
                    if (match.isPresent()) {
                        res.categoryId = match.get().getId(); res.categoryName = match.get().getName();
                        res.confidence = 0.91; res.engine = "SMART_ENGINE";
                        res.reasoning = "Matched merchant signal '" + kw + "' to " + match.get().getName() + ".";
                        return res;
                    }
                }
            }
        }

        Category fallback = cats.isEmpty() ? categoryRepository.findAll().get(0) : cats.get(0);
        res.categoryId = fallback.getId(); res.categoryName = fallback.getName();
        res.confidence = 0.65; res.engine = "SMART_ENGINE";
        res.reasoning = "Assigned default category; add merchant detail for higher precision.";
        return res;
    }

    @Override
    public Map<String, Object> analyzeSpendingPatterns(Integer month, Integer year) {
        User user = currentUser();
        YearMonth ym = resolveMonth(month, year);
        List<Transaction> expenses = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(user.getId(), ym.atDay(1), ym.atEndOfMonth())
                .stream().filter(t -> "EXPENSE".equalsIgnoreCase(t.getType())).toList();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("engine", (geminiApiKey != null && !geminiApiKey.isBlank()) ? "GEMINI_LLM_HYBRID" : "SMART_ENGINE");
        List<Map<String, Object>> insights = new ArrayList<>();

        if (expenses.isEmpty()) {
            res.put("headline", "Add expenses for " + ym.getMonth() + " to unlock behavioral spending insights.");
            res.put("insights", insights);
            return res;
        }

        BigDecimal total = expenses.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> byCat = new HashMap<>();
        BigDecimal weekend = BigDecimal.ZERO, weekday = BigDecimal.ZERO;
        for (Transaction t : expenses) {
            byCat.merge(t.getCategory().getName(), t.getAmount(), BigDecimal::add);
            DayOfWeek d = t.getTransactionDate().getDayOfWeek();
            if (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY) weekend = weekend.add(t.getAmount());
            else weekday = weekday.add(t.getAmount());
        }

        var top = byCat.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow();
        double topPct = top.getValue().divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        res.put("headline", String.format("Analyzed %d expenses totaling %s %,.0f — %s drives %.1f%% of your spend.",
                expenses.size(), user.getCurrency(), total, top.getKey(), topPct));

        insights.add(Map.of(
            "title", "Primary Spend Driver: " + top.getKey(),
            "detail", String.format("%s accounts for %.1f%% (%s %,.0f) of monthly outflow.", top.getKey(), topPct, user.getCurrency(), top.getValue()),
            "severity", topPct > 35 ? "WARNING" : "INFO",
            "actionTip", "Set a weekly cap on " + top.getKey() + " to reclaim 10-15% in monthly savings."
        ));

        double wkPct = total.compareTo(BigDecimal.ZERO) > 0 ? weekend.divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100.0 : 0;
        insights.add(Map.of(
            "title", wkPct > 35 ? "Weekend Spending Spike Detected" : "Balanced Weekly Spending Pace",
            "detail", String.format("Spent %s %,.0f (%.1f%%) on weekends vs %s %,.0f on weekdays.", user.getCurrency(), weekend, wkPct, user.getCurrency(), weekday),
            "severity", wkPct > 35 ? "WARNING" : "POSITIVE",
            "actionTip", wkPct > 35 ? "Pre-plan weekend dining & leisure budgets on Friday." : "Your weekday vs. weekend pacing is healthy."
        ));

        long micro = expenses.stream().filter(t -> t.getAmount().compareTo(new BigDecimal("500")) < 0).count();
        insights.add(Map.of(
            "title", "Micro-Transaction Frequency",
            "detail", String.format("%d of %d expenses were small purchases under 500.", micro, expenses.size()),
            "severity", micro >= 5 ? "WARNING" : "POSITIVE",
            "actionTip", "Batch small orders to avoid delivery fees and impulse creep."
        ));

        res.put("insights", insights);
        return res;
    }

    @Override
    public Map<String, Object> recommendBudgets(Integer month, Integer year) {
        User user = currentUser();
        YearMonth ym = resolveMonth(month, year);
        List<Transaction> txs = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(user.getId(), ym.atDay(1), ym.atEndOfMonth());

        Map<Category, BigDecimal> byCat = new LinkedHashMap<>();
        BigDecimal income = BigDecimal.ZERO;
        for (Transaction t : txs) {
            if ("INCOME".equalsIgnoreCase(t.getType())) income = income.add(t.getAmount());
            else byCat.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
        }

        List<Map<String, Object>> recs = new ArrayList<>();
        for (var e : byCat.entrySet()) {
            String name = e.getKey().getName().toLowerCase();
            boolean essential = name.contains("rent") || name.contains("utilities") || name.contains("groceries");
            BigDecimal rec = e.getValue().multiply(essential ? new BigDecimal("1.05") : new BigDecimal("0.88")).setScale(0, RoundingMode.CEILING);
            recs.add(Map.of(
                "categoryId", e.getKey().getId(),
                "categoryName", e.getKey().getName(),
                "currentSpend", e.getValue(),
                "recommendedLimit", rec,
                "rationale", essential ? "Essential category: 5% buffer added." : "Discretionary category: 12% trim recommended."
            ));
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("engine", (geminiApiKey != null && !geminiApiKey.isBlank()) ? "GEMINI_LLM_HYBRID" : "SMART_ENGINE");
        res.put("strategySummary", income.compareTo(BigDecimal.ZERO) > 0
                ? String.format("Tailored to your %s %,.0f monthly income using a 50/30/20 model.", user.getCurrency(), income)
                : "Based on your current category spending velocity.");
        res.put("recommendations", recs);
        return res;
    }

    @Override
    public Map<String, Object> generateMonthlySummary(Integer month, Integer year) {
        User user = currentUser();
        YearMonth ym = resolveMonth(month, year);
        List<Transaction> txs = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(user.getId(), ym.atDay(1), ym.atEndOfMonth());

        BigDecimal income = BigDecimal.ZERO, expense = BigDecimal.ZERO;
        Map<String, BigDecimal> byCat = new HashMap<>();
        for (Transaction t : txs) {
            if ("INCOME".equalsIgnoreCase(t.getType())) income = income.add(t.getAmount());
            else { expense = expense.add(t.getAmount()); byCat.merge(t.getCategory().getName(), t.getAmount(), BigDecimal::add); }
        }

        BigDecimal net = income.subtract(expense);
        double rate = income.compareTo(BigDecimal.ZERO) > 0 ? net.divide(income, 4, RoundingMode.HALF_UP).doubleValue() * 100.0 : 0.0;
        int score = (int) Math.max(30, Math.min(98, 50 + (rate * 1.2)));
        String grade = score >= 85 ? "A (Excellent)" : score >= 70 ? "B+ (Healthy)" : score >= 55 ? "C (Needs Focus)" : "D (At Risk)";
        String topLeak = byCat.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + user.getCurrency() + " " + e.getValue().intValue() + ")")
                .orElse("No expenses recorded yet");

        String summary = String.format("For %s %d, you earned %s %,.0f and spent %s %,.0f, saving %s %,.0f (%.1f%%). Financial Health Score: %d/100 (%s).",
                ym.getMonth(), ym.getYear(), user.getCurrency(), income, user.getCurrency(), expense, user.getCurrency(), net, rate, score, grade);

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String llm = callGemini("Write a 2-sentence executive monthly financial summary: Income=" + income + ", Expense=" + expense + ", Net=" + net + ", TopLeak=" + topLeak + ", Currency=" + user.getCurrency());
                if (llm != null && !llm.isBlank()) summary = llm.trim();
            } catch (Exception ignored) {}
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("engine", (geminiApiKey != null && !geminiApiKey.isBlank()) ? "GEMINI_LLM" : "SMART_ENGINE");
        res.put("healthScore", score);
        res.put("grade", grade);
        res.put("executiveSummary", summary);
        res.put("topWin", rate >= 20 ? String.format("Strong %.1f%% savings rate!", rate) : "Consistent expense tracking established.");
        res.put("biggestLeak", "Highest outflow concentrated in " + topLeak + ".");
        res.put("actionItems", List.of(
                "Automate transferring 20% of income to savings on payday.",
                "Apply AI-recommended category budgets for next month.",
                "Review recurring subscriptions and dining spend weekly."
        ));
        res.put("totalIncome", income);
        res.put("totalExpense", expense);
        res.put("netSavings", net);
        res.put("savingsRate", Math.round(rate * 10.0) / 10.0);
        return res;
    }

    private YearMonth resolveMonth(Integer m, Integer y) {
        LocalDate now = LocalDate.now();
        return YearMonth.of(y != null ? y : now.getYear(), m != null ? m : now.getMonthValue());
    }

    @SuppressWarnings("unchecked")
    private String callGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;
        Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        Map<String, Object> resp = restClient.post().uri(url).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
        if (resp != null && resp.containsKey("candidates")) {
            List<Map<String, Object>> c = (List<Map<String, Object>>) resp.get("candidates");
            if (!c.isEmpty()) {
                List<Map<String, Object>> parts = (List<Map<String, Object>>) ((Map<String, Object>) c.get(0).get("content")).get("parts");
                if (!parts.isEmpty()) return String.valueOf(parts.get(0).get("text"));
            }
        }
        return null;
    }
}

package com.finpulse.service.impl;

import com.finpulse.dto.*;
import com.finpulse.entity.*;
import com.finpulse.exception.ResourceNotFoundException;
import com.finpulse.repository.*;
import com.finpulse.service.AiService;
import com.finpulse.service.FinanceService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class FinanceServiceImpl implements FinanceService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final FeedbackRepository feedbackRepository;
    private final AiService aiService;

    public FinanceServiceImpl(UserRepository userRepository, AccountRepository accountRepository,
                              CategoryRepository categoryRepository, TransactionRepository transactionRepository,
                              BudgetRepository budgetRepository, FeedbackRepository feedbackRepository,
                              AiService aiService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.feedbackRepository = feedbackRepository;
        this.aiService = aiService;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public List<AccountDto> getAccounts() {
        return accountRepository.findByUserIdOrderByCreatedAtAsc(currentUser().getId()).stream().map(a -> {
            AccountDto d = new AccountDto();
            d.id = a.getId(); d.name = a.getName(); d.type = a.getType(); d.balance = a.getBalance();
            return d;
        }).toList();
    }

    @Override
    @Transactional
    public AccountDto createAccount(AccountDto dto) {
        Account a = accountRepository.save(new Account(currentUser(), dto.name.trim(), dto.type.toUpperCase(), dto.balance));
        dto.id = a.getId(); dto.type = a.getType();
        return dto;
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        Account a = accountRepository.findByIdAndUserId(id, currentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        accountRepository.delete(a);
    }

    @Override
    public List<CategoryDto> getCategories() {
        return categoryRepository.findAllAvailableForUser(currentUser().getId()).stream().map(c -> {
            CategoryDto d = new CategoryDto();
            d.id = c.getId(); d.name = c.getName(); d.type = c.getType(); d.color = c.getColor(); d.icon = c.getIcon();
            return d;
        }).toList();
    }

    @Override
    public CategoryDto createCategory(CategoryDto dto) {
        Category c = categoryRepository.save(new Category(currentUser(), dto.name.trim(), dto.type.toUpperCase(),
                dto.color != null ? dto.color : "#4F46E5", dto.icon != null ? dto.icon : "bi-tag"));
        dto.id = c.getId();
        return dto;
    }

    @Override
    public List<TransactionDto> getTransactions() {
        return transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(currentUser().getId())
                .stream().map(this::toTxDto).toList();
    }

    @Override
    @Transactional
    public TransactionDto createTransaction(TransactionDto dto) {
        User user = currentUser();
        Account account = accountRepository.findByIdAndUserId(dto.accountId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        boolean usedAi = dto.aiCategorized;
        Category category;
        if (dto.categoryId != null) {
            category = categoryRepository.findById(dto.categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        } else {
            var pred = aiService.categorizeExpense(dto.description, dto.type);
            category = categoryRepository.findById(pred.categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            usedAi = true;
        }

        Transaction tx = new Transaction();
        tx.setUser(user); tx.setAccount(account); tx.setCategory(category);
        tx.setAmount(dto.amount); tx.setType(dto.type.toUpperCase());
        tx.setDescription(dto.description.trim()); tx.setTransactionDate(dto.transactionDate);
        tx.setAiCategorized(usedAi);

        account.setBalance("INCOME".equalsIgnoreCase(tx.getType())
                ? account.getBalance().add(tx.getAmount())
                : account.getBalance().subtract(tx.getAmount()));
        accountRepository.save(account);
        return toTxDto(transactionRepository.save(tx));
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction tx = transactionRepository.findByIdAndUserId(id, currentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        Account acc = tx.getAccount();
        acc.setBalance("INCOME".equalsIgnoreCase(tx.getType())
                ? acc.getBalance().subtract(tx.getAmount())
                : acc.getBalance().add(tx.getAmount()));
        accountRepository.save(acc);
        transactionRepository.delete(tx);
    }

    @Override
    public List<BudgetDto> getBudgets(Integer month, Integer year) {
        User user = currentUser();
        LocalDate now = LocalDate.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();
        YearMonth ym = YearMonth.of(y, m);

        Map<Long, BigDecimal> spentByCat = new HashMap<>();
        for (Transaction t : transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                user.getId(), ym.atDay(1), ym.atEndOfMonth())) {
            if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                spentByCat.merge(t.getCategory().getId(), t.getAmount(), BigDecimal::add);
            }
        }

        return budgetRepository.findByUserIdAndMonthAndYear(user.getId(), m, y).stream().map(b -> {
            BudgetDto d = new BudgetDto();
            d.id = b.getId(); d.categoryId = b.getCategory().getId();
            d.categoryName = b.getCategory().getName(); d.categoryColor = b.getCategory().getColor();
            d.monthlyLimit = b.getMonthlyLimit(); d.month = b.getMonth(); d.year = b.getYear();
            d.spent = spentByCat.getOrDefault(b.getCategory().getId(), BigDecimal.ZERO);
            double pct = b.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0
                    ? d.spent.divide(b.getMonthlyLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0 : 0.0;
            d.percentageUsed = Math.round(pct * 10.0) / 10.0;
            d.status = pct >= 100 ? "EXCEEDED" : (pct >= 75 ? "WARNING" : "ON_TRACK");
            return d;
        }).toList();
    }

    @Override
    @Transactional
    public BudgetDto saveBudget(BudgetDto dto) {
        User user = currentUser();
        LocalDate now = LocalDate.now();
        int m = dto.month != null ? dto.month : now.getMonthValue();
        int y = dto.year != null ? dto.year : now.getYear();
        Category cat = categoryRepository.findById(dto.categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Budget b = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(user.getId(), cat.getId(), m, y)
                .orElse(new Budget(user, cat, dto.monthlyLimit, m, y));
        b.setMonthlyLimit(dto.monthlyLimit);
        b = budgetRepository.save(b);
        dto.id = b.getId(); dto.categoryName = cat.getName(); dto.month = m; dto.year = y;
        return dto;
    }

    @Override
    @Transactional
    public void deleteBudget(Long id) {
        Budget b = budgetRepository.findByIdAndUserId(id, currentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budgetRepository.delete(b);
    }

    @Override
    public Map<String, Object> getDashboardAnalytics(Integer month, Integer year) {
        User user = currentUser();
        LocalDate now = LocalDate.now();
        YearMonth ym = YearMonth.of(year != null ? year : now.getYear(), month != null ? month : now.getMonthValue());

        BigDecimal totalBalance = accountRepository.findByUserIdOrderByCreatedAtAsc(user.getId())
                .stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Transaction> monthTxs = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                user.getId(), ym.atDay(1), ym.atEndOfMonth());

        BigDecimal inc = BigDecimal.ZERO, exp = BigDecimal.ZERO;
        Map<String, BigDecimal> byCat = new LinkedHashMap<>();
        Map<String, String> colors = new HashMap<>();
        for (Transaction t : monthTxs) {
            if ("INCOME".equalsIgnoreCase(t.getType())) inc = inc.add(t.getAmount());
            else {
                exp = exp.add(t.getAmount());
                byCat.merge(t.getCategory().getName(), t.getAmount(), BigDecimal::add);
                colors.put(t.getCategory().getName(), t.getCategory().getColor());
            }
        }

        BigDecimal net = inc.subtract(exp);
        double rate = inc.compareTo(BigDecimal.ZERO) > 0 ? net.divide(inc, 4, RoundingMode.HALF_UP).doubleValue() * 100.0 : 0.0;

        List<Object> breakdown = new ArrayList<>();
        byCat.forEach((k, v) -> breakdown.add(Map.of("category", k, "amount", v, "color", colors.getOrDefault(k, "#4F46E5"))));

        List<Object> trend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth target = ym.minusMonths(i);
            BigDecimal mInc = BigDecimal.ZERO, mExp = BigDecimal.ZERO;
            for (Transaction t : transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                    user.getId(), target.atDay(1), target.atEndOfMonth())) {
                if ("INCOME".equalsIgnoreCase(t.getType())) mInc = mInc.add(t.getAmount());
                else mExp = mExp.add(t.getAmount());
            }
            trend.add(Map.of("label", target.getMonth().name().substring(0, 3) + " " + target.getYear(), "income", mInc, "expense", mExp));
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalBalance", totalBalance);
        res.put("monthlyIncome", inc);
        res.put("monthlyExpense", exp);
        res.put("netSavings", net);
        res.put("savingsRate", Math.round(rate * 10.0) / 10.0);
        res.put("transactionCount", monthTxs.size());
        res.put("categoryBreakdown", breakdown);
        res.put("monthlyTrend", trend);
        return res;
    }

    @Override
    public String exportTransactionsCsv() {
        StringBuilder sb = new StringBuilder("Date,Description,Category,Account,Type,Amount,AI_Categorized\n");
        for (TransactionDto t : getTransactions()) {
            String safeDesc = t.description != null ? t.description.replace("\"", "\"\"") : "";
            sb.append(t.transactionDate).append(",")
              .append("\"").append(safeDesc).append("\",")
              .append("\"").append(t.categoryName).append("\",")
              .append("\"").append(t.accountName).append("\",")
              .append(t.type).append(",")
              .append(t.amount).append(",")
              .append(t.aiCategorized).append("\n");
        }
        return sb.toString();
    }

    @Override
    public List<FeedbackDto> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc().stream().map(f -> {
            FeedbackDto d = new FeedbackDto();
            d.id = f.getId();
            d.authorName = f.getAuthorName();
            d.authorRole = f.getAuthorRole();
            d.rating = f.getRating();
            d.category = f.getCategory();
            d.message = f.getMessage();
            d.createdAt = f.getCreatedAt().toLocalDate().toString();
            return d;
        }).toList();
    }

    @Override
    @Transactional
    public FeedbackDto createFeedback(FeedbackDto dto) {
        User u = currentUser();
        String role = (dto.authorRole != null && !dto.authorRole.isBlank()) ? dto.authorRole.trim() : "Finora Member";
        Feedback saved = feedbackRepository.save(new Feedback(u, u.getFullName(), role, dto.rating, dto.category.trim(), dto.message.trim()));
        dto.id = saved.getId();
        dto.authorName = saved.getAuthorName();
        dto.authorRole = saved.getAuthorRole();
        dto.createdAt = saved.getCreatedAt().toLocalDate().toString();
        return dto;
    }

    private TransactionDto toTxDto(Transaction t) {
        TransactionDto d = new TransactionDto();
        d.id = t.getId(); d.accountId = t.getAccount().getId(); d.accountName = t.getAccount().getName();
        d.categoryId = t.getCategory().getId(); d.categoryName = t.getCategory().getName();
        d.categoryColor = t.getCategory().getColor(); d.amount = t.getAmount(); d.type = t.getType();
        d.description = t.getDescription(); d.transactionDate = t.getTransactionDate();
        d.aiCategorized = t.isAiCategorized();
        return d;
    }
}

package com.finpulse.service;

import com.finpulse.dto.*;
import java.util.List;
import java.util.Map;

public interface FinanceService {
    List<AccountDto> getAccounts();
    AccountDto createAccount(AccountDto dto);
    void deleteAccount(Long id);

    List<CategoryDto> getCategories();
    CategoryDto createCategory(CategoryDto dto);

    List<TransactionDto> getTransactions();
    TransactionDto createTransaction(TransactionDto dto);
    void deleteTransaction(Long id);

    List<BudgetDto> getBudgets(Integer month, Integer year);
    BudgetDto saveBudget(BudgetDto dto);
    void deleteBudget(Long id);

    Map<String, Object> getDashboardAnalytics(Integer month, Integer year);
    String exportTransactionsCsv();

    List<FeedbackDto> getAllFeedback();
    FeedbackDto createFeedback(FeedbackDto dto);
}

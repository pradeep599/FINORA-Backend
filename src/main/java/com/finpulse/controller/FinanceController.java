package com.finpulse.controller;

import com.finpulse.dto.*;
import com.finpulse.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FinanceController {
    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/accounts")
    public List<AccountDto> getAccounts() { return financeService.getAccounts(); }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createAccount(dto));
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        financeService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories() { return financeService.getCategories(); }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createCategory(dto));
    }

    @GetMapping("/transactions")
    public List<TransactionDto> getTransactions() { return financeService.getTransactions(); }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createTransaction(dto));
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        financeService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/budgets")
    public List<BudgetDto> getBudgets(@RequestParam(required = false) Integer month,
                                      @RequestParam(required = false) Integer year) {
        return financeService.getBudgets(month, year);
    }

    @PostMapping("/budgets")
    public ResponseEntity<BudgetDto> saveBudget(@Valid @RequestBody BudgetDto dto) {
        return ResponseEntity.ok(financeService.saveBudget(dto));
    }

    @DeleteMapping("/budgets/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        financeService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics/dashboard")
    public Map<String, Object> dashboard(@RequestParam(required = false) Integer month,
                                         @RequestParam(required = false) Integer year) {
        return financeService.getDashboardAnalytics(month, year);
    }

    @GetMapping("/reports/transactions/csv")
    public ResponseEntity<String> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Finora_Report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(financeService.exportTransactionsCsv());
    }

    @GetMapping("/feedback")
    public List<FeedbackDto> getFeedback() {
        return financeService.getAllFeedback();
    }

    @PostMapping("/feedback")
    public ResponseEntity<FeedbackDto> createFeedback(@Valid @RequestBody FeedbackDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createFeedback(dto));
    }
}

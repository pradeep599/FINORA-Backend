package com.finpulse.service;

import com.finpulse.dto.AiCategorizeResponse;
import java.util.Map;

public interface AiService {
    AiCategorizeResponse categorizeExpense(String description, String type);
    Map<String, Object> analyzeSpendingPatterns(Integer month, Integer year);
    Map<String, Object> recommendBudgets(Integer month, Integer year);
    Map<String, Object> generateMonthlySummary(Integer month, Integer year);
}

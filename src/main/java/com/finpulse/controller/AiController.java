package com.finpulse.controller;

import com.finpulse.dto.AiCategorizeRequest;
import com.finpulse.dto.AiCategorizeResponse;
import com.finpulse.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/categorize")
    public AiCategorizeResponse categorize(@RequestBody AiCategorizeRequest req) {
        return aiService.categorizeExpense(req.description, req.type);
    }

    @GetMapping("/spending-patterns")
    public Map<String, Object> patterns(@RequestParam(required = false) Integer month,
                                        @RequestParam(required = false) Integer year) {
        return aiService.analyzeSpendingPatterns(month, year);
    }

    @GetMapping("/budget-recommendations")
    public Map<String, Object> recommendations(@RequestParam(required = false) Integer month,
                                               @RequestParam(required = false) Integer year) {
        return aiService.recommendBudgets(month, year);
    }

    @GetMapping("/monthly-summary")
    public Map<String, Object> monthlySummary(@RequestParam(required = false) Integer month,
                                              @RequestParam(required = false) Integer year) {
        return aiService.generateMonthlySummary(month, year);
    }
}

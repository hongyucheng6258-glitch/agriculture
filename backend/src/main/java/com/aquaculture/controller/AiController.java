package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.dto.ChatRequest;
import com.aquaculture.dto.ChatResponse;
import com.aquaculture.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.fail("消息不能为空");
        }
        ChatResponse response = aiService.chat(message);
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/chat/history")
    public Result<ChatResponse> chatWithHistory(@RequestBody ChatRequest chatRequest) {
        if (chatRequest.getMessages() == null || chatRequest.getMessages().isEmpty()) {
            return Result.fail("消息不能为空");
        }
        ChatResponse response = aiService.chatWithHistory(chatRequest);
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/analyze/dashboard")
    public Result<ChatResponse> analyzeDashboard() {
        ChatResponse response = aiService.analyzeDashboard();
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/analyze/water-quality")
    public Result<ChatResponse> analyzeWaterQuality() {
        ChatResponse response = aiService.analyzeWaterQuality();
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/analyze/alerts")
    public Result<ChatResponse> analyzeAlerts() {
        ChatResponse response = aiService.analyzeAlerts();
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/analyze/feeding")
    public Result<ChatResponse> analyzeFeeding() {
        ChatResponse response = aiService.analyzeFeeding();
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/suggest")
    public Result<ChatResponse> smartSuggest(@RequestBody(required = false) Map<String, String> params) {
        String type = params != null ? params.get("type") : "general";
        ChatResponse response = aiService.smartSuggest(type);
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/analyze/disease")
    public Result<ChatResponse> analyzeDisease() {
        ChatResponse response = aiService.analyzeDisease();
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/analyze/weather")
    public Result<ChatResponse> analyzeWeather() {
        ChatResponse response = aiService.analyzeWeather();
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/analyze/cage")
    public Result<ChatResponse> analyzeCage() {
        ChatResponse response = aiService.analyzeCage();
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/analyze/stock")
    public Result<ChatResponse> analyzeStock() {
        ChatResponse response = aiService.analyzeStock();
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }

    @PostMapping("/report/comprehensive")
    public Result<ChatResponse> generateComprehensiveReport() {
        ChatResponse response = aiService.generateComprehensiveReport();
        if (response.isSuccess()) {
            return Result.success(response);
        } else {
            return Result.fail(response.getError());
        }
    }
}

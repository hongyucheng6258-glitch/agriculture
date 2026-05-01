package com.aquaculture.service;

import com.aquaculture.dto.ChatRequest;
import com.aquaculture.dto.ChatResponse;

import java.util.Map;

public interface AiService {
    ChatResponse chat(String message);
    ChatResponse chatWithHistory(ChatRequest chatRequest);
    ChatResponse analyzeDashboard();
    ChatResponse analyzeWaterQuality();
    ChatResponse analyzeAlerts();
    ChatResponse analyzeFeeding();
    ChatResponse smartSuggest(String contextType);
    ChatResponse analyzeDisease();
    ChatResponse analyzeWeather();
    ChatResponse analyzeCage();
    ChatResponse analyzeStock();
    ChatResponse generateComprehensiveReport();
}

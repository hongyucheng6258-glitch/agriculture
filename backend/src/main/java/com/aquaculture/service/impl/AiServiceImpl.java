package com.aquaculture.service.impl;

import com.aquaculture.config.AiConfig;
import com.aquaculture.dto.ChatRequest;
import com.aquaculture.dto.ChatResponse;
import com.aquaculture.entity.Alert;
import com.aquaculture.entity.WaterQuality;
import com.aquaculture.entity.Disease;
import com.aquaculture.entity.Weather;
import com.aquaculture.entity.Cage;
import com.aquaculture.entity.FeedStock;
import com.aquaculture.service.AiService;
import com.aquaculture.service.StatisticsService;
import com.aquaculture.service.WaterQualityService;
import com.aquaculture.service.AlertService;
import com.aquaculture.service.DiseaseService;
import com.aquaculture.service.WeatherService;
import com.aquaculture.service.CageService;
import com.aquaculture.service.FeedStockService;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceImpl.class);

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private WaterQualityService waterQualityService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private DiseaseService diseaseService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private CageService cageService;

    @Autowired
    private FeedStockService feedStockService;

    private final OkHttpClient client;
    private final Gson gson;

    public AiServiceImpl() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    @Override
    public ChatResponse chat(String message) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatRequest.Message("user", message));
        ChatRequest request = new ChatRequest();
        request.setMessages(messages);
        return chatWithHistory(request);
    }

    @Override
    public ChatResponse chatWithHistory(ChatRequest chatRequest) {
        try {
            logger.info("开始调用智谱AI API...");
            logger.info("API URL: {}", aiConfig.getApiUrl());
            logger.info("Model: {}", aiConfig.getModel());

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", aiConfig.getModel());

            JsonArray messagesArray = new JsonArray();
            for (ChatRequest.Message msg : chatRequest.getMessages()) {
                JsonObject messageObj = new JsonObject();
                messageObj.addProperty("role", msg.getRole());
                messageObj.addProperty("content", msg.getContent());
                messagesArray.add(messageObj);
            }
            requestBody.add("messages", messagesArray);

            String jsonBody = gson.toJson(requestBody);
            logger.info("Request body: {}", jsonBody);

            Request request = new Request.Builder()
                    .url(aiConfig.getApiUrl())
                    .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            logger.info("发送请求到智谱AI...");
            try (Response response = client.newCall(request).execute()) {
                logger.info("收到响应，状态码: {}", response.code());

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "no body";
                    logger.error("API调用失败，响应: {}", errorBody);
                    return ChatResponse.error("API调用失败: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                logger.info("响应内容: {}", responseBody);

                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject choice = choices.get(0).getAsJsonObject();
                    JsonObject messageObj = choice.getAsJsonObject("message");
                    String content = messageObj.get("content").getAsString();
                    logger.info("成功获取AI响应: {}", content);
                    return ChatResponse.success(content);
                }

                logger.error("未找到choices字段");
                return ChatResponse.error("未获取到响应内容");
            }
        } catch (IOException e) {
            logger.error("IO错误调用AI API", e);
            return ChatResponse.error("网络错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("系统错误", e);
            return ChatResponse.error("系统错误: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse analyzeDashboard() {
        try {
            Map<String, Object> dashboardData = statisticsService.getDashboard();
            String prompt = buildDashboardAnalysisPrompt(dashboardData);
            return chat(prompt);
        } catch (Exception e) {
            logger.error("分析仪表盘失败", e);
            return ChatResponse.error("分析仪表盘失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse analyzeWaterQuality() {
        try {
            List<WaterQuality> recentData = getRecentWaterQualityData();
            String prompt = buildWaterQualityAnalysisPrompt(recentData);
            return chat(prompt);
        } catch (Exception e) {
            logger.error("分析水质失败", e);
            return ChatResponse.error("分析水质失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse analyzeAlerts() {
        try {
            List<Alert> unhandledAlerts = getUnhandledAlerts();
            String prompt = buildAlertAnalysisPrompt(unhandledAlerts);
            return chat(prompt);
        } catch (Exception e) {
            logger.error("分析告警失败", e);
            return ChatResponse.error("分析告警失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse analyzeFeeding() {
        try {
            Map<String, Object> feedingData = statisticsService.getFeedingSummary(null, null, null);
            String prompt = buildFeedingAnalysisPrompt(feedingData);
            return chat(prompt);
        } catch (Exception e) {
            logger.error("分析投喂失败", e);
            return ChatResponse.error("分析投喂失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse smartSuggest(String contextType) {
        try {
            String prompt;
            switch (contextType != null ? contextType : "general") {
                case "water":
                    prompt = "请给我一些水产养殖水质管理的专业建议，包括溶解氧、pH值、水温等指标的最佳范围和日常维护方法。";
                    break;
                case "feeding":
                    prompt = "请给我一些水产养殖投喂管理的专业建议，包括投喂量、投喂频率、饲料选择等方面。";
                    break;
                case "disease":
                    prompt = "请给我一些水产养殖常见病害预防和治疗的专业建议。";
                    break;
                case "emergency":
                    prompt = "请给我一些水产养殖应急处理方案，比如水质突变、病害爆发等情况的应对措施。";
                    break;
                default:
                    Map<String, Object> dashboard = statisticsService.getDashboard();
                    prompt = buildGeneralSuggestPrompt(dashboard);
            }
            return chat(prompt);
        } catch (Exception e) {
            logger.error("生成智能建议失败", e);
            return ChatResponse.error("生成建议失败: " + e.getMessage());
        }
    }

    private String buildDashboardAnalysisPrompt(Map<String, Object> data) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为智慧渔业系统的AI助手，分析以下系统数据并给出专业建议：\n\n");
        prompt.append("【系统概览数据】\n");
        prompt.append("- 网箱总数: ").append(data.get("cageCount")).append("\n");
        prompt.append("- 活跃网箱: ").append(data.get("activeCages")).append("\n");
        prompt.append("- 在职员工: ").append(data.get("staffCount")).append("\n");
        prompt.append("- 未处理告警: ").append(data.get("alertCount")).append("\n");
        prompt.append("- 饲料库存: ").append(data.get("feedStock")).append("kg\n");
        prompt.append("- 近7天病害数: ").append(data.get("recentDiseases")).append("\n\n");

        Object avgDo = data.get("avgDo");
        Object avgPh = data.get("avgPh");
        if (avgDo != null) {
            prompt.append("- 平均溶解氧: ").append(avgDo).append("mg/L\n");
        }
        if (avgPh != null) {
            prompt.append("- 平均pH值: ").append(avgPh).append("\n");
        }

        prompt.append("\n请根据以上数据：\n");
        prompt.append("1. 分析系统整体运行状态\n");
        prompt.append("2. 指出需要关注的问题\n");
        prompt.append("3. 给出具体的改进建议\n");
        prompt.append("4. 提供下一步行动方案");

        return prompt.toString();
    }

    private String buildWaterQualityAnalysisPrompt(List<WaterQuality> data) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为智慧渔业系统的AI助手，分析以下水质数据并给出专业建议：\n\n");
        prompt.append("【最近水质数据】\n");

        for (int i = 0; i < Math.min(5, data.size()); i++) {
            WaterQuality wq = data.get(i);
            prompt.append(String.format("- %s: 溶解氧=%.1f, pH=%.1f, 水温=%.1f℃\n",
                    wq.getRecordTime(),
                    wq.getDissolvedOxygen() != null ? wq.getDissolvedOxygen() : 0,
                    wq.getPh() != null ? wq.getPh() : 0,
                    wq.getWaterTemp() != null ? wq.getWaterTemp() : 0));
        }

        prompt.append("\n请根据以上数据：\n");
        prompt.append("1. 分析水质状况是否正常\n");
        prompt.append("2. 指出异常指标及可能原因\n");
        prompt.append("3. 给出具体的水质调节建议\n");
        prompt.append("4. 提供日常水质管理注意事项");

        return prompt.toString();
    }

    private String buildAlertAnalysisPrompt(List<Alert> alerts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为智慧渔业系统的AI助手，分析以下告警数据并给出专业建议：\n\n");
        prompt.append("【未处理告警】\n");

        if (alerts.isEmpty()) {
            prompt.append("暂无未处理告警，系统状态良好！\n\n");
            prompt.append("请给出一些预防性的建议，帮助维持系统稳定运行。");
        } else {
            for (int i = 0; i < Math.min(5, alerts.size()); i++) {
                Alert alert = alerts.get(i);
                String alertDesc = String.format("%s指标%s（当前值=%s，阈值=%s）",
                    alert.getIndicatorLabel(),
                    alert.getAlertType(),
                    alert.getCurrentValue(),
                    alert.getThresholdValue());
                prompt.append(String.format("- %s [%s]: %s (网箱ID=%s)\n",
                    alert.getCreateTime(),
                    alert.getAlertLevel(),
                    alertDesc,
                    alert.getCageId()));
            }

            prompt.append("\n请根据以上告警：\n");
            prompt.append("1. 分析告警的紧急程度和影响范围\n");
            prompt.append("2. 给出处理优先级建议\n");
            prompt.append("3. 提供具体的处理方案\n");
            prompt.append("4. 给出预防类似问题再次发生的建议");
        }

        return prompt.toString();
    }

    private String buildFeedingAnalysisPrompt(Map<String, Object> data) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为智慧渔业系统的AI助手，分析以下投喂数据并给出专业建议：\n\n");
        prompt.append("【投喂数据】\n");

        Object totalAmount = data.get("totalAmount");
        if (totalAmount != null) {
            prompt.append("- 总投喂量: ").append(totalAmount).append("kg\n");
        }

        prompt.append("\n请根据以上数据：\n");
        prompt.append("1. 分析投喂策略是否合理\n");
        prompt.append("2. 给出投喂优化建议\n");
        prompt.append("3. 提供饲料库存管理建议\n");
        prompt.append("4. 给出提高投喂效率的方案");

        return prompt.toString();
    }

    private String buildGeneralSuggestPrompt(Map<String, Object> dashboard) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是智慧渔业系统的AI助手，请根据以下系统状态提供专业的养殖建议：\n\n");
        prompt.append("【当前系统状态】\n");
        prompt.append("- 网箱数: ").append(dashboard.get("cageCount")).append("\n");
        prompt.append("- 未处理告警: ").append(dashboard.get("alertCount")).append("\n");
        prompt.append("- 饲料库存: ").append(dashboard.get("feedStock")).append("kg\n\n");
        prompt.append("请提供：\n");
        prompt.append("1. 日常养殖管理要点\n");
        prompt.append("2. 季节变化注意事项\n");
        prompt.append("3. 提高产量的建议\n");
        prompt.append("4. 成本优化方案");

        return prompt.toString();
    }

    private List<WaterQuality> getRecentWaterQualityData() {
        LambdaQueryWrapper<WaterQuality> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(WaterQuality::getRecordTime, DateUtils.getDaysAgo(3));
        wrapper.orderByDesc(WaterQuality::getRecordTime);
        wrapper.last("LIMIT 20");
        return waterQualityService.list(wrapper);
    }

    private List<Alert> getUnhandledAlerts() {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alert::getIsHandled, 0);
        wrapper.orderByDesc(Alert::getCreateTime);
        wrapper.last("LIMIT 10");
        return alertService.list(wrapper);
    }

    @Override
    public ChatResponse analyzeDisease() {
        try {
            List<Disease> recentDiseases = getRecentDiseases();
            String prompt = buildDiseaseAnalysisPrompt(recentDiseases);
            return chat(prompt);
        } catch (Exception e) {
            logger.error("分析病害失败", e);
            return ChatResponse.error("分析病害失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse analyzeWeather() {
        try {
            List<Weather> recentWeather = getRecentWeather();
            String prompt = buildWeatherAnalysisPrompt(recentWeather);
            return chat(prompt);
        } catch (Exception e) {
            logger.error("分析天气失败", e);
            return ChatResponse.error("分析天气失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse analyzeCage() {
        try {
            List<Cage> cages = getAllCages();
            String prompt = buildCageAnalysisPrompt(cages);
            return chat(prompt);
        } catch (Exception e) {
            logger.error("分析网箱失败", e);
            return ChatResponse.error("分析网箱失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse analyzeStock() {
        try {
            List<FeedStock> stocks = getFeedStocks();
            String prompt = buildStockAnalysisPrompt(stocks);
            return chat(prompt);
        } catch (Exception e) {
            logger.error("分析库存失败", e);
            return ChatResponse.error("分析库存失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse generateComprehensiveReport() {
        try {
            Map<String, Object> reportData = collectComprehensiveData();
            String prompt = buildComprehensiveReportPrompt(reportData);
            return chat(prompt);
        } catch (Exception e) {
            logger.error("生成综合报告失败", e);
            return ChatResponse.error("生成综合报告失败: " + e.getMessage());
        }
    }

    private String buildDiseaseAnalysisPrompt(List<Disease> diseases) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为智慧渔业系统的AI助手，分析以下病害数据并给出专业建议：\n\n");
        prompt.append("【最近病害记录】\n");

        if (diseases.isEmpty()) {
            prompt.append("暂无病害记录，养殖状态良好！\n\n");
            prompt.append("请给出一些病害预防的专业建议。");
        } else {
            for (int i = 0; i < Math.min(5, diseases.size()); i++) {
                Disease disease = diseases.get(i);
                prompt.append(String.format("- %s [%s]: %s (网箱ID=%s, 严重程度=%s, 状态=%s)\n",
                    disease.getDiscoverTime(),
                    disease.getSeverity(),
                    disease.getDiseaseName(),
                    disease.getCageId(),
                    disease.getSeverity(),
                    disease.getStatus()));
                if (disease.getSymptom() != null) {
                    prompt.append("  症状: ").append(disease.getSymptom()).append("\n");
                }
            }

            prompt.append("\n请根据以上病害数据：\n");
            prompt.append("1. 分析病害发生的趋势和规律\n");
            prompt.append("2. 评估当前病害风险等级\n");
            prompt.append("3. 给出具体的治疗方案\n");
            prompt.append("4. 提供病害预防措施\n");
            prompt.append("5. 建议需要重点关注的网箱");
        }

        return prompt.toString();
    }

    private String buildWeatherAnalysisPrompt(List<Weather> weatherList) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为智慧渔业系统的AI助手，分析以下天气数据并给出专业建议：\n\n");
        prompt.append("【最近天气记录】\n");

        if (weatherList.isEmpty()) {
            prompt.append("暂无天气记录。\n\n");
            prompt.append("请给出天气与养殖关系的专业建议。");
        } else {
            for (int i = 0; i < Math.min(7, weatherList.size()); i++) {
                Weather weather = weatherList.get(i);
                prompt.append(String.format("- %s: 温度=%.1f℃, 风速=%.1fm/s, %s\n",
                    weather.getRecordTime(),
                    weather.getTemperature() != null ? weather.getTemperature() : 0,
                    weather.getWindSpeed() != null ? weather.getWindSpeed() : 0,
                    weather.getWeatherDesc() != null ? weather.getWeatherDesc() : ""));
            }

            prompt.append("\n请根据以上天气数据：\n");
            prompt.append("1. 分析天气变化趋势\n");
            prompt.append("2. 评估天气对养殖的影响\n");
            prompt.append("3. 给出应对天气变化的措施\n");
            prompt.append("4. 提供天气预警建议");
        }

        return prompt.toString();
    }

    private String buildCageAnalysisPrompt(List<Cage> cages) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为智慧渔业系统的AI助手，分析以下网箱数据并给出专业建议：\n\n");
        prompt.append("【网箱状态】\n");

        int activeCount = 0;
        for (Cage cage : cages) {
            prompt.append(String.format("- 网箱%s (ID=%s): 状态=%s\n",
                cage.getCageCode(),
                cage.getId(),
                cage.getStatus()));
            if ("active".equals(cage.getStatus()) || "正常".equals(cage.getStatus())) {
                activeCount++;
            }
        }

        prompt.append(String.format("\n总计: %d个网箱, %d个活跃\n\n", cages.size(), activeCount));
        prompt.append("请根据以上网箱数据：\n");
        prompt.append("1. 分析网箱整体健康状况\n");
        prompt.append("2. 评估网箱使用效率\n");
        prompt.append("3. 给出网箱维护建议\n");
        prompt.append("4. 提供网箱优化配置方案");

        return prompt.toString();
    }

    private String buildStockAnalysisPrompt(List<FeedStock> stocks) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为智慧渔业系统的AI助手，分析以下库存数据并给出专业建议：\n\n");
        prompt.append("【饲料库存】\n");

        if (stocks.isEmpty()) {
            prompt.append("暂无库存记录。\n\n");
            prompt.append("请给出饲料库存管理的专业建议。");
        } else {
            double totalStock = 0;
            for (FeedStock stock : stocks) {
                double amount = stock.getStockAmount() != null ? stock.getStockAmount() : 0;
                totalStock += amount;
                prompt.append(String.format("- %s: 库存=%.1fkg, 单价=%.2f元, 供应商=%s\n",
                    stock.getFeedType(),
                    amount,
                    stock.getUnitPrice() != null ? stock.getUnitPrice() : 0,
                    stock.getSupplier()));
            }

            prompt.append(String.format("\n总库存: %.1fkg\n\n", totalStock));
            prompt.append("请根据以上库存数据：\n");
            prompt.append("1. 分析库存是否充足\n");
            prompt.append("2. 评估库存结构合理性\n");
            prompt.append("3. 给出库存预警建议\n");
            prompt.append("4. 提供采购计划建议\n");
            prompt.append("5. 优化库存成本控制");
        }

        return prompt.toString();
    }

    private String buildComprehensiveReportPrompt(Map<String, Object> data) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为智慧渔业系统的AI助手，基于以下全面数据生成一份专业的综合养殖报告：\n\n");

        prompt.append("【一、系统概览】\n");
        prompt.append("- 网箱总数: ").append(data.get("cageCount")).append("\n");
        prompt.append("- 活跃网箱: ").append(data.get("activeCages")).append("\n");
        prompt.append("- 未处理告警: ").append(data.get("alertCount")).append("\n\n");

        prompt.append("【二、水质状况】\n");
        prompt.append("- 平均溶解氧: ").append(data.get("avgDo")).append("mg/L\n");
        prompt.append("- 平均pH值: ").append(data.get("avgPh")).append("\n\n");

        prompt.append("【三、库存情况】\n");
        prompt.append("- 饲料库存: ").append(data.get("feedStock")).append("kg\n");
        prompt.append("- 近7天病害: ").append(data.get("recentDiseases")).append("例\n\n");

        prompt.append("请生成一份完整的养殖报告，包含以下内容：\n");
        prompt.append("1. 整体运行状况评估\n");
        prompt.append("2. 各模块专项分析\n");
        prompt.append("3. 存在问题及风险提示\n");
        prompt.append("4. 具体改进建议\n");
        prompt.append("5. 下一步行动计划");

        return prompt.toString();
    }

    private List<Disease> getRecentDiseases() {
        LambdaQueryWrapper<Disease> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Disease::getDiscoverTime, DateUtils.getDaysAgo(30));
        wrapper.orderByDesc(Disease::getDiscoverTime);
        wrapper.last("LIMIT 20");
        return diseaseService.list(wrapper);
    }

    private List<Weather> getRecentWeather() {
        LambdaQueryWrapper<Weather> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Weather::getRecordTime, DateUtils.getDaysAgo(7) + " 00:00:00");
        wrapper.orderByDesc(Weather::getRecordTime);
        wrapper.last("LIMIT 10");
        return weatherService.list(wrapper);
    }

    private List<Cage> getAllCages() {
        return cageService.list();
    }

    private List<FeedStock> getFeedStocks() {
        return feedStockService.list();
    }

    private Map<String, Object> collectComprehensiveData() {
        Map<String, Object> data = new HashMap<>();
        try {
            Map<String, Object> dashboard = statisticsService.getDashboard();
            data.putAll(dashboard);
        } catch (Exception e) {
            logger.warn("获取仪表盘数据失败", e);
            data.put("cageCount", 0);
            data.put("activeCages", 0);
            data.put("alertCount", 0);
            data.put("feedStock", 0);
            data.put("recentDiseases", 0);
            data.put("avgDo", "-");
            data.put("avgPh", "-");
        }
        return data;
    }
}

package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.*;
import com.aquaculture.service.*;
import com.aquaculture.util.AlertEngine;
import com.aquaculture.util.DateUtils;
import com.aquaculture.util.TraceCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/simulate")
public class DataSimulateController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private WaterQualityService waterQualityService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private AlertEngine alertEngine;

    @Autowired
    private TraceService traceService;

    @Autowired
    private FeedingService feedingService;

    @Autowired
    private DiseaseService diseaseService;

    private static final String[] WEATHER_DESCS = {"晴", "多云", "阴", "小雨", "大雨"};
    private static final String[] CONSUMER_NAMES = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十", "郑十一", "王十二"};
    private static final String[] PHONE_PREFIXES = {"138", "139", "150", "151", "152", "186", "187", "188", "189", "136"};
    private static final String[] ADDRESSES = {"北京市朝阳区", "上海市浦东新区", "广州市天河区", "深圳市南山区", "杭州市西湖区", "南京市鼓楼区", "成都市武侯区", "武汉市洪山区", "西安市雁塔区", "重庆市渝中区"};
    private static final String[] FEED_TYPES = {"颗粒饲料A", "颗粒饲料B", "鱼粉饲料", "混合饲料", "天然饵料"};
    private static final String[] DISEASE_NAMES = {"水霉病", "烂鳃病", "肠炎病", "赤皮病", "出血病"};
    private static final String[] SEED_SOURCES = {"福建养殖场", "浙江育苗基地", "广东水产中心", "山东渔场", "海南良种场"};

    @PostMapping("/water-quality")
    public Result<?> simulateWaterQuality(@RequestBody Map<String, Object> params) {
        Long cageId = Long.valueOf(params.get("cageId").toString());
        Integer count = params.get("count") != null ? Integer.valueOf(params.get("count").toString()) : 100;
        int startDay = params.get("startDay") != null ? Integer.valueOf(params.get("startDay").toString()) : -30;
        int endDay = params.get("endDay") != null ? Integer.valueOf(params.get("endDay").toString()) : 0;

        // 确保 minDay <= maxDay
        int minDay = Math.min(startDay, endDay);
        int maxDay = Math.max(startDay, endDay);
        int range = maxDay - minDay;
        if (range <= 0) range = 1; // 至少1天范围

        Random random = ThreadLocalRandom.current();
        List<WaterQuality> list = new ArrayList<>();
        String now = DateUtils.now();

        for (int i = 0; i < count; i++) {
            WaterQuality wq = new WaterQuality();
            wq.setCageId(cageId);

            // Random time between minDay and maxDay (negative=past, positive=future)
            int randomDays = random.nextInt(range + 1) + minDay;
            int randomHours = random.nextInt(24);
            int randomMinutes = random.nextInt(60);
            LocalDateTime dateTime = LocalDateTime.now()
                    .plusDays(randomDays)
                    .withHour(randomHours)
                    .withMinute(randomMinutes)
                    .withSecond(0)
                    .withNano(0);
            String recordTime = dateTime.format(FORMATTER);
            wq.setRecordTime(recordTime);

            wq.setWaterTemp(15.0 + random.nextDouble() * 13.0); // 15-28
            wq.setSalinity(20.0 + random.nextDouble() * 12.0);  // 20-32
            wq.setDissolvedOxygen(4.0 + random.nextDouble() * 5.0); // 4-9
            wq.setPh(6.0 + random.nextDouble() * 3.0);          // 6.0-9.0

            // Round to 2 decimal places
            wq.setWaterTemp(Math.round(wq.getWaterTemp() * 100.0) / 100.0);
            wq.setSalinity(Math.round(wq.getSalinity() * 100.0) / 100.0);
            wq.setDissolvedOxygen(Math.round(wq.getDissolvedOxygen() * 100.0) / 100.0);
            wq.setPh(Math.round(wq.getPh() * 100.0) / 100.0);

            wq.setDataSource("模拟数据");
            wq.setCreateTime(now);
            wq.setUpdateTime(now);
            list.add(wq);
        }

        waterQualityService.saveBatch(list);

        // 对每条模拟数据触发预警检查
        int alertCount = 0;
        for (WaterQuality wq : list) {
            List<com.aquaculture.entity.Alert> alerts = alertEngine.checkWaterQuality(
                    wq.getCageId(), wq.getDissolvedOxygen(), wq.getPh(), wq.getWaterTemp());
            alertCount += alerts.size();
        }

        String msg = "成功生成" + count + "条水质模拟数据";
        if (alertCount > 0) {
            msg += "，触发" + alertCount + "条预警";
        }
        return Result.success(msg);
    }

    @PostMapping("/weather")
    public Result<?> simulateWeather(@RequestBody(required = false) Map<String, Object> params) {
        if (params == null) {
            params = new java.util.HashMap<>();
        }
        Integer count = params.get("count") != null ? Integer.valueOf(params.get("count").toString()) : 100;
        int startDay = params.get("startDay") != null ? Integer.valueOf(params.get("startDay").toString()) : -30;
        int endDay = params.get("endDay") != null ? Integer.valueOf(params.get("endDay").toString()) : 0;

        // 确保 minDay <= maxDay
        int minDay = Math.min(startDay, endDay);
        int maxDay = Math.max(startDay, endDay);
        int range = maxDay - minDay;
        if (range <= 0) range = 1; // 至少1天范围

        Random random = ThreadLocalRandom.current();
        List<Weather> list = new ArrayList<>();
        String now = DateUtils.now();

        for (int i = 0; i < count; i++) {
            Weather weather = new Weather();

            // Random time between minDay and maxDay (negative=past, positive=future)
            int randomDays = random.nextInt(range + 1) + minDay;
            int randomHours = random.nextInt(24);
            int randomMinutes = random.nextInt(60);
            LocalDateTime dateTime = LocalDateTime.now()
                    .plusDays(randomDays)
                    .withHour(randomHours)
                    .withMinute(randomMinutes)
                    .withSecond(0)
                    .withNano(0);
            String recordTime = dateTime.format(FORMATTER);
            weather.setRecordTime(recordTime);

            weather.setTemperature(10.0 + random.nextDouble() * 25.0); // 10-35
            weather.setWindSpeed(random.nextDouble() * 15.0);           // 0-15
            weather.setWeatherDesc(WEATHER_DESCS[random.nextInt(WEATHER_DESCS.length)]);

            // Round to 2 decimal places
            weather.setTemperature(Math.round(weather.getTemperature() * 100.0) / 100.0);
            weather.setWindSpeed(Math.round(weather.getWindSpeed() * 100.0) / 100.0);

            weather.setDataSource("模拟数据");
            weather.setCreateTime(now);
            weather.setUpdateTime(now);
            list.add(weather);
        }

        weatherService.saveBatch(list);
        return Result.success("成功生成" + count + "条天气模拟数据");
    }

    @PostMapping("/trace")
    @Transactional
    public Result<?> simulateTrace(@RequestBody Map<String, Object> params) {
        Long cageId = Long.valueOf(params.get("cageId").toString());
        Integer count = params.get("count") != null ? Integer.valueOf(params.get("count").toString()) : 50;
        
        Random random = ThreadLocalRandom.current();
        String now = DateUtils.now();
        List<Trace> traceList = new ArrayList<>();
        List<Feeding> feedingList = new ArrayList<>();
        List<Disease> diseaseList = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Trace trace = new Trace();
            trace.setTraceCode(TraceCodeGenerator.generate());
            trace.setCageId(cageId);
            trace.setBatchNo("BATCH-" + System.currentTimeMillis() + "-" + i);
            trace.setStatus(random.nextBoolean() ? "已审核" : "待审核");
            if ("已审核".equals(trace.getStatus())) {
                trace.setAuditUser("管理员");
                trace.setAuditTime(now);
            }
            
            // 苗种信息 - 60-90天前
            LocalDateTime seedTime = LocalDateTime.now().minusDays(60 + random.nextInt(30));
            trace.setSeedPurchaseTime(seedTime.format(FORMATTER));
            trace.setSeedSpec((5 + random.nextInt(10)) + "cm");
            trace.setSeedSource(SEED_SOURCES[random.nextInt(SEED_SOURCES.length)]);
            
            // 捕捞时间 - 0-10天前
            LocalDateTime harvestTime = LocalDateTime.now().minusDays(random.nextInt(10));
            trace.setHarvestTime(harvestTime.format(FORMATTER));
            
            // 随机消费者信息
            if (random.nextBoolean()) {
                trace.setConsumerName(CONSUMER_NAMES[random.nextInt(CONSUMER_NAMES.length)]);
                trace.setConsumerPhone(PHONE_PREFIXES[random.nextInt(PHONE_PREFIXES.length)] + 
                    String.format("%08d", random.nextInt(100000000)));
                trace.setConsumerAddress(ADDRESSES[random.nextInt(ADDRESSES.length)]);
                trace.setSaleTime(LocalDateTime.now().minusDays(random.nextInt(5)).format(FORMATTER));
                trace.setSaleQuantity(Math.round((5.0 + random.nextDouble() * 20.0) * 10.0) / 10.0);
            }
            
            trace.setProcessStandard("符合GB/T 2001标准");
            trace.setProductQuality("优等");
            trace.setCreateTime(now);
            trace.setUpdateTime(now);
            
            // 生成投喂概况和数据
            int feedingCount = 5 + random.nextInt(10);
            StringBuilder feedingSummary = new StringBuilder();
            for (int j = 0; j < feedingCount; j++) {
                Feeding feeding = new Feeding();
                feeding.setCageId(cageId);
                String feedType = FEED_TYPES[random.nextInt(FEED_TYPES.length)];
                feeding.setFeedType(feedType);
                // 投喂量保留1位小数
                double amount = Math.round((1.0 + random.nextDouble() * 5.0) * 10.0) / 10.0;
                feeding.setFeedAmount(amount);
                
                LocalDateTime feedTime = seedTime.plusDays(j * 5 + random.nextInt(3));
                feeding.setFeedingTime(feedTime.format(FORMATTER));
                feeding.setOperator("饲养员" + (random.nextInt(10) + 1));
                feeding.setCreateTime(now);
                feeding.setUpdateTime(now);
                feedingList.add(feeding);
                
                feedingSummary.append(feedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .append(" 投喂 ").append(feedType)
                    .append(" ").append(String.format("%.1f", amount)).append("kg；");
            }
            trace.setFeedingSummary(feedingSummary.toString());
            
            // 生成病害概况和数据（30%概率有病害）
            if (random.nextDouble() < 0.3) {
                int diseaseCount = 1 + random.nextInt(2);
                StringBuilder diseaseSummary = new StringBuilder();
                for (int j = 0; j < diseaseCount; j++) {
                    Disease disease = new Disease();
                    disease.setCageId(cageId);
                    String diseaseName = DISEASE_NAMES[random.nextInt(DISEASE_NAMES.length)];
                    disease.setDiseaseName(diseaseName);
                    disease.setSymptom("鱼体出现" + diseaseName.replace("病", "") + "症状");
                    disease.setSeverity(random.nextBoolean() ? "轻度" : "中度");
                    disease.setTreatment("使用专用药物治疗，已好转");
                    disease.setHandler("技术员" + (random.nextInt(10) + 1));
                    
                    LocalDateTime discoverTime = seedTime.plusDays(20 + random.nextInt(30));
                    disease.setDiscoverTime(discoverTime.format(FORMATTER));
                    disease.setHandleTime(discoverTime.plusDays(3 + random.nextInt(5)).format(FORMATTER));
                    disease.setStatus("已处理");
                    disease.setCreateTime(now);
                    disease.setUpdateTime(now);
                    diseaseList.add(disease);
                    
                    diseaseSummary.append(discoverTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .append(" 发现").append(diseaseName)
                        .append("，").append(disease.getSeverity()).append("，已处理；");
                }
                trace.setDiseaseSummary(diseaseSummary.toString());
            } else {
                trace.setDiseaseSummary("养殖期间无病害记录");
            }
            
            traceList.add(trace);
        }
        
        // 批量保存
        if (!traceList.isEmpty()) {
            traceService.saveBatch(traceList);
        }
        if (!feedingList.isEmpty()) {
            feedingService.saveBatch(feedingList);
        }
        if (!diseaseList.isEmpty()) {
            diseaseService.saveBatch(diseaseList);
        }
        
        return Result.success("成功生成" + traceList.size() + "条溯源记录，" + 
            feedingList.size() + "条投喂记录，" + diseaseList.size() + "条病害记录");
    }
}

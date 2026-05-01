package com.aquaculture.service.impl;

import com.aquaculture.entity.*;
import com.aquaculture.mapper.*;
import com.aquaculture.service.StatisticsService;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private CageMapper cageMapper;

    @Autowired
    private WaterQualityMapper waterQualityMapper;

    @Autowired
    private FeedingMapper feedingMapper;

    @Autowired
    private DiseaseMapper diseaseMapper;

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private FeedStockMapper feedStockMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private WeatherMapper weatherMapper;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // 总网箱数
        Long cageCount = cageMapper.selectCount(new QueryWrapper<>());
        dashboard.put("cageCount", cageCount);

        // 活跃网箱数 (status = '使用中')
        Long activeCages = cageMapper.selectCount(
                new LambdaQueryWrapper<Cage>().eq(Cage::getStatus, "使用中")
        );
        dashboard.put("activeCages", activeCages);

        // 在职员工数
        Long staffCount = staffMapper.selectCount(
                new LambdaQueryWrapper<Staff>().eq(Staff::getStatus, "在职")
        );
        dashboard.put("staffCount", staffCount);

        // 未处理告警数
        Long alertCount = alertMapper.selectCount(
                new LambdaQueryWrapper<Alert>().eq(Alert::getIsHandled, 0)
        );
        dashboard.put("alertCount", alertCount);

        // 饲料总库存
        List<FeedStock> feedStocks = feedStockMapper.selectList(new QueryWrapper<>());
        Double feedStock = feedStocks.stream()
                .mapToDouble(fs -> fs.getStockAmount() != null ? fs.getStockAmount() : 0.0)
                .sum();
        dashboard.put("feedStock", feedStock);

        // ===== 新增：各网箱最新水质数据（大屏用） =====
        List<Cage> allCages = cageMapper.selectList(new QueryWrapper<>());
        List<Map<String, Object>> cagesData = new ArrayList<>();
        for (Cage cage : allCages) {
            Map<String, Object> cageInfo = new HashMap<>();
            cageInfo.put("id", cage.getId());
            cageInfo.put("cageCode", cage.getCageCode());
            cageInfo.put("name", cage.getCageCode());

            // 获取该网箱最新一条水质记录
            WaterQuality latestWq = waterQualityMapper.selectOne(
                    new LambdaQueryWrapper<WaterQuality>()
                            .eq(WaterQuality::getCageId, cage.getId())
                            .orderByDesc(WaterQuality::getRecordTime)
                            .last("LIMIT 1")
            );
            cageInfo.put("dissolvedOxygen", latestWq != null ? latestWq.getDissolvedOxygen() : null);
            cageInfo.put("ph", latestWq != null ? latestWq.getPh() : null);
            cageInfo.put("waterTemp", latestWq != null ? latestWq.getWaterTemp() : null);
            cageInfo.put("salinity", latestWq != null ? latestWq.getSalinity() : null);
            cagesData.add(cageInfo);
        }
        dashboard.put("cages", cagesData);

        // 汇总溶解氧和pH
        DoubleSummaryStatistics doStats = cagesData.stream()
                .filter(c -> c.get("dissolvedOxygen") != null)
                .mapToDouble(c -> ((Number) c.get("dissolvedOxygen")).doubleValue())
                .summaryStatistics();
        dashboard.put("avgDo", doStats.getCount() > 0 ? doStats.getAverage() : null);
        dashboard.put("maxDo", doStats.getCount() > 0 ? doStats.getMax() : null);
        dashboard.put("minDo", doStats.getCount() > 0 ? doStats.getMin() : null);

        DoubleSummaryStatistics phStats = cagesData.stream()
                .filter(c -> c.get("ph") != null)
                .mapToDouble(c -> ((Number) c.get("ph")).doubleValue())
                .summaryStatistics();
        dashboard.put("avgPh", phStats.getCount() > 0 ? phStats.getAverage() : null);
        dashboard.put("maxPh", phStats.getCount() > 0 ? phStats.getMax() : null);
        dashboard.put("minPh", phStats.getCount() > 0 ? phStats.getMin() : null);

        // ===== 新增：最新天气数据（大屏用） =====
        Weather latestWeather = weatherMapper.selectOne(
                new LambdaQueryWrapper<Weather>()
                        .orderByDesc(Weather::getRecordTime)
                        .last("LIMIT 1")
        );
        dashboard.put("temperature", latestWeather != null ? latestWeather.getTemperature() : null);
        dashboard.put("windSpeed", latestWeather != null ? latestWeather.getWindSpeed() : null);
        dashboard.put("weather", latestWeather != null ? latestWeather.getWeatherDesc() : "晴");

        // 今日投喂汇总（按网箱分组）
        String todayStart = DateUtils.getTodayStart();
        String now = DateUtils.now();
        List<Feeding> todayFeedings = feedingMapper.selectList(
                new QueryWrapper<Feeding>()
                        .ge("feeding_time", todayStart)
                        .le("feeding_time", now)
        );

        // 按网箱分组
        Map<Long, List<Feeding>> byCageMap = todayFeedings.stream()
                .collect(Collectors.groupingBy(Feeding::getCageId));

        List<Map<String, Object>> feedingSummary = new ArrayList<>();
        for (Map.Entry<Long, List<Feeding>> entry : byCageMap.entrySet()) {
            Cage cage = cageMapper.selectById(entry.getKey());
            List<Feeding> cageFeedings = entry.getValue();
            double totalAmount = cageFeedings.stream()
                    .mapToDouble(f -> f.getFeedAmount() != null ? f.getFeedAmount() : 0.0)
                    .sum();
            // 获取最后一次投喂时间（yyyy-MM-dd HH:mm:ss 格式字符串可直接按字典序比较）
            String lastTime = cageFeedings.stream()
                    .map(Feeding::getFeedingTime)
                    .filter(Objects::nonNull)
                    .max(String::compareTo)
                    .orElse("-");

            Map<String, Object> item = new HashMap<>();
            item.put("cageCode", cage != null ? cage.getCageCode() : "未知网箱");
            // 投喂量保留1位小数
            item.put("totalAmount", Math.round(totalAmount * 10.0) / 10.0);
            item.put("feedingTime", lastTime);
            feedingSummary.add(item);
        }
        dashboard.put("feedingSummary", feedingSummary);

        // 近7天病害数
        String sevenDaysAgo = DateUtils.getDaysAgo(7);
        Long recentDiseases = diseaseMapper.selectCount(
                new QueryWrapper<Disease>()
                        .ge("discover_time", sevenDaysAgo)
                        .le("discover_time", now)
        );
        dashboard.put("recentDiseases", recentDiseases);

        // ===== 新增：图表数据（仪表盘用） =====
        // 1. 近7天投喂量趋势数据
        List<Map<String, Object>> feedingTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String dayStart = DateUtils.getDayStart(i);
            String dayEnd = DateUtils.getDayEnd(i);
            List<Feeding> dayFeedings = feedingMapper.selectList(
                    new QueryWrapper<Feeding>()
                            .ge("feeding_time", dayStart)
                            .le("feeding_time", dayEnd)
            );
            double dayTotal = dayFeedings.stream()
                    .mapToDouble(f -> f.getFeedAmount() != null ? f.getFeedAmount() : 0.0)
                    .sum();
            Map<String, Object> dayItem = new HashMap<>();
            dayItem.put("date", dayStart.substring(0, 10));
            dayItem.put("amount", Math.round(dayTotal * 10.0) / 10.0);
            feedingTrend.add(dayItem);
        }
        dashboard.put("feedingTrend", feedingTrend);

        // 2. 预警统计（按预警类型分组）
        Map<String, Long> alertByTypeMap = alertMapper.selectList(
                new LambdaQueryWrapper<Alert>()
                        .eq(Alert::getIsHandled, 0)
        ).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getIndicatorLabel() != null ? a.getIndicatorLabel() : "其他",
                        Collectors.counting()
                ));
        List<Map<String, Object>> alertStats = new ArrayList<>();
        for (Map.Entry<String, Long> entry : alertByTypeMap.entrySet()) {
            Map<String, Object> statItem = new HashMap<>();
            statItem.put("name", entry.getKey());
            statItem.put("value", entry.getValue());
            alertStats.add(statItem);
        }
        dashboard.put("alertStats", alertStats);

        // 3. 水质指标统计（最近7天的水质数据分布）
        List<WaterQuality> recentWaterQuality = waterQualityMapper.selectList(
                new QueryWrapper<WaterQuality>()
                        .ge("record_time", sevenDaysAgo)
                        .le("record_time", now)
                        .orderByDesc("record_time")
                        .last("LIMIT 20")
        );
        List<Map<String, Object>> waterQualityStats = new ArrayList<>();
        for (WaterQuality wq : recentWaterQuality) {
            Map<String, Object> wqItem = new HashMap<>();
            wqItem.put("recordTime", wq.getRecordTime());
            wqItem.put("dissolvedOxygen", wq.getDissolvedOxygen());
            wqItem.put("ph", wq.getPh());
            wqItem.put("waterTemp", wq.getWaterTemp());
            wqItem.put("salinity", wq.getSalinity());
            waterQualityStats.add(wqItem);
        }
        dashboard.put("waterQualityStats", waterQualityStats);

        return dashboard;
    }

    @Override
    public Map<String, Object> getFeedingSummary(String startTime, String endTime, Long cageId) {
        Map<String, Object> summary = new HashMap<>();

        QueryWrapper<Feeding> queryWrapper = new QueryWrapper<>();
        if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.ge("feeding_time", startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.le("feeding_time", endTime);
        }
        if (cageId != null) {
            queryWrapper.eq("cage_id", cageId);
        }

        List<Feeding> feedings = feedingMapper.selectList(queryWrapper);

        Double totalAmount = feedings.stream()
                .mapToDouble(f -> f.getFeedAmount() != null ? f.getFeedAmount() : 0.0)
                .sum();
        summary.put("totalAmount", totalAmount);

        Map<Long, Double> byCageMap = feedings.stream()
                .collect(Collectors.groupingBy(
                        Feeding::getCageId,
                        Collectors.summingDouble(f -> f.getFeedAmount() != null ? f.getFeedAmount() : 0.0)
                ));

        List<Map<String, Object>> byCageType = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : byCageMap.entrySet()) {
            Cage cage = cageMapper.selectById(entry.getKey());
            Map<String, Object> item = new HashMap<>();
            item.put("cageCode", cage != null ? cage.getCageCode() : "unknown");
            // 投喂量保留1位小数
            item.put("amount", Math.round(entry.getValue() * 10.0) / 10.0);
            byCageType.add(item);
        }
        summary.put("byCageType", byCageType);

        Map<String, Double> byFeedTypeMap = feedings.stream()
                .filter(f -> f.getFeedType() != null)
                .collect(Collectors.groupingBy(
                        Feeding::getFeedType,
                        Collectors.summingDouble(f -> f.getFeedAmount() != null ? f.getFeedAmount() : 0.0)
                ));

        List<Map<String, Object>> byFeedType = new ArrayList<>();
        for (Map.Entry<String, Double> entry : byFeedTypeMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("feedType", entry.getKey());
            item.put("amount", entry.getValue());
            byFeedType.add(item);
        }
        summary.put("byFeedType", byFeedType);

        return summary;
    }

    @Override
    public Map<String, Object> getDiseaseSummary(String startTime, String endTime, Long cageId) {
        Map<String, Object> summary = new HashMap<>();

        QueryWrapper<Disease> queryWrapper = new QueryWrapper<>();
        if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.ge("discover_time", startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.le("discover_time", endTime);
        }
        if (cageId != null) {
            queryWrapper.eq("cage_id", cageId);
        }

        List<Disease> diseases = diseaseMapper.selectList(queryWrapper);

        summary.put("totalCount", diseases.size());

        Map<String, Long> bySeverityMap = diseases.stream()
                .filter(d -> d.getSeverity() != null)
                .collect(Collectors.groupingBy(Disease::getSeverity, Collectors.counting()));

        List<Map<String, Object>> bySeverity = new ArrayList<>();
        for (Map.Entry<String, Long> entry : bySeverityMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("severity", entry.getKey());
            item.put("count", entry.getValue());
            bySeverity.add(item);
        }
        summary.put("bySeverity", bySeverity);

        Map<String, Long> byDiseaseNameMap = diseases.stream()
                .filter(d -> d.getDiseaseName() != null)
                .collect(Collectors.groupingBy(Disease::getDiseaseName, Collectors.counting()));

        List<Map<String, Object>> byDiseaseName = new ArrayList<>();
        for (Map.Entry<String, Long> entry : byDiseaseNameMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("diseaseName", entry.getKey());
            item.put("count", entry.getValue());
            byDiseaseName.add(item);
        }
        summary.put("byDiseaseName", byDiseaseName);

        return summary;
    }
}

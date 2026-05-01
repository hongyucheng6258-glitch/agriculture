package com.aquaculture.util;

import com.aquaculture.entity.Alert;
import com.aquaculture.entity.AlertThreshold;
import com.aquaculture.entity.FeedStock;
import com.aquaculture.entity.WaterQuality;
import com.aquaculture.mapper.AlertMapper;
import com.aquaculture.mapper.AlertThresholdMapper;
import com.aquaculture.mapper.FeedStockMapper;
import com.aquaculture.mapper.WaterQualityMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class AlertEngine {
    @Autowired
    private AlertThresholdMapper thresholdMapper;
    @Autowired
    private AlertMapper alertMapper;
    @Autowired
    private FeedStockMapper feedStockMapper;
    @Autowired
    private WaterQualityMapper waterQualityMapper;

    // Check water quality indicators (dissolved_oxygen, ph, water_temp)
    public List<Alert> checkWaterQuality(Long cageId, Double dissolvedOxygen, Double ph, Double waterTemp) {
        // Get all enabled thresholds
        List<AlertThreshold> thresholds = thresholdMapper.selectList(
            new LambdaQueryWrapper<AlertThreshold>().eq(AlertThreshold::getIsEnabled, 1));

        ArrayList<Alert> alerts = new ArrayList<>();

        for (AlertThreshold t : thresholds) {
            Double currentValue = null;
            if ("dissolved_oxygen".equals(t.getIndicatorName()) && dissolvedOxygen != null) {
                currentValue = dissolvedOxygen;
            } else if ("ph".equals(t.getIndicatorName()) && ph != null) {
                currentValue = ph;
            } else if ("water_temp".equals(t.getIndicatorName()) && waterTemp != null) {
                currentValue = waterTemp;
            }

            if (currentValue != null) {
                Alert alert = checkAndCreateAlert(cageId, t, currentValue, null);
                if (alert != null) {
                    alerts.add(alert);
                }
            }
        }
        return alerts;
    }

    // Check feed stock level
    public List<Alert> checkFeedStock() {
        List<AlertThreshold> thresholds = thresholdMapper.selectList(
            new LambdaQueryWrapper<AlertThreshold>()
                .eq(AlertThreshold::getIsEnabled, 1)
                .eq(AlertThreshold::getIndicatorName, "feed_stock"));

        ArrayList<Alert> alerts = new ArrayList<>();

        if (!thresholds.isEmpty()) {
            AlertThreshold t = thresholds.get(0);
            List<FeedStock> stocks = feedStockMapper.selectList(null);
            for (FeedStock stock : stocks) {
                if (t.getMinValue() != null && stock.getStockAmount() < t.getMinValue()) {
                    Alert alert = checkAndCreateAlert(null, t, stock.getStockAmount(), "饲料库存-" + stock.getFeedType());
                    if (alert != null) {
                        alerts.add(alert);
                    }
                }
            }
        }
        return alerts;
    }

    // Re-check all alerts when thresholds change
    public int reCheckAllAlerts() {
        return reCheckByThreshold(null);
    }

    // Re-check alerts for a specific threshold
    public int reCheckByThreshold(Long thresholdId) {
        List<AlertThreshold> thresholds;
        if (thresholdId != null) {
            thresholds = new ArrayList<>();
            AlertThreshold t = thresholdMapper.selectById(thresholdId);
            if (t != null) {
                thresholds.add(t);
            }
        } else {
            thresholds = thresholdMapper.selectList(
                new LambdaQueryWrapper<AlertThreshold>().eq(AlertThreshold::getIsEnabled, 1));
        }

        int newAlerts = 0;

        for (AlertThreshold t : thresholds) {
            if ("feed_stock".equals(t.getIndicatorName())) {
                // Check feed stock
                List<FeedStock> stocks = feedStockMapper.selectList(null);
                for (FeedStock stock : stocks) {
                    if (t.getMinValue() != null && stock.getStockAmount() < t.getMinValue()) {
                        Alert alert = checkAndCreateAlert(null, t, stock.getStockAmount(), "饲料库存-" + stock.getFeedType());
                        if (alert != null) newAlerts++;
                    }
                }
            } else {
                // Check water quality (last 30 days)
                LambdaQueryWrapper<WaterQuality> wrapper = new LambdaQueryWrapper<>();
                wrapper.orderByDesc(WaterQuality::getRecordTime);
                wrapper.last("LIMIT 100"); // Limit to avoid performance issues
                List<WaterQuality> waterQualities = waterQualityMapper.selectList(wrapper);
                
                for (WaterQuality wq : waterQualities) {
                    Double currentValue = null;
                    if ("dissolved_oxygen".equals(t.getIndicatorName())) {
                        currentValue = wq.getDissolvedOxygen();
                    } else if ("ph".equals(t.getIndicatorName())) {
                        currentValue = wq.getPh();
                    } else if ("water_temp".equals(t.getIndicatorName())) {
                        currentValue = wq.getWaterTemp();
                    }
                    
                    if (currentValue != null) {
                        Alert alert = checkAndCreateAlert(wq.getCageId(), t, currentValue, null);
                        if (alert != null) newAlerts++;
                    }
                }
            }
        }

        return newAlerts;
    }

    // Check threshold and create alert with deduplication
    private Alert checkAndCreateAlert(Long cageId, AlertThreshold threshold, Double currentValue, String customLabel) {
        boolean triggered = false;
        String alertType = "";
        Double thresholdValue = null;

        if (threshold.getMinValue() != null && currentValue < threshold.getMinValue()) {
            triggered = true;
            alertType = "低于下限";
            thresholdValue = threshold.getMinValue();
        } else if (threshold.getMaxValue() != null && currentValue > threshold.getMaxValue()) {
            triggered = true;
            alertType = "高于上限";
            thresholdValue = threshold.getMaxValue();
        }

        if (!triggered) {
            return null;
        }

        // Deduplication: check if similar alert already exists in last 1 hour
        String label = customLabel != null ? customLabel : threshold.getIndicatorLabel();
        String oneHourAgo = LocalDateTime.now().minusHours(1)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LambdaQueryWrapper<Alert> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Alert::getCageId, cageId);
        checkWrapper.eq(Alert::getIndicatorName, threshold.getIndicatorName());
        checkWrapper.eq(Alert::getAlertType, alertType);
        checkWrapper.eq(Alert::getIsHandled, 0);
        // Only check alerts created in the last 1 hour to avoid duplicates
        checkWrapper.ge(Alert::getCreateTime, oneHourAgo);
        // Use limit(1) to avoid fetching all records
        checkWrapper.last("LIMIT 1");
        List<Alert> existing = alertMapper.selectList(checkWrapper);
        if (!existing.isEmpty()) {
            return null; // Skip duplicate
        }

        Alert alert = new Alert();
        alert.setCageId(cageId);
        alert.setIndicatorName(threshold.getIndicatorName());
        alert.setIndicatorLabel(label);
        alert.setCurrentValue(currentValue);
        alert.setThresholdValue(thresholdValue);
        alert.setAlertType(alertType);
        alert.setAlertLevel(determineLevel(currentValue, thresholdValue));
        alert.setIsHandled(0);
        alert.setCreateTime(DateUtils.now());
        alert.setUpdateTime(DateUtils.now());
        alertMapper.insert(alert);
        return alert;
    }

    private String determineLevel(Double currentValue, Double thresholdValue) {
        if (thresholdValue == 0) return "紧急";
        double deviation = Math.abs(currentValue - thresholdValue) / thresholdValue;
        if (deviation > 0.3) return "紧急";
        if (deviation > 0.1) return "严重";
        return "一般";
    }
}

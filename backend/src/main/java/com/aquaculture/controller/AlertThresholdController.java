package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.AlertThreshold;
import com.aquaculture.service.AlertThresholdService;
import com.aquaculture.util.AlertEngine;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alert-threshold")
public class AlertThresholdController {

    @Autowired
    private AlertThresholdService alertThresholdService;
    
    @Autowired
    private AlertEngine alertEngine;

    @GetMapping("/list")
    public Result<?> list() {
        List<AlertThreshold> result = alertThresholdService.list(
                new LambdaQueryWrapper<AlertThreshold>().orderByAsc(AlertThreshold::getId));
        return Result.success(result);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable(value = "id") Long id, @RequestBody AlertThreshold threshold) {
        threshold.setId(id);
        threshold.setUpdateTime(DateUtils.now());
        alertThresholdService.updateById(threshold);
        return Result.success("更新成功");
    }

    @PostMapping("/{id}/recheck")
    public Result<?> recheckByThreshold(@PathVariable(value = "id") Long id) {
        int newAlerts = alertEngine.reCheckByThreshold(id);
        Map<String, Object> result = new HashMap<>();
        result.put("newAlerts", newAlerts);
        return Result.success("重新检查完成，新增 " + newAlerts + " 条预警", result);
    }

    @PostMapping("/recheck-all")
    public Result<?> recheckAll() {
        int newAlerts = alertEngine.reCheckAllAlerts();
        Map<String, Object> result = new HashMap<>();
        result.put("newAlerts", newAlerts);
        return Result.success("全部重新检查完成，新增 " + newAlerts + " 条预警", result);
    }

    @PostMapping("/init")
    public Result<?> init() {
        // Define default thresholds
        List<AlertThreshold> defaults = Arrays.asList(
                createDefault("dissolved_oxygen", "溶解氧", 4.0, 9.0, "mg/L"),
                createDefault("ph", "pH值", 6.5, 8.5, ""),
                createDefault("water_temp", "水温", 15.0, 30.0, "℃"),
                createDefault("feed_stock", "饲料库存", 100.0, null, "kg")
        );

        int inserted = 0;
        for (AlertThreshold def : defaults) {
            LambdaQueryWrapper<AlertThreshold> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AlertThreshold::getIndicatorName, def.getIndicatorName());
            Long count = alertThresholdService.count(wrapper);
            if (count == 0) {
                alertThresholdService.save(def);
                inserted++;
            }
        }
        return Result.success("初始化完成，新增" + inserted + "条阈值配置");
    }

    private AlertThreshold createDefault(String name, String label, Double min, Double max, String unit) {
        AlertThreshold t = new AlertThreshold();
        t.setIndicatorName(name);
        t.setIndicatorLabel(label);
        t.setMinValue(min);
        t.setMaxValue(max);
        t.setUnit(unit);
        t.setIsEnabled(1);
        t.setCreateTime(DateUtils.now());
        t.setUpdateTime(DateUtils.now());
        return t;
    }
}

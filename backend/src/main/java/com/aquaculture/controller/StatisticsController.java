package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        Map<String, Object> data = statisticsService.getDashboard();
        return Result.success(data);
    }

    @GetMapping("/feeding-summary")
    public Result<?> feedingSummary(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "cageId", required = false) Long cageId) {
        Map<String, Object> data = statisticsService.getFeedingSummary(startTime, endTime, cageId);
        return Result.success(data);
    }

    @GetMapping("/disease-summary")
    public Result<?> diseaseSummary(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "cageId", required = false) Long cageId) {
        Map<String, Object> data = statisticsService.getDiseaseSummary(startTime, endTime, cageId);
        return Result.success(data);
    }
}

package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.WaterQuality;
import com.aquaculture.service.WaterQualityService;
import com.aquaculture.util.AlertEngine;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/water-quality")
public class WaterQualityController {

    @Autowired
    private WaterQualityService waterQualityService;

    @Autowired
    private AlertEngine alertEngine;

    @PostMapping("/list")
    public Result<?> list(@RequestBody(required = false) Map<String, Object> params) {
        int page = 1, size = 10;
        Long cageId = null;
        String startTime = null, endTime = null;
        if (params != null) {
            if (params.get("page") != null) page = Integer.parseInt(params.get("page").toString());
            if (params.get("size") != null) size = Integer.parseInt(params.get("size").toString());
            if (params.get("cageId") != null) cageId = Long.parseLong(params.get("cageId").toString());
            if (params.get("startTime") != null) startTime = params.get("startTime").toString();
            if (params.get("endTime") != null) endTime = params.get("endTime").toString();
        }
        Page<WaterQuality> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<WaterQuality> wrapper = new LambdaQueryWrapper<>();
        if (cageId != null) {
            wrapper.eq(WaterQuality::getCageId, cageId);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(WaterQuality::getRecordTime, startTime + " 00:00:00");
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(WaterQuality::getRecordTime, endTime + " 23:59:59");
        }
        wrapper.orderByDesc(WaterQuality::getRecordTime);
        Page<WaterQuality> result = waterQualityService.page(pageObj, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable(value = "id") Long id) {
        WaterQuality waterQuality = waterQualityService.getById(id);
        if (waterQuality == null) {
            return Result.fail("水质记录不存在");
        }
        return Result.success(waterQuality);
    }

    @PostMapping
    public Result<?> save(@RequestBody WaterQuality waterQuality) {
        String now = DateUtils.now();
        waterQuality.setCreateTime(now);
        waterQuality.setUpdateTime(now);
        if (waterQuality.getRecordTime() == null || waterQuality.getRecordTime().isEmpty()) {
            waterQuality.setRecordTime(now);
        }
        waterQualityService.save(waterQuality);
        // Trigger alert check
        alertEngine.checkWaterQuality(
                waterQuality.getCageId(),
                waterQuality.getDissolvedOxygen(),
                waterQuality.getPh(),
                waterQuality.getWaterTemp());
        return Result.success("新增成功");
    }

    @PostMapping("/batch")
    public Result<?> saveBatch(@RequestBody List<WaterQuality> list) {
        String now = DateUtils.now();
        for (WaterQuality wq : list) {
            wq.setCreateTime(now);
            wq.setUpdateTime(now);
        }
        waterQualityService.saveBatch(list);
        return Result.success("批量新增成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable(value = "id") Long id, @RequestBody WaterQuality waterQuality) {
        waterQuality.setId(id);
        waterQuality.setUpdateTime(DateUtils.now());
        waterQualityService.updateById(waterQuality);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable(value = "id") Long id) {
        waterQualityService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/trend")
    public Result<?> trend(
            @RequestParam(value = "cageId", required = false) Long cageId,
            @RequestParam(value = "indicator", required = false) String indicator,
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        LambdaQueryWrapper<WaterQuality> wrapper = new LambdaQueryWrapper<>();
        if (cageId != null) {
            wrapper.eq(WaterQuality::getCageId, cageId);
        }
        wrapper.ge(WaterQuality::getRecordTime, DateUtils.getDaysAgo(days));
        wrapper.orderByAsc(WaterQuality::getRecordTime);
        List<WaterQuality> result = waterQualityService.list(wrapper);
        return Result.success(result);
    }
}

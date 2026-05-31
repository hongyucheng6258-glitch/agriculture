package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.Weather;
import com.aquaculture.service.WeatherService;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @PostMapping("/list")
    public Result<?> list(@RequestBody(required = false) Map<String, Object> params) {
        int page = 1, size = 10;
        String startTime = null, endTime = null;
        if (params != null) {
            if (params.get("page") != null) page = Integer.parseInt(params.get("page").toString());
            if (params.get("size") != null) size = Integer.parseInt(params.get("size").toString());
            if (params.get("startTime") != null) startTime = params.get("startTime").toString();
            if (params.get("endTime") != null) endTime = params.get("endTime").toString();
        }
        Page<Weather> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Weather> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(Weather::getRecordTime, startTime + " 00:00:00");
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(Weather::getRecordTime, endTime + " 23:59:59");
        }
        wrapper.orderByDesc(Weather::getRecordTime);
        Page<Weather> result = weatherService.page(pageObj, wrapper);
        return Result.success(result);
    }

    @PostMapping
    public Result<?> save(@RequestBody Weather weather) {
        String now = DateUtils.now();
        weather.setCreateTime(now);
        weather.setUpdateTime(now);
        if (weather.getRecordTime() == null || weather.getRecordTime().isEmpty()) {
            weather.setRecordTime(now);
        }
        weatherService.save(weather);
        return Result.success("新增成功");
    }

    @PostMapping("/batch")
    public Result<?> saveBatch(@RequestBody List<Weather> list) {
        String now = DateUtils.now();
        for (Weather w : list) {
            w.setCreateTime(now);
            w.setUpdateTime(now);
        }
        weatherService.saveBatch(list);
        return Result.success("批量新增成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable(value = "id") Long id, @RequestBody Weather weather) {
        weather.setId(id);
        weather.setUpdateTime(DateUtils.now());
        weatherService.updateById(weather);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable(value = "id") Long id) {
        weatherService.removeById(id);
        return Result.success("删除成功");
    }
}

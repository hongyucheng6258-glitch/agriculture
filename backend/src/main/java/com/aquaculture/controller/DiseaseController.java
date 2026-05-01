package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.Disease;
import com.aquaculture.service.DiseaseService;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/disease")
public class DiseaseController {

    @Autowired
    private DiseaseService diseaseService;

    @PostMapping("/list")
    public Result<?> list(@RequestBody(required = false) Map<String, Object> params) {
        int page = 1, size = 10;
        Long cageId = null;
        String status = null, startTime = null, endTime = null;
        if (params != null) {
            if (params.get("page") != null) page = Integer.parseInt(params.get("page").toString());
            if (params.get("size") != null) size = Integer.parseInt(params.get("size").toString());
            if (params.get("cageId") != null) cageId = Long.parseLong(params.get("cageId").toString());
            if (params.get("status") != null) status = params.get("status").toString();
            if (params.get("startTime") != null) startTime = params.get("startTime").toString();
            if (params.get("endTime") != null) endTime = params.get("endTime").toString();
        }
        Page<Disease> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Disease> wrapper = new LambdaQueryWrapper<>();
        if (cageId != null) {
            wrapper.eq(Disease::getCageId, cageId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Disease::getStatus, status);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(Disease::getDiscoverTime, DateUtils.parse(startTime));
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(Disease::getDiscoverTime, DateUtils.parse(endTime));
        }
        wrapper.orderByDesc(Disease::getDiscoverTime);
        Page<Disease> result = diseaseService.page(pageObj, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable(value = "id") Long id) {
        Disease disease = diseaseService.getById(id);
        if (disease == null) {
            return Result.fail("病害记录不存在");
        }
        return Result.success(disease);
    }

    @PostMapping
    public Result<?> save(@RequestBody Disease disease) {
        String now = DateUtils.now();
        disease.setCreateTime(now);
        disease.setUpdateTime(now);
        if (disease.getDiscoverTime() == null || disease.getDiscoverTime().isEmpty()) {
            disease.setDiscoverTime(now);
        }
        diseaseService.save(disease);
        return Result.success("新增成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable(value = "id") Long id, @RequestBody Disease disease) {
        disease.setId(id);
        disease.setUpdateTime(DateUtils.now());
        diseaseService.updateById(disease);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/handle")
    public Result<?> handle(@PathVariable(value = "id") Long id, @RequestBody Map<String, String> params) {
        Disease disease = diseaseService.getById(id);
        if (disease == null) {
            return Result.fail("病害记录不存在");
        }
        disease.setHandler(params.get("handler"));
        disease.setHandleTime(DateUtils.now());
        disease.setStatus("已解决");
        disease.setUpdateTime(DateUtils.now());
        diseaseService.updateById(disease);
        return Result.success("处理成功");
    }
}

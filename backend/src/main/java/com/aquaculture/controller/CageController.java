package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.Cage;
import com.aquaculture.service.CageService;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cage")
public class CageController {

    @Autowired
    private CageService cageService;

    @PostMapping("/list")
    public Result<?> list(@RequestBody(required = false) Map<String, Object> params) {
        int page = 1, size = 10;
        String cageCode = null, status = null;
        if (params != null) {
            if (params.get("page") != null) page = Integer.parseInt(params.get("page").toString());
            if (params.get("size") != null) size = Integer.parseInt(params.get("size").toString());
            if (params.get("cageCode") != null) cageCode = params.get("cageCode").toString();
            if (params.get("status") != null) status = params.get("status").toString();
        }
        Page<Cage> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Cage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(cageCode)) {
            wrapper.like(Cage::getCageCode, cageCode);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Cage::getStatus, status);
        }
        wrapper.orderByDesc(Cage::getCreateTime);
        Page<Cage> result = cageService.page(pageObj, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable(value = "id") Long id) {
        Cage cage = cageService.getById(id);
        if (cage == null) {
            return Result.fail("网箱不存在");
        }
        return Result.success(cage);
    }

    @GetMapping("/all")
    public Result<?> all() {
        return Result.success(cageService.list());
    }

    @PostMapping
    public Result<?> save(@RequestBody Cage cage) {
        cage.setCreateTime(DateUtils.now());
        cage.setUpdateTime(DateUtils.now());
        cageService.save(cage);
        return Result.success("新增成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable(value = "id") Long id, @RequestBody Cage cage) {
        cage.setId(id);
        cage.setUpdateTime(DateUtils.now());
        cageService.updateById(cage);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable(value = "id") Long id) {
        cageService.removeById(id);
        return Result.success("删除成功");
    }
}

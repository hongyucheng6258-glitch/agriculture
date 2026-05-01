package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.FeedStock;
import com.aquaculture.entity.Feeding;
import com.aquaculture.service.FeedStockService;
import com.aquaculture.service.FeedingService;
import com.aquaculture.util.AlertEngine;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feeding")
public class FeedingController {

    @Autowired
    private FeedingService feedingService;

    @Autowired
    private FeedStockService feedStockService;

    @Autowired
    private AlertEngine alertEngine;

    @PostMapping("/list")
    public Result<?> list(@RequestBody(required = false) Map<String, Object> params) {
        int page = 1, size = 10;
        Long cageId = null;
        String feedType = null, startTime = null, endTime = null;
        if (params != null) {
            if (params.get("page") != null) page = Integer.parseInt(params.get("page").toString());
            if (params.get("size") != null) size = Integer.parseInt(params.get("size").toString());
            if (params.get("cageId") != null) cageId = Long.parseLong(params.get("cageId").toString());
            if (params.get("feedType") != null) feedType = params.get("feedType").toString();
            if (params.get("startTime") != null) startTime = params.get("startTime").toString();
            if (params.get("endTime") != null) endTime = params.get("endTime").toString();
        }
        Page<Feeding> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Feeding> wrapper = new LambdaQueryWrapper<>();
        if (cageId != null) {
            wrapper.eq(Feeding::getCageId, cageId);
        }
        if (StringUtils.hasText(feedType)) {
            wrapper.eq(Feeding::getFeedType, feedType);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(Feeding::getFeedingTime, DateUtils.parse(startTime));
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(Feeding::getFeedingTime, DateUtils.parse(endTime));
        }
        wrapper.orderByDesc(Feeding::getFeedingTime);
        Page<Feeding> result = feedingService.page(pageObj, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable(value = "id") Long id) {
        Feeding feeding = feedingService.getById(id);
        if (feeding == null) {
            return Result.fail("投喂记录不存在");
        }
        return Result.success(feeding);
    }

    @PostMapping
    public Result<?> save(@RequestBody Feeding feeding) {
        String now = DateUtils.now();
        feeding.setCreateTime(now);
        feeding.setUpdateTime(now);
        if (feeding.getFeedingTime() == null || feeding.getFeedingTime().isEmpty()) {
            feeding.setFeedingTime(now);
        }
        feedingService.save(feeding);

        // Auto deduct feed stock
        if (StringUtils.hasText(feeding.getFeedType()) && feeding.getFeedAmount() != null) {
            LambdaQueryWrapper<FeedStock> stockWrapper = new LambdaQueryWrapper<>();
            stockWrapper.eq(FeedStock::getFeedType, feeding.getFeedType());
            FeedStock stock = feedStockService.getOne(stockWrapper);
            if (stock != null) {
                stock.setStockAmount(stock.getStockAmount() - feeding.getFeedAmount());
                stock.setUpdateTime(DateUtils.now());
                feedStockService.updateById(stock);
            }
        }

        // Check feed stock alert
        alertEngine.checkFeedStock();

        return Result.success("新增成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable(value = "id") Long id, @RequestBody Feeding feeding) {
        feeding.setId(id);
        feeding.setUpdateTime(DateUtils.now());
        feedingService.updateById(feeding);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable(value = "id") Long id) {
        feedingService.removeById(id);
        return Result.success("删除成功");
    }
}

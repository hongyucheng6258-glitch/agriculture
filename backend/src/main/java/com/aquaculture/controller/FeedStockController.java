package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.FeedStock;
import com.aquaculture.service.FeedStockService;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feed-stock")
public class FeedStockController {

    @Autowired
    private FeedStockService feedStockService;

    @GetMapping("/list")
    public Result<?> list() {
        List<FeedStock> result = feedStockService.list(
                new LambdaQueryWrapper<FeedStock>().orderByAsc(FeedStock::getId));
        return Result.success(result);
    }

    @PostMapping
    public Result<?> save(@RequestBody FeedStock feedStock) {
        feedStock.setCreateTime(DateUtils.now());
        feedStock.setUpdateTime(DateUtils.now());
        feedStockService.save(feedStock);
        return Result.success("新增成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable(value = "id") Long id, @RequestBody FeedStock feedStock) {
        feedStock.setId(id);
        feedStock.setUpdateTime(DateUtils.now());
        feedStockService.updateById(feedStock);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/restock")
    public Result<?> restock(@PathVariable(value = "id") Long id, @RequestBody Map<String, Double> params) {
        FeedStock feedStock = feedStockService.getById(id);
        if (feedStock == null) {
            return Result.fail("饲料库存记录不存在");
        }
        Double amount = params.get("amount");
        if (amount == null || amount <= 0) {
            return Result.fail("补货数量必须大于0");
        }
        feedStock.setStockAmount(feedStock.getStockAmount() + amount);
        feedStock.setLastRestockTime(DateUtils.now());
        feedStock.setUpdateTime(DateUtils.now());
        feedStockService.updateById(feedStock);
        return Result.success("补货成功");
    }
}

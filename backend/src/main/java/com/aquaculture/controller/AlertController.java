package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.Alert;
import com.aquaculture.service.AlertService;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alert")
public class AlertController {

    @Autowired
    private AlertService alertService;

    /**
     * 告警分页列表查询 - 使用POST + @RequestBody避免中文参数URL编码问题
     * 浏览器XHR发送GET请求时，中文参数以原始UTF-8字节出现在HTTP请求行中，
     * Tomcat 9严格模式会拒绝这种请求返回400。使用POST请求体传递参数彻底绕过此问题。
     */
    @PostMapping("/list")
    public Result<?> list(@RequestBody(required = false) Map<String, Object> params) {
        int page = 1;
        int size = 10;
        Long cageId = null;
        Integer isHandled = null;
        String alertLevel = null;
        String startTime = null;
        String endTime = null;

        if (params != null) {
            if (params.get("page") != null) {
                page = Integer.parseInt(params.get("page").toString());
            }
            if (params.get("size") != null) {
                size = Integer.parseInt(params.get("size").toString());
            }
            if (params.get("cageId") != null) {
                cageId = Long.parseLong(params.get("cageId").toString());
            }
            if (params.get("isHandled") != null) {
                isHandled = Integer.parseInt(params.get("isHandled").toString());
            }
            if (params.get("alertLevel") != null) {
                alertLevel = params.get("alertLevel").toString();
            }
            if (params.get("startTime") != null) {
                startTime = params.get("startTime").toString();
            }
            if (params.get("endTime") != null) {
                endTime = params.get("endTime").toString();
            }
        }

        Page<Alert> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        if (cageId != null) {
            wrapper.eq(Alert::getCageId, cageId);
        }
        if (isHandled != null) {
            wrapper.eq(Alert::getIsHandled, isHandled);
        }
        if (StringUtils.hasText(alertLevel)) {
            wrapper.eq(Alert::getAlertLevel, alertLevel);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(Alert::getCreateTime, DateUtils.parse(startTime));
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(Alert::getCreateTime, DateUtils.parse(endTime));
        }
        wrapper.orderByDesc(Alert::getCreateTime);
        Page<Alert> result = alertService.page(pageObj, wrapper);
        return Result.success(result);
    }

    @GetMapping("/unhandled")
    public Result<?> unhandled() {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alert::getIsHandled, 0);
        wrapper.orderByDesc(Alert::getCreateTime);
        List<Alert> result = alertService.list(wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable(value = "id") Long id) {
        Alert alert = alertService.getById(id);
        if (alert == null) {
            return Result.fail("告警记录不存在");
        }
        return Result.success(alert);
    }

    @PutMapping("/{id}/handle")
    public Result<?> handle(@PathVariable(value = "id") Long id, @RequestBody Map<String, String> params) {
        Alert alert = alertService.getById(id);
        if (alert == null) {
            return Result.fail("告警记录不存在");
        }
        alert.setHandler(params.get("handler"));
        alert.setHandleTime(DateUtils.now());
        alert.setIsHandled(1);
        alert.setHandleRemark(params.get("handleRemark"));
        alert.setUpdateTime(DateUtils.now());
        alertService.updateById(alert);
        return Result.success("处理成功");
    }

    @GetMapping("/count")
    public Result<?> count() {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alert::getIsHandled, 0);
        Long count = alertService.count(wrapper);
        return Result.success(count);
    }
}

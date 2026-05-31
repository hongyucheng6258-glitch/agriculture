package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.Staff;
import com.aquaculture.service.StaffService;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @PostMapping("/list")
    public Result<?> list(@RequestBody(required = false) Map<String, Object> params) {
        int page = 1, size = 10;
        String name = null, position = null, status = null;
        if (params != null) {
            if (params.get("page") != null) page = Integer.parseInt(params.get("page").toString());
            if (params.get("size") != null) size = Integer.parseInt(params.get("size").toString());
            if (params.get("name") != null) name = params.get("name").toString();
            if (params.get("position") != null) position = params.get("position").toString();
            if (params.get("status") != null) status = params.get("status").toString();
        }
        Page<Staff> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Staff::getName, name);
        }
        if (StringUtils.hasText(position)) {
            wrapper.eq(Staff::getPosition, position);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Staff::getStatus, status);
        }
        wrapper.orderByDesc(Staff::getCreateTime);
        Page<Staff> result = staffService.page(pageObj, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable(value = "id") Long id) {
        Staff staff = staffService.getById(id);
        if (staff == null) {
            return Result.fail("员工不存在");
        }
        return Result.success(staff);
    }

    @PostMapping
    public Result<?> save(@RequestBody Staff staff) {
        staff.setCreateTime(DateUtils.now());
        staff.setUpdateTime(DateUtils.now());
        staffService.save(staff);
        return Result.success("新增成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable(value = "id") Long id, @RequestBody Staff staff) {
        staff.setId(id);
        staff.setUpdateTime(DateUtils.now());
        staffService.updateById(staff);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable(value = "id") Long id) {
        staffService.removeById(id);
        return Result.success("删除成功");
    }
}

package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.SysUser;
import com.aquaculture.mapper.SysUserMapper;
import com.aquaculture.service.SysUserService;
import com.aquaculture.util.AuthUtil;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping
    public Result<?> list(@RequestHeader(value = "Authorization", required = false) String authHeader,
                         @RequestParam(defaultValue = "1") int pageNum,
                         @RequestParam(defaultValue = "10") int pageSize,
                         @RequestParam(required = false) String keyword) {
        if (!AuthUtil.isAdmin(authHeader)) {
            return Result.fail(403, "无权限操作");
        }
        Page<SysUser> page = sysUserService.page(pageNum, pageSize, keyword);
        // 移除密码字段
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@RequestHeader(value = "Authorization", required = false) String authHeader,
                            @PathVariable Long id) {
        if (!AuthUtil.isAdmin(authHeader)) {
            return Result.fail(403, "无权限操作");
        }
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping
    public Result<?> add(@RequestHeader(value = "Authorization", required = false) String authHeader,
                        @RequestBody SysUser user) {
        if (!AuthUtil.isAdmin(authHeader)) {
            return Result.fail(403, "无权限操作");
        }
        if (!StringUtils.hasText(user.getUsername()) || 
            !StringUtils.hasText(user.getPassword()) ||
            !StringUtils.hasText(user.getRealName())) {
            return Result.fail("用户名、密码和真实姓名不能为空");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            return Result.fail("用户名已存在");
        }

        if (!StringUtils.hasText(user.getRole())) {
            user.setRole("operator");
        }

        boolean success = sysUserService.add(user);
        return success ? Result.success("添加成功") : Result.fail("添加失败");
    }

    @PutMapping("/{id}")
    public Result<?> update(@RequestHeader(value = "Authorization", required = false) String authHeader,
                           @PathVariable Long id, @RequestBody SysUser user) {
        if (!AuthUtil.isAdmin(authHeader)) {
            return Result.fail(403, "无权限操作");
        }
        user.setId(id);
        boolean success = sysUserService.update(user);
        return success ? Result.success("更新成功") : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@RequestHeader(value = "Authorization", required = false) String authHeader,
                           @PathVariable Long id) {
        if (!AuthUtil.isAdmin(authHeader)) {
            return Result.fail(403, "无权限操作");
        }
        boolean success = sysUserService.delete(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }

    @PutMapping("/{id}/toggle")
    public Result<?> toggleEnabled(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                  @PathVariable Long id) {
        if (!AuthUtil.isAdmin(authHeader)) {
            return Result.fail(403, "无权限操作");
        }
        boolean success = sysUserService.toggleEnabled(id);
        return success ? Result.success("操作成功") : Result.fail("操作失败");
    }

    @PutMapping("/{id}/password")
    public Result<?> resetPassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                  @PathVariable Long id, @RequestBody Map<String, String> params) {
        if (!AuthUtil.isAdmin(authHeader)) {
            return Result.fail(403, "无权限操作");
        }
        String newPassword = params.get("newPassword");
        if (!StringUtils.hasText(newPassword)) {
            return Result.fail("新密码不能为空");
        }

        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        user.setPassword(newPassword);
        user.setUpdateTime(DateUtils.now());
        return sysUserMapper.updateById(user) > 0 
            ? Result.success("密码重置成功") 
            : Result.fail("密码重置失败");
    }
}

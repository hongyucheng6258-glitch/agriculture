package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.entity.SysUser;
import com.aquaculture.mapper.SysUserMapper;
import com.aquaculture.util.DateUtils;
import com.aquaculture.util.TokenManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Result.fail("用户名和密码不能为空");
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (!password.equals(user.getPassword())) {
            return Result.fail("密码错误");
        }
        if (user.getIsEnabled() != null && user.getIsEnabled() != 1) {
            return Result.fail("用户已被禁用");
        }

        // Generate token
        String token = UUID.randomUUID().toString().replace("-", "");
        user.setLastLoginTime(DateUtils.now());
        sysUserMapper.updateById(user);
        user.setPassword(null);
        TokenManager.put(token, user);

        // Return both token and user info for frontend compatibility
        java.util.HashMap<String, Object> data = new java.util.HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.success("登录成功", data);
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            TokenManager.remove(token);
        }
        return Result.success("退出成功");
    }

    @GetMapping("/info")
    public Result<?> info(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!StringUtils.hasText(authHeader)) {
            return Result.fail(401, "未登录或token已过期");
        }
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        SysUser user = TokenManager.get(token);
        if (user == null) {
            return Result.fail(401, "未登录或token已过期");
        }
        // Return user info without password
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/password")
    public Result<?> password(@RequestHeader(value = "Authorization", required = false) String authHeader,
                              @RequestBody Map<String, String> params) {
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return Result.fail(401, "未登录或token已过期");
        }
        String token = authHeader.substring(7);
        SysUser user = TokenManager.get(token);
        if (user == null) {
            return Result.fail(401, "未登录或token已过期");
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            return Result.fail("旧密码和新密码不能为空");
        }

        // Verify old password
        SysUser dbUser = sysUserMapper.selectById(user.getId());
        if (!oldPassword.equals(dbUser.getPassword())) {
            return Result.fail("旧密码错误");
        }

        // Update password
        dbUser.setPassword(newPassword);
        dbUser.setUpdateTime(DateUtils.now());
        sysUserMapper.updateById(dbUser);

        return Result.success("密码修改成功");
    }

    @PutMapping("/info")
    public Result<?> updateInfo(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                @RequestBody Map<String, String> params) {
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return Result.fail(401, "未登录或token已过期");
        }
        String token = authHeader.substring(7);
        SysUser user = TokenManager.get(token);
        if (user == null) {
            return Result.fail(401, "未登录或token已过期");
        }

        SysUser dbUser = sysUserMapper.selectById(user.getId());
        if (dbUser == null) {
            return Result.fail("用户不存在");
        }

        String realName = params.get("realName");
        if (StringUtils.hasText(realName)) {
            dbUser.setRealName(realName);
        }

        dbUser.setUpdateTime(DateUtils.now());
        sysUserMapper.updateById(dbUser);

        // Update token cache
        dbUser.setPassword(null);
        TokenManager.put(token, dbUser);

        return Result.success("信息修改成功", dbUser);
    }
}

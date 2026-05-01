package com.aquaculture.service.impl;

import com.aquaculture.entity.SysUser;
import com.aquaculture.mapper.SysUserMapper;
import com.aquaculture.service.SysUserService;
import com.aquaculture.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Page<SysUser> page(int pageNum, int pageSize, String keyword) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return sysUserMapper.selectPage(page, wrapper);
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    public boolean add(SysUser user) {
        user.setCreateTime(DateUtils.now());
        user.setUpdateTime(DateUtils.now());
        if (user.getIsEnabled() == null) {
            user.setIsEnabled(1);
        }
        return sysUserMapper.insert(user) > 0;
    }

    @Override
    public boolean update(SysUser user) {
        user.setUpdateTime(DateUtils.now());
        user.setPassword(null); // 不允许通过此接口修改密码
        return sysUserMapper.updateById(user) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return sysUserMapper.deleteById(id) > 0;
    }

    @Override
    public boolean toggleEnabled(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return false;
        }
        user.setIsEnabled(user.getIsEnabled() == 1 ? 0 : 1);
        user.setUpdateTime(DateUtils.now());
        return sysUserMapper.updateById(user) > 0;
    }
}

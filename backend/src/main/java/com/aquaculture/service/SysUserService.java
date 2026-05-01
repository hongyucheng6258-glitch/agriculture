package com.aquaculture.service;

import com.aquaculture.entity.SysUser;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SysUserService {
    Page<SysUser> page(int pageNum, int pageSize, String keyword);
    SysUser getById(Long id);
    boolean add(SysUser user);
    boolean update(SysUser user);
    boolean delete(Long id);
    boolean toggleEnabled(Long id);
}

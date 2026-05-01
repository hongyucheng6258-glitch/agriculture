package com.aquaculture.service.impl;

import com.aquaculture.entity.Staff;
import com.aquaculture.mapper.StaffMapper;
import com.aquaculture.service.StaffService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {
}

package com.aquaculture.service.impl;

import com.aquaculture.entity.WaterQuality;
import com.aquaculture.mapper.WaterQualityMapper;
import com.aquaculture.service.WaterQualityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class WaterQualityServiceImpl extends ServiceImpl<WaterQualityMapper, WaterQuality> implements WaterQualityService {
}

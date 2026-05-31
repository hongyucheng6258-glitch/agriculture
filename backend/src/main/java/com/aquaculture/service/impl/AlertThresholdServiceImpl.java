package com.aquaculture.service.impl;

import com.aquaculture.entity.AlertThreshold;
import com.aquaculture.mapper.AlertThresholdMapper;
import com.aquaculture.service.AlertThresholdService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AlertThresholdServiceImpl extends ServiceImpl<AlertThresholdMapper, AlertThreshold> implements AlertThresholdService {
}

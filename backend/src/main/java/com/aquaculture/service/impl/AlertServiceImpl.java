package com.aquaculture.service.impl;

import com.aquaculture.entity.Alert;
import com.aquaculture.mapper.AlertMapper;
import com.aquaculture.service.AlertService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {
}

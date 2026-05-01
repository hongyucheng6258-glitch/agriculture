package com.aquaculture.service.impl;

import com.aquaculture.entity.Cage;
import com.aquaculture.mapper.CageMapper;
import com.aquaculture.service.CageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CageServiceImpl extends ServiceImpl<CageMapper, Cage> implements CageService {
}

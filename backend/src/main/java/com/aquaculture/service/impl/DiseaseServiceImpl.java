package com.aquaculture.service.impl;

import com.aquaculture.entity.Disease;
import com.aquaculture.mapper.DiseaseMapper;
import com.aquaculture.service.DiseaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DiseaseServiceImpl extends ServiceImpl<DiseaseMapper, Disease> implements DiseaseService {
}

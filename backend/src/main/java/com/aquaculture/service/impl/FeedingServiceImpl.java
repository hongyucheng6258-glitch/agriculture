package com.aquaculture.service.impl;

import com.aquaculture.entity.Feeding;
import com.aquaculture.mapper.FeedingMapper;
import com.aquaculture.service.FeedingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FeedingServiceImpl extends ServiceImpl<FeedingMapper, Feeding> implements FeedingService {
}

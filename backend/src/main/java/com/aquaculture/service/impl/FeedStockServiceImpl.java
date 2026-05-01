package com.aquaculture.service.impl;

import com.aquaculture.entity.FeedStock;
import com.aquaculture.mapper.FeedStockMapper;
import com.aquaculture.service.FeedStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FeedStockServiceImpl extends ServiceImpl<FeedStockMapper, FeedStock> implements FeedStockService {
}

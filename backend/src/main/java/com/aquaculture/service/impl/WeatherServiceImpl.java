package com.aquaculture.service.impl;

import com.aquaculture.entity.Weather;
import com.aquaculture.mapper.WeatherMapper;
import com.aquaculture.service.WeatherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl extends ServiceImpl<WeatherMapper, Weather> implements WeatherService {
}

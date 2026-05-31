package com.aquaculture.mapper;

import com.aquaculture.entity.Weather;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeatherMapper extends BaseMapper<Weather> {
}

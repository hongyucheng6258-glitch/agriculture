package com.aquaculture.service;

import java.util.Map;

public interface StatisticsService {

    /**
     * 获取仪表盘概览数据
     */
    Map<String, Object> getDashboard();

    /**
     * 获取投喂汇总数据
     *
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime   结束时间 (yyyy-MM-dd HH:mm:ss)
     * @param cageId    网箱ID (可选)
     */
    Map<String, Object> getFeedingSummary(String startTime, String endTime, Long cageId);

    /**
     * 获取病害汇总数据
     *
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime   结束时间 (yyyy-MM-dd HH:mm:ss)
     * @param cageId    网箱ID (可选)
     */
    Map<String, Object> getDiseaseSummary(String startTime, String endTime, Long cageId);
}

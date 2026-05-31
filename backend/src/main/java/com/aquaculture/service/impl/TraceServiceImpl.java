package com.aquaculture.service.impl;

import com.aquaculture.dto.TraceDetailDTO;
import com.aquaculture.entity.*;
import com.aquaculture.mapper.*;
import com.aquaculture.service.TraceService;
import com.aquaculture.util.DateUtils;
import com.aquaculture.util.TraceCodeGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TraceServiceImpl extends ServiceImpl<TraceMapper, Trace> implements TraceService {

    @Autowired
    private FeedingMapper feedingMapper;

    @Autowired
    private DiseaseMapper diseaseMapper;

    @Autowired
    private WaterQualityMapper waterQualityMapper;

    @Override
    public TraceDetailDTO getTraceDetail(Long id) {
        Trace trace = this.getById(id);
        if (trace == null) {
            return null;
        }

        TraceDetailDTO dto = new TraceDetailDTO();
        dto.setTrace(trace);

        // 查询关联的投喂记录（限制20条）
        LambdaQueryWrapper<Feeding> feedingWrapper = new LambdaQueryWrapper<>();
        feedingWrapper.eq(Feeding::getCageId, trace.getCageId());
        feedingWrapper.orderByDesc(Feeding::getFeedingTime);
        feedingWrapper.last("LIMIT 20");
        dto.setFeedingList(feedingMapper.selectList(feedingWrapper));

        // 查询关联的病害记录（限制20条）
        LambdaQueryWrapper<Disease> diseaseWrapper = new LambdaQueryWrapper<>();
        diseaseWrapper.eq(Disease::getCageId, trace.getCageId());
        diseaseWrapper.orderByDesc(Disease::getDiscoverTime);
        diseaseWrapper.last("LIMIT 20");
        dto.setDiseaseList(diseaseMapper.selectList(diseaseWrapper));

        // 查询关联的水质记录（限制20条）
        LambdaQueryWrapper<WaterQuality> waterWrapper = new LambdaQueryWrapper<>();
        waterWrapper.eq(WaterQuality::getCageId, trace.getCageId());
        waterWrapper.orderByDesc(WaterQuality::getRecordTime);
        waterWrapper.last("LIMIT 20");
        dto.setWaterQualityList(waterQualityMapper.selectList(waterWrapper));

        return dto;
    }

    @Override
    public void auditTrace(Long id, String auditUser) {
        Trace trace = this.getById(id);
        if (trace != null) {
            trace.setStatus("已审核");
            trace.setAuditUser(auditUser);
            trace.setAuditTime(DateUtils.now());
            trace.setUpdateTime(DateUtils.now());
            this.updateById(trace);
        }
    }

    @Override
    public void deleteTrace(Long id) {
        this.removeById(id);
    }

    @Override
    public List<Trace> batchGenerate(Integer count, Long cageId) {
        List<Trace> traces = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Trace trace = new Trace();
            trace.setTraceCode(TraceCodeGenerator.generate());
            trace.setCageId(cageId);
            trace.setBatchNo("BATCH-" + System.currentTimeMillis() + "-" + i);
            trace.setStatus("待审核");
            trace.setCreateTime(DateUtils.now());
            trace.setUpdateTime(DateUtils.now());
            this.save(trace);
            traces.add(trace);
        }
        return traces;
    }

    @Override
    public List<Trace> searchByConsumer(String keyword) {
        LambdaQueryWrapper<Trace> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(Trace::getConsumerName, keyword)
                .or().like(Trace::getConsumerPhone, keyword));
        wrapper.orderByDesc(Trace::getCreateTime);
        return this.list(wrapper);
    }
}

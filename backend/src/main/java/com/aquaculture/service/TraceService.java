package com.aquaculture.service;

import com.aquaculture.dto.TraceDetailDTO;
import com.aquaculture.entity.Trace;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TraceService extends IService<Trace> {
    TraceDetailDTO getTraceDetail(Long id);
    void auditTrace(Long id, String auditUser);
    void deleteTrace(Long id);
    List<Trace> batchGenerate(Integer count, Long cageId);
    List<Trace> searchByConsumer(String keyword);
}

package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.dto.TraceDetailDTO;
import com.aquaculture.entity.Trace;
import com.aquaculture.service.TraceService;
import com.aquaculture.util.DateUtils;
import com.aquaculture.util.QRCodeGenerator;
import com.aquaculture.util.TraceCodeGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trace")
public class TraceController {

    @Autowired
    private TraceService traceService;

    @PostMapping("/list")
    public Result<?> list(@RequestBody(required = false) Map<String, Object> params) {
        int page = 1, size = 10;
        String traceCode = null, batchNo = null, consumerKeyword = null;
        if (params != null) {
            if (params.get("page") != null) page = Integer.parseInt(params.get("page").toString());
            if (params.get("size") != null) size = Integer.parseInt(params.get("size").toString());
            if (params.get("traceCode") != null) traceCode = params.get("traceCode").toString();
            if (params.get("batchNo") != null) batchNo = params.get("batchNo").toString();
            if (params.get("consumerKeyword") != null) consumerKeyword = params.get("consumerKeyword").toString();
        }
        Page<Trace> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Trace> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(traceCode)) {
            wrapper.like(Trace::getTraceCode, traceCode);
        }
        if (StringUtils.hasText(batchNo)) {
            wrapper.like(Trace::getBatchNo, batchNo);
        }
        final String finalConsumerKeyword = consumerKeyword;
        if (StringUtils.hasText(finalConsumerKeyword)) {
            wrapper.and(w -> w.like(Trace::getConsumerName, finalConsumerKeyword)
                    .or().like(Trace::getConsumerPhone, finalConsumerKeyword));
        }
        wrapper.orderByDesc(Trace::getCreateTime);
        Page<Trace> result = traceService.page(pageObj, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable(value = "id") Long id) {
        Trace trace = traceService.getById(id);
        if (trace == null) {
            return Result.fail("溯源记录不存在");
        }
        return Result.success(trace);
    }

    @GetMapping("/{id}/detail")
    public Result<?> getDetail(@PathVariable(value = "id") Long id) {
        TraceDetailDTO dto = traceService.getTraceDetail(id);
        if (dto == null) {
            return Result.fail("溯源记录不存在");
        }
        return Result.success(dto);
    }

    @PostMapping
    public Result<?> save(@RequestBody Trace trace) {
        trace.setTraceCode(TraceCodeGenerator.generate());
        trace.setCreateTime(DateUtils.now());
        trace.setUpdateTime(DateUtils.now());
        trace.setStatus("待审核");
        traceService.save(trace);
        return Result.success("新增成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable(value = "id") Long id, @RequestBody Trace trace) {
        trace.setId(id);
        trace.setUpdateTime(DateUtils.now());
        traceService.updateById(trace);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable(value = "id") Long id) {
        traceService.deleteTrace(id);
        return Result.success("删除成功");
    }

    @PostMapping("/{id}/audit")
    public Result<?> audit(@PathVariable(value = "id") Long id, @RequestBody Map<String, String> params) {
        String auditUser = params != null && params.get("auditUser") != null ? params.get("auditUser") : "管理员";
        traceService.auditTrace(id, auditUser);
        return Result.success("审核成功");
    }

    @PostMapping("/batch")
    public Result<?> batchGenerate(@RequestBody Map<String, Object> params) {
        if (params == null || params.get("count") == null || params.get("cageId") == null) {
            return Result.fail("count 和 cageId 参数不能为空");
        }
        Integer count;
        Long cageId;
        try {
            count = Integer.parseInt(params.get("count").toString());
            cageId = Long.parseLong(params.get("cageId").toString());
        } catch (NumberFormatException e) {
            return Result.fail("count 和 cageId 必须为有效数字");
        }
        if (count <= 0 || count > 1000) {
            return Result.fail("count 必须在 1~1000 之间");
        }
        List<Trace> traces = traceService.batchGenerate(count, cageId);
        return Result.success(traces);
    }

    @GetMapping("/consumer/search")
    public Result<?> searchByConsumer(@RequestParam("keyword") String keyword) {
        List<Trace> traces = traceService.searchByConsumer(keyword);
        return Result.success(traces);
    }

    @GetMapping("/query")
    public Result<?> query(@RequestParam("traceCode") String traceCode) {
        LambdaQueryWrapper<Trace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trace::getTraceCode, traceCode);
        Trace trace = traceService.getOne(wrapper);
        if (trace == null) {
            return Result.fail("溯源码不存在");
        }
        return Result.success(trace);
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> getQRCode(@PathVariable(value = "id") Long id) {
        try {
            Trace trace = traceService.getById(id);
            if (trace == null) {
                return ResponseEntity.notFound().build();
            }
            String content = "溯源码：" + trace.getTraceCode();
            byte[] qrCodeBytes = QRCodeGenerator.generateQRCode(content, 300, 300);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "qrcode-" + trace.getTraceCode() + ".png");
            
            return ResponseEntity.ok().headers(headers).body(qrCodeBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

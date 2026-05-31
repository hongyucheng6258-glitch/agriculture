package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("trace")
public class Trace {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String traceCode;

    private Long cageId;

    private String batchNo;

    private String seedPurchaseTime;

    private String seedSpec;

    private String seedSource;

    private String feedingSummary;

    private String diseaseSummary;

    private String harvestTime;

    private String processStandard;

    private String productQuality;

    private String status;

    private String auditUser;

    private String auditTime;

    private String consumerName;

    private String consumerPhone;

    private String consumerAddress;

    private String saleTime;

    private Double saleQuantity;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}

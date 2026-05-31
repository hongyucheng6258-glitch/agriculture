package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("alert_threshold")
public class AlertThreshold {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String indicatorName;

    private String indicatorLabel;

    private Double minValue;

    private Double maxValue;

    private String unit;

    @TableField("is_enabled")
    private Integer isEnabled;

    private String remark;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}

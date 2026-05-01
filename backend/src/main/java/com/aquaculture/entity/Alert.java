package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("alert")
public class Alert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cageId;

    private String indicatorName;

    private String indicatorLabel;

    private Double currentValue;

    private Double thresholdValue;

    private String alertType;

    private String alertLevel;

    @TableField("is_handled")
    private Integer isHandled;

    private String handler;

    private String handleTime;

    private String handleRemark;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}

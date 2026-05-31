package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("disease")
public class Disease {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cageId;

    private String diseaseName;

    private String symptom;

    private String severity;

    private String treatment;

    private String handler;

    private String discoverTime;

    private String handleTime;

    private String status;

    private String remark;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}

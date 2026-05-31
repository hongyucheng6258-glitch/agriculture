package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("feeding")
public class Feeding {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cageId;

    private String feedType;

    private Double feedAmount;

    private String feedingTime;

    private String operator;

    private String remark;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}

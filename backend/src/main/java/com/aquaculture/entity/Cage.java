package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("cage")
public class Cage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String cageCode;

    private String location;

    private String breedType;

    private Double scale;

    private String status;

    private String remark;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}

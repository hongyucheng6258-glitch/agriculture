package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("water_quality")
public class WaterQuality {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cageId;

    private Double waterTemp;

    private Double salinity;

    private Double dissolvedOxygen;

    private Double ph;

    private String recordTime;

    private String dataSource;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}

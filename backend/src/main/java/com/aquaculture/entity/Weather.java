package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("weather")
public class Weather {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Double temperature;

    private Double windSpeed;

    private String weatherDesc;

    private String recordTime;

    private String dataSource;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}

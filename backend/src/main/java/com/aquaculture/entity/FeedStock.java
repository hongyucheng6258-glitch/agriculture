package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("feed_stock")
public class FeedStock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String feedType;

    private Double stockAmount;

    private Double unitPrice;

    private String supplier;

    private String lastRestockTime;

    private String remark;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}

package com.aquaculture.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String role;

    @TableField("is_enabled")
    private Integer isEnabled;

    private String lastLoginTime;

    private String createTime;

    private String updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}

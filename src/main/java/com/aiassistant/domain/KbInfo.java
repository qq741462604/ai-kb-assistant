package com.aiassistant.domain;

import lombok.Data;

import java.util.Date;

@Data
public class KbInfo {
    private Long id;
    private String canonicalField;
    private String description;
    private String aliases; // 逗号分隔字符串
    private Date createTime;
    private Date updateTime;
}

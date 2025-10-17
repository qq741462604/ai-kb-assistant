package com.aiassistant.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class KbPending {
    private Long id;
    private String originalField;
    private String aiCanonicalField;           // AI 推断的规范字段
    private String canonicalFieldDescription;
    private String reason;
    private Double confidence;
    private List<String> aliases; // JsonArrayTypeHandler 映射
    private String status; // PENDING / APPROVED / REJECTED / AUTO_APPROVED
    private Date createTime;
    private Date updateTime;

    // 新增字段用于标记和追溯（已确认可新增）
    private Boolean autoApproved; // true/false
    private Date approveTime;
}

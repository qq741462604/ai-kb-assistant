package com.aiassistant.domain;

import lombok.Data;

import java.util.List;

@Data
public class LlmMatchResult {
    private String canonicalField;   // 建议的规范字段名
    private String description;      // 建议的字段说明（可选）
    private List<String> aliases;    // 建议的别名列表（可选）
    private Double confidence;       // 置信度 0.0 - 1.0
    private String reason;           // 简短匹配理由
}

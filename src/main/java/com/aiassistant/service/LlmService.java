package com.aiassistant.service;

import com.aiassistant.domain.CanonicalField;
import com.aiassistant.domain.LlmMatchResult;

import java.util.List;

public interface LlmService {
    /**
     * 对单个原始字段进行 Top-N 匹配（LLM + 本地回退）
     *
     * @param originalField 原始字段名
     * @param canonicalFields 全量/候选规范字段（用于在 prompt 中展示候选）
     * @param topN 返回候选个数
     * @return List of LlmMatchResult（长度 <= topN）
     */
    List<LlmMatchResult> topNMatch(String originalField, List<CanonicalField> canonicalFields, int topN);
}

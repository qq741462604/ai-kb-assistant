package com.aiassistant.service.impl;

import com.aiassistant.domain.CanonicalField;
import com.aiassistant.domain.LlmMatchResult;
import com.aiassistant.llm.QwenClient;
import com.aiassistant.service.LlmService;
import com.aiassistant.service.TopNMatcherService;
import com.aiassistant.util.JsonExtractorUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LlmServiceImpl implements LlmService {

    @Resource
    private QwenClient qwenClient;

    @Resource
    private TopNMatcherService topNMatcherService;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final double FALLBACK_CONFIDENCE = 0.5;

    @Override
    public List<LlmMatchResult> topNMatch(String originalField, List<CanonicalField> canonicalFields, int topN) {

        String systemPrompt = "你是企业级字段标准化助手。任务：对给定的业务字段返回 Top " + topN
                + " 个最匹配的规范字段。输出必须严格为 JSON 数组，数组元素为对象，包含字段："
                + "canonicalField (字符串), description (字符串，可空), aliases (数组), confidence (0-1 数值), reason (简短理由)。"
                + "不要输出多余文字或 Markdown 代码块。";

        StringBuilder userSb = new StringBuilder();
        userSb.append("请为业务字段：\"").append(originalField).append("\" 匹配 Top ").append(topN).append(" 个规范字段。\n");
        userSb.append("下面是候选规范字段（每行：规范名：描述【别名】）：\n");

        int cap = 200;
        int i = 0;
        for (CanonicalField f : canonicalFields) {
            if (i++ >= cap) break;
            userSb.append(f.getCanonicalField() == null ? "" : f.getCanonicalField());
            if (f.getDescription() != null && !f.getDescription().isEmpty()) {
                userSb.append("：").append(f.getDescription());
            }
            if (f.getAliases() != null && !f.getAliases().isEmpty()) {
                userSb.append("【").append(String.join(",", f.getAliases())).append("】");
            }
            userSb.append("\n");
        }
        userSb.append("\n请仅返回 JSON 数组并确保数组为合法 JSON。");

        // 2. 调用 LLM
        String rawResp = null;
        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
            Map<String, Object> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", userSb.toString());
            messages.add(user);

            rawResp = qwenClient.chat(messages);
            log.debug("LLM rawResp: {}", rawResp);
        } catch (Exception e) {
            log.warn("LLM 调用异常，准备回退到本地 TopN: {}", e.getMessage());
        }

        // 3. 解析 LLM 返回
        List<LlmMatchResult> llmResults = new ArrayList<>();
        if (rawResp != null) {
            try {
                String arrayStr = JsonExtractorUtil.extractFirstJsonArray(rawResp);
                if (arrayStr != null) {
                    llmResults = MAPPER.readValue(arrayStr, new TypeReference<List<LlmMatchResult>>() {});
                    llmResults = llmResults.stream().map(LlmServiceImpl::normalizeAndFill).collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("解析或补全 LLM 返回失败，error = {}", e.getMessage());
            }
        }

        // 4. 返回 TopN 或回退本地
        if (!llmResults.isEmpty()) {
            return llmResults.stream().limit(topN).collect(Collectors.toList());
        }

        // 回退 TopN
        try {
            List<CanonicalField> localTop = topNMatcherService.matchTopN(originalField, canonicalFields);
            List<LlmMatchResult> fallback = new ArrayList<>();
            for (int idx = 0; idx < Math.min(topN, localTop.size()); idx++) {
                CanonicalField f = localTop.get(idx);
                LlmMatchResult r = new LlmMatchResult();
                r.setCanonicalField(f.getCanonicalField());
                r.setDescription(f.getDescription() == null ? "" : f.getDescription());
                r.setAliases(f.getAliases() == null ? new ArrayList<>() : f.getAliases());
                r.setConfidence(FALLBACK_CONFIDENCE);
                r.setReason("本地 KB 匹配回退（TopN）");
                fallback.add(r);
            }
            return fallback;
        } catch (Exception ex) {
            log.error("回退 TopN 匹配也失败", ex);
            return new ArrayList<>();
        }
    }

    private static LlmMatchResult normalizeAndFill(LlmMatchResult r) {
        if (r == null) r = new LlmMatchResult();
        if (r.getCanonicalField() == null) r.setCanonicalField("");
        if (r.getDescription() == null) r.setDescription("");
        if (r.getAliases() == null) r.setAliases(new ArrayList<>());
        if (r.getConfidence() == null) r.setConfidence(0.0);
        if (r.getReason() == null) r.setReason("LLM返回字段缺失，系统自动补全");
        if (r.getConfidence() < 0.0) r.setConfidence(0.0);
        if (r.getConfidence() > 1.0) r.setConfidence(1.0);
        return r;
    }
}

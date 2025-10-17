package com.aiassistant.util;

import com.aiassistant.domain.LlmMatchResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class LLMResponseParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 总入口 - 给定 LLM 的完整返回 JSON 字符串，自动提取并转为 List<LlmMatchResult>
     */
    public static List<LlmMatchResult> extractMatches(String llmRawJson) {
        try {
            String content = extractContent(llmRawJson);
            if (content == null) {
                log.warn("LLMResponseParser: extractContent failed, raw = {}", llmRawJson);
                return new ArrayList<>();
            }
            String jsonArrayText = extractFirstJsonArray(content);
            if (jsonArrayText == null) {
                log.warn("LLMResponseParser: No JSON array found in content = {}", content);
                return new ArrayList<>();
            }
            return mapper.readValue(jsonArrayText, new TypeReference<List<LlmMatchResult>>() {});
        } catch (Exception e) {
            log.error("LLMResponseParser: failed to parse LLM response", e);
            return new ArrayList<>();
        }
    }

    /**
     * Step1: 解析 choices[0].message.content
     */
    private static String extractContent(String llmRawJson) {
        try {
            JsonNode root = mapper.readTree(llmRawJson);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }
            log.warn("LLMResponseParser: choices[0].message.content not found");
        } catch (Exception e) {
            log.error("LLMResponseParser: extractContent error", e);
        }
        return null;
    }

    /**
     * Step2: 从 content 文本中截取第一个合法 JSON 数组 "[ ... ]"
     */
    private static String extractFirstJsonArray(String text) {
        Pattern pattern = Pattern.compile("\\[[\\s\\S]*?\\]");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}

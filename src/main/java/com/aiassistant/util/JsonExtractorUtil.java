package com.aiassistant.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonExtractorUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 从原始文本中抽取第一个合法 JSON 数组。
     * 支持：
     * 1. 顶层 JSON 数组
     * 2. Chat Completion 包装 content 中的数组
     * 3. 多行、嵌套、换行
     */
    public static String extractFirstJsonArray(String raw) {
        if (raw == null) return null;

        try {
            // 尝试解析为 JsonNode
            JsonNode node = MAPPER.readTree(raw);

            if (node.isArray()) {
                return MAPPER.writeValueAsString(node);
            } else if (node.has("choices")) {
                JsonNode contentNode = node.at("/choices/0/message/content");
                if (!contentNode.isMissingNode()) {
                    String content = contentNode.asText();
                    return extractFirstJsonArray(content); // 递归解析 content
                }
            }

            // 顶层不是数组，也没有 content
            // 使用计数器方式手动匹配数组
            int start = raw.indexOf('[');
            if (start == -1) return null;

            int bracketCount = 0;
            for (int i = start; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if (c == '[') bracketCount++;
                else if (c == ']') bracketCount--;

                if (bracketCount == 0) {
                    return raw.substring(start, i + 1);
                }
            }

        } catch (Exception e) {
            log.warn("extractFirstJsonArray 解析异常: {}", e.getMessage());
        }

        return null;
    }
}

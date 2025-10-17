package com.aiassistant.llm;

import com.aiassistant.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class QwenClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private OkHttpClient client;

    @Value("${llm.api.url}")
    private String apiUrl;

    @Value("${llm.api.key:}")
    private String apiKey;

    @Value("${llm.model:qwen-plus}")
    private String modelName;


    /**
     * 调用 Qwen 接口
     * @param messages 消息列表，格式示例：
     *                 [
     *                   { "role":"system", "content":"你是助手" },
     *                   { "role":"user", "content":"匹配字段" }
     *                 ]
     * @return LLM 原始 JSON 字符串
     */
    public String chat(List<Map<String, Object>> messages) throws Exception {
        // 构建请求 JSON
        String jsonBody = String.format("{\"model\":\"%s\",\"messages\":%s}", modelName, JsonUtil.toJson(messages));

        RequestBody body = RequestBody.create( MediaType.parse("application/json; charset=utf-8"), jsonBody);
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Qwen API 请求失败，HTTP " + response.code());
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new RuntimeException("Qwen API 返回空响应");
            }
            String respStr = responseBody.string();
            log.debug("QwenClient resp: {}", respStr);
            return respStr;
        }
    }
    /**
     * 基础 chat 调用，返回 LLM 原始 JSON 字符串
     */
    public String chat1(List<Map<String, Object>> messages) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName); // ✅ 支持 yml 配置
            payload.put("messages", messages);

            String jsonBody = mapper.writeValueAsString(payload);
            log.debug("LLM request payload: {}", jsonBody);

            RequestBody body = RequestBody.create(JSON, jsonBody);

            Request.Builder reqBuilder = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .addHeader("Content-Type", "application/json");

            if (apiKey != null && !apiKey.trim().isEmpty()) {
                reqBuilder.addHeader("Authorization", "Bearer " + apiKey);
            }

            Request request = reqBuilder.build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() == null) {
                    log.error("LLM response body is null");
                    return null;
                }
                String respBody = response.body().string();
                if (!response.isSuccessful()) {
                    log.error("LLM returned non-success: code={} body={}", response.code(), respBody);
                    return respBody;
                }
                return respBody;
            }
        } catch (IOException e) {
            log.error("call llm failed", e);
            return null;
        }
    }

    /**
     * 工具方法 - system + user prompt（方便调用）
     */
    public String chatWithSystemAndUserRaw(String systemPrompt, String userPrompt) {
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> sys = new HashMap<>();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);

        Map<String, Object> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", userPrompt);
        messages.add(user);

        return chat1(messages);
    }

    /**
     * ✅ 自动解析 choices[0].message.content
     * 如果接口格式不符合预期，返回原始字符串
     */
    public String chatAndExtractContent(String systemPrompt, String userPrompt) {
        String raw = chatWithSystemAndUserRaw(systemPrompt, userPrompt);
        if (raw == null) {
            return null;
        }
        try {
            JsonNode root = mapper.readTree(raw);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract content from LLM response, return raw text instead", e);
        }
        return raw; // 返回原始数据作为 fallback
    }
}

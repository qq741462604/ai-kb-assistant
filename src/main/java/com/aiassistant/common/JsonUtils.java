package com.aiassistant.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> List<T> extractJsonArray(String rawText, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        if (rawText == null || rawText.isEmpty()) return result;
        int start = rawText.indexOf('[');
        int end = rawText.lastIndexOf(']');
        if (start >= 0 && end > start) {
            String jsonArrayStr = rawText.substring(start, end + 1);
            try {
                result = MAPPER.readValue(jsonArrayStr,
                        MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}

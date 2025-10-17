package com.aiassistant.service;

import com.aiassistant.domain.CanonicalField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopNMatcherService {

    private static final int N = 5; // Top5 候选

    public List<CanonicalField> matchTopN(String input, List<CanonicalField> kbFields) {
        return kbFields.stream()
                .sorted((a, b) -> score(b, input) - score(a, input))
                .limit(N)
                .collect(Collectors.toList());
    }

    /**
     * 字段评分逻辑，可以扩展
     */
    private int score(CanonicalField field, String input) {
        String canonical = field.getCanonicalField();
        List<String> aliases = field.getAliases() != null ? field.getAliases() : new ArrayList<>();

        // 完全等于
        if (canonical.equalsIgnoreCase(input)) return 100;

        // 别名命中
        if (aliases.stream().anyMatch(a -> a.equalsIgnoreCase(input))) return 80;

        // 模糊包含
        if (canonical.toLowerCase().contains(input.toLowerCase())) return 60;

        // 字符串相似度（编辑距离）
        int distance = StringUtils.getLevenshteinDistance(input.toLowerCase(), canonical.toLowerCase());
        int maxLen = Math.max(input.length(), canonical.length());
        int similarity = (int) ((1 - (double) distance / maxLen) * 40); // 最大40分

        return similarity;
    }
}

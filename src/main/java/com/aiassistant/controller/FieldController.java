package com.aiassistant.controller;

import com.aiassistant.domain.CanonicalField;
import com.aiassistant.domain.KbPending;
import com.aiassistant.domain.LlmMatchResult;
import com.aiassistant.service.CanonicalFieldService;
import com.aiassistant.service.KbPendingService;
import com.aiassistant.service.LlmService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/field")
public class FieldController {

    @Resource
    private CanonicalFieldService canonicalFieldService;

    @Resource
    private KbPendingService kbPendingService;

    @Resource
    private LlmService llmService;

    @PostMapping("/aiMatch")
    public List<KbPending> aiMatch(@RequestBody List<String> fields) {
        List<CanonicalField> canonicalFields = canonicalFieldService.listAllCanonicalFields();
        List<KbPending> pendingList = new ArrayList<>();

        for (String originalField : fields) {
            List<LlmMatchResult> topMatches = llmService.topNMatch(originalField, canonicalFields, 3);
            if (!topMatches.isEmpty()) {
                LlmMatchResult best = topMatches.get(0);
                KbPending pending = new KbPending();
                pending.setOriginalField(originalField);
                pending.setAiCanonicalField(best.getCanonicalField());
                pending.setCanonicalFieldDescription(best.getDescription());
                pending.setAliases(best.getAliases());
                pending.setConfidence(best.getConfidence());
                pending.setReason(best.getReason());
                pending.setStatus("PENDING");

                kbPendingService.addPendingIfNotExists(pending);
                pendingList.add(pending);
            }
        }
        return pendingList;
    }




    @PostMapping("/pending/autoReviewTopN")
    public String autoReviewTopN(@RequestParam(defaultValue = "10") int topN) {
        kbPendingService.asyncAutoReview();
        return "异步 Top " + topN + " 自动审核已触发";
    }

}
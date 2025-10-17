package com.aiassistant.controller;

import com.aiassistant.domain.KbPending;
import com.aiassistant.domain.PageResult;
import com.aiassistant.domain.PendingStats;
import com.aiassistant.service.KbInfoService;
import com.aiassistant.service.KbPendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/field/pending")
@RequiredArgsConstructor
public class KbPendingController {

    private final KbPendingService pendingService;
    private final KbInfoService kbInfoService;

    @GetMapping("/page")
    public PageResult<KbPending> page(@RequestParam int pageNum, @RequestParam int pageSize) {
        return pendingService.selectPendingPage(pageNum, pageSize);
    }


    @PostMapping("/approve")
    public boolean approve(@RequestBody KbPending pending) {
        // 1. 更新 pending 状态及字段
        pending.setStatus("APPROVED");
        pending.setUpdateTime(new Date());
        pendingService.updatePending(pending); // 直接传整个对象

        // 2. 写入 kb_info
        kbInfoService.insertKbInfo(
                pending.getAiCanonicalField(),
                pending.getCanonicalFieldDescription(),
                pending.getAliases()
        );

        return true;
    }


    @PostMapping("/reject")
    public boolean reject(@RequestParam Long id) {
        return pendingService.reject(id);
    }

    @PostMapping("/batchApprove")
    public boolean batchApprove(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            KbPending p = pendingService.getById(id);
            approve(p);
        }
        return true;
    }

    @PostMapping("/batchReject")
    public boolean batchReject(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            reject(id);
        }
        return true;
    }

    // PendingController.java

    @PostMapping("/autoApprove")
    public ResponseEntity<?> autoApprove() {
        int approvedCount = pendingService.autoApprove();
        return ResponseEntity.ok("Auto-approved " + approvedCount + " records.");
    }

    // Manually trigger autoReview (Top N) -- synchronous
    @PostMapping("/autoReview")
    public int autoReview(@RequestParam(defaultValue = "50") int topN) {
        return pendingService.autoReviewTopN(topN);
    }

    // Stats endpoint
    @GetMapping("/stats")
    public PendingStats stats() {
        return pendingService.stats();
    }
}

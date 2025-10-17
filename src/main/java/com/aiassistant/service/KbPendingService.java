package com.aiassistant.service;

import com.aiassistant.domain.KbPending;
import com.aiassistant.domain.PageResult;
import com.aiassistant.domain.PendingStats;

import java.util.List;

public interface KbPendingService {
    List<KbPending> listPending();
    KbPending getById(Long id);
    boolean approve(Long id);
    boolean reject(Long id);
    // 新增：写入 pending，如果不存在
    boolean addPendingIfNotExists(KbPending pending);
    // 新增分页查询
    PageResult<KbPending> selectPendingPage(int pageNum, int pageSize);
    // 自动审核
    void autoReviewPending();
    void autoReviewPendingTopN(int topN);
    void asyncAutoReview();

    // --- 新增方法 ---
    // 自动审核
    int autoReviewTopN(int topN); // 返回自动通过数量
    // 统计
    PendingStats stats();
    boolean updatePending(KbPending pending);        // 保存前端编辑
    boolean batchApprove(List<Long> ids);            // 批量通过（用 DB 的当前值）
    int autoApprove();
}

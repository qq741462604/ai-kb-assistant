package com.aiassistant.service.impl;

import com.aiassistant.domain.KbInfo;
import com.aiassistant.domain.KbPending;
import com.aiassistant.domain.PageResult;
import com.aiassistant.domain.PendingStats;
import com.aiassistant.mapper.KbInfoMapper;
import com.aiassistant.mapper.KbPendingMapper;
import com.aiassistant.service.KbInfoService;
import com.aiassistant.service.KbPendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class KbPendingServiceImpl implements KbPendingService {

    @Resource
    private KbPendingMapper kbPendingMapper;

    @Resource
    private KbInfoMapper kbInfoMapper;

    @Resource
    private KbInfoService kbInfoService;

    @Value("${kb.auto-approve-threshold:0.9}")
    private double autoApproveThreshold;
    @Value("${kb.auto-approve-enabled:true}")
    private boolean autoApproveEnabled;

    @Value("${kb.auto-approve-top-n:50}")
    private int autoApproveTopN;

    @Override
    public List<KbPending> listPending() {
        return kbPendingMapper.selectAllPending();
    }

    @Override
    public KbPending getById(Long id) {
        return kbPendingMapper.selectById(id);
    }

//    @Override
//    public boolean approve(Long id) {
//        KbPending pending = kbPendingMapper.selectById(id);
//        if (pending == null) return false;
//        if (!"PENDING".equals(pending.getStatus()) && !"AUTO_APPROVED".equals(pending.getStatus())) {
//            // 只允许对 PENDING 或 AUTO_APPROVED 进行人工确认
//            // 也允许在 AUTO_APPROVED 情况下人工再次确认（业务可调整）
//        }
//        // 防重复：当 kb_info 中已存在 canonicalField 时仍标记为 APPROVED
//        if (!kbInfoService.existsByCanonicalField(pending.getAiCanonicalField())) {
//            kbInfoService.insertKbInfo(pending.getAiCanonicalField(), pending.getCanonicalFieldDescription(), pending.getAliases());
//        }
//        pending.setStatus("APPROVED");
//        pending.setUpdateTime(new Date());
//        int rows = kbPendingMapper.updateStatus(pending.getId(), pending.getStatus());
//        return rows > 0;
//    }

    @Override
    public boolean approve(Long id) {
        KbPending p = kbPendingMapper.selectById(id);
        if (p == null) return false;
        // if not exist in kb_info then insert
        if (kbInfoMapper.countByCanonicalField(p.getAiCanonicalField()) == 0) {
            KbInfo info = new KbInfo();
            info.setCanonicalField(p.getAiCanonicalField());
            info.setDescription(p.getCanonicalFieldDescription());
            info.setAliases(p.getAliases() != null ? String.join(",", p.getAliases()) : "");
            info.setCreateTime(new Date());
            info.setUpdateTime(new Date());
            kbInfoMapper.insert(info);
        }
        p.setStatus("APPROVED");
        p.setAutoApproved(false);
        p.setApproveTime(new Date());
        kbPendingMapper.updatePending(p);
        return true;
    }

    @Override
    public boolean reject(Long id) {
        KbPending pending = kbPendingMapper.selectById(id);
        if (pending == null) return false;
        pending.setStatus("REJECTED");
        pending.setUpdateTime(new Date());
        return kbPendingMapper.updateStatus(pending.getId(), pending.getStatus()) > 0;
    }

    @Override
    public boolean addPendingIfNotExists(KbPending pending) {
        int count = kbPendingMapper.countByOriginalAndAiField(pending.getOriginalField(), pending.getAiCanonicalField());
        if (count > 0) return false;
        pending.setStatus("PENDING");
        Date now = new Date();
        pending.setCreateTime(now);
        pending.setUpdateTime(now);
        kbPendingMapper.insert(pending);
        return true;
    }

//    @Override
//    public List<KbPending> selectPendingPage(int pageNum, int pageSize) {
//        int offset = (pageNum - 1) * pageSize;
//        return kbPendingMapper.selectPendingPage(pageSize, offset);
//    }

    public PageResult<KbPending> selectPendingPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<KbPending> list = kbPendingMapper.selectPendingPage(pageSize, offset);
        long total = kbPendingMapper.countPending();
        return new PageResult<>(list, total);
    }

//    // 新增：分页查询
//    public PageResult<KbPending> pagePending(int page, int size, String status, String q) {
//        int offset = (page - 1) * size;
//        List<KbPending> list = kbPendingMapper.selectPendingPage(status, offset, size, q);
//        int total = kbPendingMapper.countByStatusAndQuery(status, q);
//        return new PageResult<>(total, page, size, list);
//    }

    @Override
    public void autoReviewPending() {
        List<KbPending> pendingList = kbPendingMapper.listPending();
        for (KbPending p : pendingList) {
            // 简单示例：confidence >= 0.9 自动通过
            if (p.getConfidence() != null && p.getConfidence() >= 0.9) {
                // 写入 kb_info
                if (!kbInfoService.existsByCanonicalField(p.getAiCanonicalField())) {
                    kbInfoService.insertKbInfo(p.getAiCanonicalField(), p.getCanonicalFieldDescription(), p.getAliases());
                }
                // 更新状态
                kbPendingMapper.updateStatus(p.getId(), "APPROVED");
            }
        }
    }

    @Override
    public void autoReviewPendingTopN(int topN) {
        // 取所有 PENDING 并按 confidence 降序
        List<KbPending> pendingList = kbPendingMapper.listPending();
        pendingList.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));

        int count = 0;
        for (KbPending p : pendingList) {
            if (count >= topN) break;

            if (p.getConfidence() != null && p.getConfidence() >= 0.9) {
                if (!kbInfoService.existsByCanonicalField(p.getAiCanonicalField())) {
                    kbInfoService.insertKbInfo(p.getAiCanonicalField(), p.getCanonicalFieldDescription(), p.getAliases());
                }
                kbPendingMapper.updateStatus(p.getId(), "APPROVED");
                count++;
            }
        }
    }


    @Async
    @Override
    public void asyncAutoReview() {
        autoReviewPendingTopN(10); // 默认 Top 10 自动审核
    }

    @Override
    public boolean updatePending(KbPending pending) {
        if (pending == null || pending.getId() == null) return false;
        // 只允许更新部分字段（aiCanonicalField, canonicalFieldDescription, aliases, confidence, reason）
        pending.setUpdateTime(new Date());
        int rows = kbPendingMapper.updatePending(pending);
        return rows > 0;
    }



    @Override
    public boolean batchApprove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return false;
        for (Long id : ids) {
            approve(id);
        }
        return true;
    }

    @Override
    public int autoApprove() {
        List<KbPending> pendingList = kbPendingMapper.selectPendingByConfidence(0.9);
        int count = 0;

        for (KbPending item : pendingList) {
            // 如果 kb_info 中没有这个字段，则插入
            if (!kbInfoService.existsByCanonicalField(item.getAiCanonicalField())) {
                kbInfoService.insertKbInfo(item.getAiCanonicalField(), item.getCanonicalFieldDescription(), item.getAliases());
            }

            kbPendingMapper.updateAutoApproveStatus(item.getId());
            count++;
        }
        return count;
    }

    /**
     * 自动审核 Top N（按 confidence 降序）。返回自动通过的数量。
     */
    @Override
    public int autoReviewTopN(int topN) {
        if (!autoApproveEnabled) return 0;
        int limit = Math.min(topN, autoApproveTopN);
        List<KbPending> list = kbPendingMapper.selectPendingOrderedByConfidence(limit, 0);
        int autoCount = 0;
        for (KbPending p : list) {
            if (p.getConfidence() != null && p.getConfidence() >= autoApproveThreshold) {
                // 防重复
                if (kbInfoMapper.countByCanonicalField(p.getAiCanonicalField()) == 0) {
                    KbInfo info = new KbInfo();
                    info.setCanonicalField(p.getAiCanonicalField());
                    info.setDescription(p.getCanonicalFieldDescription());
                    info.setAliases(p.getAliases() != null ? String.join(",", p.getAliases()) : "");
                    info.setCreateTime(new Date());
                    info.setUpdateTime(new Date());
                    kbInfoMapper.insert(info);
                }
                p.setStatus("AUTO_APPROVED");
                p.setAutoApproved(true);
                p.setApproveTime(new Date());
                p.setUpdateTime(new Date());
                kbPendingMapper.updatePending(p);
                autoCount++;
            } else {
                // 因为按 confidence 降序，低于阈值则后续大多数会低于阈值，可继续或 break
                // break;
            }
        }
        return autoCount;
    }

    @Override
    public PendingStats stats() {
        PendingStats s = new PendingStats();
        s.setTotalPending(kbPendingMapper.countPending());
        // 简单的计数查询：你可以新增 mapper 方法或 SQL 统计状态
        // 这里举例：SELECT COUNT(*) FROM kb_pending WHERE status='APPROVED' 等
        // 假设你已经在 mapper 实现了以下方法：
        s.setTotalApproved(kbPendingMapper.countByStatus("APPROVED"));
        s.setTotalRejected(kbPendingMapper.countByStatus("REJECTED"));
        s.setTotalAutoApproved(kbPendingMapper.countByStatus("AUTO_APPROVED"));
        return s;
    }
}


package com.aiassistant.mapper;

import com.aiassistant.domain.KbPending;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KbPendingMapper {
    List<KbPending> listPending();
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    List<KbPending> selectAllPending();

    KbPending selectById(@Param("id") Long id);

    int update(KbPending pending);

    int countByOriginalAndAiField(@Param("originalField") String originalField,
                                  @Param("aiCanonicalField") String aiCanonicalField);

    int insert(KbPending pending);

    // 分页查询（返回 pageSize 条，从 offset 开始）
    List<KbPending> selectPendingPage(@Param("limit") int limit, @Param("offset") int offset);
    int countByStatusAndQuery(@Param("status") String status, @Param("q") String q);

    int updatePending(KbPending kbPending);

    long countByStatus(@Param("status") String status);
    @Select("SELECT COUNT(*) FROM kb_pending WHERE status='PENDING'")
    long countPending();
    List<KbPending> selectPendingByConfidence(@Param("threshold") double threshold);
    int updateAutoApproveStatus(@Param("id") Long id);
    // 可重用：按 confidence 降序取若干
    List<KbPending> selectPendingOrderedByConfidence(@Param("limit") int limit, @Param("offset") int offset);
}

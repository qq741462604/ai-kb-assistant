package com.aiassistant.mapper;

import com.aiassistant.domain.KbInfo;
import com.aiassistant.domain.KbPending;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KbInfoMapper {
//    KbInfo getByCanonicalField(@Param("canonicalField") String canonicalField);
    int countByCanonicalField(@Param("canonicalField") String canonicalField);
    int insert(KbInfo info);
}
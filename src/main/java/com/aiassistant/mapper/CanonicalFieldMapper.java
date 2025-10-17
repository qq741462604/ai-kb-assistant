package com.aiassistant.mapper;

import com.aiassistant.domain.CanonicalField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CanonicalFieldMapper {

    List<CanonicalField> selectAll();

    CanonicalField selectByCanonicalName(@Param("canonicalName") String canonicalName);

    int insert(CanonicalField field);

    int update(CanonicalField field);

    List<CanonicalField> listAll();
}
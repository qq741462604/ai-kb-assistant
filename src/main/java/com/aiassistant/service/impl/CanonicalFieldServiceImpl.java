package com.aiassistant.service.impl;

import com.aiassistant.domain.CanonicalField;
import com.aiassistant.mapper.CanonicalFieldMapper;
import com.aiassistant.service.CanonicalFieldService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CanonicalFieldServiceImpl implements CanonicalFieldService {

    @Resource
    private CanonicalFieldMapper canonicalFieldMapper;

    @Override
    public List<CanonicalField> listAllCanonicalFields() {
        return canonicalFieldMapper.listAll();
    }
}

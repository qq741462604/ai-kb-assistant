package com.aiassistant.service.impl;

import com.aiassistant.domain.KbInfo;
import com.aiassistant.domain.KbPending;
import com.aiassistant.mapper.KbInfoMapper;
import com.aiassistant.service.KbInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class KbInfoServiceImpl implements KbInfoService {

    @Resource
    private KbInfoMapper kbInfoMapper;

    @Override
    public boolean existsByCanonicalField(String canonicalField) {
        return kbInfoMapper.countByCanonicalField(canonicalField) > 0;
    }

    @Override
    public boolean insertKbInfo(String canonicalField, String description, List<String> aliases) {
        if (existsByCanonicalField(canonicalField)) return false;
        KbInfo info = new KbInfo();
        info.setCanonicalField(canonicalField);
        info.setDescription(description);
        info.setAliases(aliases != null ? String.join(",", aliases) : "");
        info.setCreateTime(new Date());
        info.setUpdateTime(new Date());
        return kbInfoMapper.insert(info) > 0;
    }
}


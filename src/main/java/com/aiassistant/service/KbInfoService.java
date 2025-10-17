package com.aiassistant.service;

import com.aiassistant.domain.KbPending;

import java.util.List;

public interface KbInfoService {
    boolean existsByCanonicalField(String canonicalField);
    boolean insertKbInfo(String canonicalField, String description, List<String> aliases);
}

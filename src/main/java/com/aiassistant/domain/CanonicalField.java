package com.aiassistant.domain;

import lombok.Data;

import java.util.List;

@Data
public class CanonicalField {
    private Long id;
    private String canonicalField;
    private String description;
    private List<String> aliases;
}

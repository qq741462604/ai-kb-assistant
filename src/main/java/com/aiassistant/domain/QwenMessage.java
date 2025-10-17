package com.aiassistant.domain;

import lombok.Data;

@Data
public class QwenMessage {
    private String role;
    private String content;
}
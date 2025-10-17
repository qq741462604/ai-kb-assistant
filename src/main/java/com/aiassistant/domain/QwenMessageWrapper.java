package com.aiassistant.domain;

import lombok.Data;

@Data
public class QwenMessageWrapper {
    private QwenMessage message;
    private String finish_reason;
    private Integer index;
}

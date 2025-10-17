package com.aiassistant.domain;

import lombok.Data;

@Data
public class PendingStats {
    private long totalPending;
    private long totalApproved;
    private long totalRejected;
    private long totalAutoApproved;
}

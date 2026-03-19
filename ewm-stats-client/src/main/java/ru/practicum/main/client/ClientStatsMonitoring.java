package ru.practicum.main.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientStatsMonitoring {
    private final int successfulRequests;
    private final int failedRequests;
    private final int retryAttempts;
    private final String serverUrl;
}
package org.example.common.model;

public record FileEvent (String businessId, String productId, String filePath, String timestamp, String eventType) {
}

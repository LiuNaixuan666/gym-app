package com.liu.gymmanagement.dto;

public class NotificationRequest {
    private String message;
    private String type; // 例如：Maintenance、System 等

    // Getter 和 Setter
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}

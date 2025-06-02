package com.liu.gymmanagement.model;

import java.time.LocalDateTime;

public class ScanFeedback {
    private boolean success;
    private String message;
    private LocalDateTime time;

    public ScanFeedback(boolean success, String message, LocalDateTime time) {
        this.success = success;
        this.message = message;
        this.time = time;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTime() {
        return time;
    }
}

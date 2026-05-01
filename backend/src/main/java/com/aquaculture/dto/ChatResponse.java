package com.aquaculture.dto;

public class ChatResponse {
    private String content;
    private boolean success;
    private String error;

    public ChatResponse() {}

    public ChatResponse(String content, boolean success) {
        this.content = content;
        this.success = success;
    }

    public static ChatResponse success(String content) {
        return new ChatResponse(content, true);
    }

    public static ChatResponse error(String error) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

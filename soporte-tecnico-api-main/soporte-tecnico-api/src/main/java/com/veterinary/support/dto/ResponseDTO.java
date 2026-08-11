package com.veterinary.support.dto;

public class ResponseDTO<T> {
    private int responseCode;
    private String responseMessage;
    private T data;

    // Constructores
    public ResponseDTO(int responseCode, String responseMessage, T data) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.data = data;
    }

    public ResponseDTO(int responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    // Getters y Setters
    public int getResponseCode() { return responseCode; }
    public void setResponseCode(int responseCode) { this.responseCode = responseCode; }
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}

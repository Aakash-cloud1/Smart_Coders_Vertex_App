package com.example.quizapp.qrcode;

public class QrRequest {
    private String url;//link will be encoded into the QR code
    private Integer size = 300;//default QR size unless user provides one

    public QrRequest() {
    }

    public QrRequest(String url, Integer size) {
        this.url = url;
        this.size = size;
    }
    //getter for url
    public String getUrl() {
        return url;
    }
    //setter for url
    public void setUrl(String url) {
        this.url = url;
    }
    //getter for the QR code size
    public Integer getSize() {
        return size;
    }
    //setter for the QR code size
    public void setSize(Integer size) {
        this.size = size;
    }
}

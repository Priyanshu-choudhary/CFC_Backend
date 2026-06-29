package com.cfc.platform.Pojo.Posts;

public class HelperCode {
    private String templateCode;
    private String boilerCode;

    // No-args constructor
    public HelperCode() {
    }

    // All-args constructor
    public HelperCode(String templateCode, String boilerCode) {
        this.templateCode = templateCode;
        this.boilerCode = boilerCode;
    }

    // Getters and Setters
    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getBoilerCode() {
        return boilerCode;
    }

    public void setBoilerCode(String boilerCode) {
        this.boilerCode = boilerCode;
    }
}

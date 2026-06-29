package com.cfc.platform.DTO;

public class CFImportRequestDTO {

    private String url;

    public CFImportRequestDTO() {
    }

    public CFImportRequestDTO(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
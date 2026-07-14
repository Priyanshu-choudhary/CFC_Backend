package com.cfc.platform.DTO;

public class CFContestImportRequestDTO {

    private String url;
    private Integer startDelayMinutes;

    public CFContestImportRequestDTO() {
    }

    public CFContestImportRequestDTO(String url, Integer startDelayMinutes) {
        this.url = url;
        this.startDelayMinutes = startDelayMinutes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getStartDelayMinutes() {
        return startDelayMinutes;
    }

    public void setStartDelayMinutes(Integer startDelayMinutes) {
        this.startDelayMinutes = startDelayMinutes;
    }
}

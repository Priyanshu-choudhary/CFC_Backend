package com.cfc.platform.DTO;

public class MoveFileNodeRequest {

    private String newParentId;
    private Integer order;

    public MoveFileNodeRequest() {
    }

    public String getNewParentId() {
        return newParentId;
    }

    public void setNewParentId(String newParentId) {
        this.newParentId = newParentId;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}

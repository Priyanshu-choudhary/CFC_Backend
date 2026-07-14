package com.cfc.platform.DTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FileNodeResponse {

    private String id;
    private String name;
    private boolean folder;
    private String parentId;
    private String content;
    private Integer order;
    private Instant createdAt;
    private Instant updatedAt;
    private List<FileNodeResponse> children = new ArrayList<>();

    public FileNodeResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<FileNodeResponse> getChildren() {
        return children;
    }

    public void setChildren(List<FileNodeResponse> children) {
        this.children = children;
    }
}

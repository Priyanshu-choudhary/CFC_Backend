package com.cfc.platform.Pojo.Lecture;

import java.util.List;

public class RemoveHeadingsWapper {

    private List<String> headingsToRemove;
    private List<String> subHeadingsToRemove;

    // No-args constructor
    public RemoveHeadingsWapper() {
    }

    // Getters and Setters
    public List<String> getHeadingsToRemove() {
        return headingsToRemove;
    }

    public void setHeadingsToRemove(List<String> headingsToRemove) {
        this.headingsToRemove = headingsToRemove;
    }

    public List<String> getSubHeadingsToRemove() {
        return subHeadingsToRemove;
    }

    public void setSubHeadingsToRemove(List<String> subHeadingsToRemove) {
        this.subHeadingsToRemove = subHeadingsToRemove;
    }
}

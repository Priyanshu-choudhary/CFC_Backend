package com.example.WebSecurityExample.Pojo.Lecture;

import lombok.Data;

import java.util.List;

@Data
public class RemoveHeadingsWapper {

    private List<String> headingsToRemove;
    private List<String> subHeadingsToRemove;
}

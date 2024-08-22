package com.example.WebSecurityExample.CustomQuery.Lecture;

import com.example.WebSecurityExample.Pojo.Lecture.Lecture;

import java.util.Optional;

public interface CustomQueryLecture {
    Optional<Lecture> findLectureByUsernameAndTitle(String username, String lectureTitle);
}

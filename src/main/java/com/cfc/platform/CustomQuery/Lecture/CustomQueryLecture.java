package com.cfc.platform.CustomQuery.Lecture;

import com.cfc.platform.Pojo.Lecture.Lecture;

import java.util.Optional;

public interface CustomQueryLecture {
    Optional<Lecture> findLectureByUsernameAndTitle(String username, String lectureTitle);
}

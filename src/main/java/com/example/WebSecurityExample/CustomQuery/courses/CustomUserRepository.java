package com.example.WebSecurityExample.CustomQuery.courses;

import com.example.WebSecurityExample.Pojo.User;

import java.util.List;

public interface CustomUserRepository {
    List<User> findOfficialCoursesByName(String name, int skip , int limit);
}

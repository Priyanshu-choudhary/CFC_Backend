package com.cfc.platform.CustomQuery.courses;

import com.cfc.platform.Pojo.User;

import java.util.List;

public interface CustomUserRepository {
    List<User> findOfficialCoursesByName(String name, int skip , int limit);
}

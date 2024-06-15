package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.CourseRepo;
import com.example.WebSecurityExample.MongoRepo.PostRepo;
import com.example.WebSecurityExample.Pojo.Course;
import com.example.WebSecurityExample.Pojo.Posts;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.controller.CourseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private UserService userService;

    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    @Transactional
    public void createCourse(Course course, String inputUser) {
        try {
            // Fetch the user and their associated courses
            User myUser = userService.findByName(inputUser);
            List<Course> userCourses = myUser.getCourses();

            // Check if a course with the same title already exists in the user's courses
            for (Course existingCourse : userCourses) {
                if (existingCourse.getTitle().equalsIgnoreCase(course.getTitle())) {
                    // Course already exists, handle accordingly (e.g., throw an exception)
                    logger.error("Course with the same title already exists for this user.");
                    throw new RuntimeException("Course with the same title already exists for this user.");
                }
            }

            // Proceed to create a new course
            Course savedCourse = courseRepo.save(course);
            myUser.getCourses().add(savedCourse);
            userService.createUser(myUser); // Save the user to update the courses

        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("An error occurred while saving the entry", e);
        }

    }

    @Transactional
    public void deleteUserById(String id, String name) {
        try {
            User myuser = userService.findByName(name);
            boolean b = myuser.getCourses().removeIf(x -> x.getId().equals(id));
            if (b) {
                userService.createUser(myuser);
                courseRepo.deleteById(id);
            }
        } catch (Exception e) {

            System.out.println(e);
            throw new RuntimeException("Error occur while delete post", e);
        }
    }
}
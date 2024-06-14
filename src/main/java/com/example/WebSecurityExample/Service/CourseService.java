package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.CourseRepo;
import com.example.WebSecurityExample.MongoRepo.PostRepo;
import com.example.WebSecurityExample.Pojo.Course;
import com.example.WebSecurityExample.Pojo.Posts;
import com.example.WebSecurityExample.Pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {
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
            // Check if a course with the same title already exists
            Course existingCourse = courseRepo.findByTitle(course.getTitle());
            if (existingCourse != null) {
                // Course already exists, handle accordingly (e.g., throw an exception)
                throw new RuntimeException("Course with the same title already exists.");
            }

            // Proceed to create a new course
            User myUser = userService.findByName(inputUser);
            Course saved = courseRepo.save(course);
            myUser.getCourses().add(saved);
            userService.createUser(myUser); // save in user DB (creating ref)
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("An error occurred while saving an entry", e);
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
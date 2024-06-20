package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.CourseRepo;
import com.example.WebSecurityExample.MongoRepo.PostRepo;
import com.example.WebSecurityExample.MongoRepo.UserRepo;
import com.example.WebSecurityExample.Pojo.Course;
import com.example.WebSecurityExample.Pojo.Posts;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.controller.CourseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseRepo courseRepo;
@Autowired
private UserRepo userRepo;
    @Autowired
    private PostRepo postRepo;

    @Autowired
    private UserService userService;

    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    @Transactional
    public String createCourse(Course course, String inputUser) {
        try {
            // Fetch the user
            User myUser = userService.findByName(inputUser);

            // Check if a course with the same title already exists for this user
            Optional<Course> existingCourseOpt = myUser.getCourses().stream()
                    .filter(c -> c.getTitle().equalsIgnoreCase(course.getTitle()))
                    .findFirst();

            if (existingCourseOpt.isPresent()) {
                // Course already exists, return the existing course ID
                logger.info("Course with the same title already exists for this user. Returning existing course ID.");
                return existingCourseOpt.get().getId();
            } else {
                // Associate the course with the user
//                course.setUser(myUser);
                Course savedCourse = courseRepo.save(course);

                // Update user's course list
                myUser.getCourses().add(savedCourse);
                userService.createUser(myUser); // Save the user to update the courses

                // Return the new course ID
                return savedCourse.getId();
            }

        } catch (Exception e) {
            logger.error("An error occurred while saving the entry", e);
            throw new RuntimeException("An error occurred while saving the entry", e);
        }
    }

    public Course findByName(String courseName) {
        return courseRepo.findByTitle(courseName);
    }


    @Transactional
    public boolean deleteUserById(String id, String name) {
        try {
            User myuser = userService.findByName(name);
            boolean b = myuser.getCourses().removeIf(x -> x.getId().equals(id));
            if (b) {
                userService.createUser(myuser);
               return b;
            }
        } catch (Exception e) {

            System.out.println(e);
            return false;

        }
        return false;
    }


    @Transactional
    public Course updateCourse(String id, Course newCourse, String username) {
        try {
            logger.info("Updating course with ID {} for user {}", id, username);

            // Fetch user from service
            User user = userService.findByName(username);
            logger.info("Fetched user {} for updating course", username);

            // Find existing course
            Optional<Course> existingCourseOpt = courseRepo.findById(id);
            logger.info("Fetched course with ID {}", id);

            // Check if course exists
            if (existingCourseOpt.isPresent()) {
                Course existingCourse = existingCourseOpt.get();
                logger.info("Found existing course with ID {}", id);

                // Check if user owns the course
                if (user.getCourses().contains(existingCourse)) {
                    logger.debug("User {} owns course {}", username, existingCourse.getId());



                    int newUniqueQuestions = 0;

                    // Update completeQuestions if provided
                    if (newCourse.getCompleteQuestions() != null) {
                        List<String> currentCompleteQuestions = existingCourse.getCompleteQuestions();
                        if (currentCompleteQuestions == null) {
                            currentCompleteQuestions = new ArrayList<>();
                            logger.debug("Initialized new completeQuestions list");
                            // Update course progress
                            if (newCourse.getProgress() != null) {
                                Integer newProgress=+newCourse.getProgress()+existingCourse.getProgress();
                                existingCourse.setProgress(newProgress);
                                logger.info("Updated course progress to {}", newCourse.getProgress());
                            }
                        }
                        for (String questionId : newCourse.getCompleteQuestions()) {
                            if (!currentCompleteQuestions.contains(questionId)) {
                                currentCompleteQuestions.add(questionId);
                                newUniqueQuestions++;
                                logger.info("Added question ID {} to completeQuestions list", questionId);
                                logger.info("trying to increase the progress");
                                // Update course progress
                                if (newCourse.getProgress() != null) {
                                    Integer newProgress=+newCourse.getProgress()+existingCourse.getProgress();
                                    existingCourse.setProgress(newProgress);
                                    logger.info("Updated course progress to {}", newCourse.getProgress());
                                }
                            } else {
                                logger.debug("Question ID {} is already in completeQuestions list", questionId);
                            }
                        }
                        existingCourse.setCompleteQuestions(currentCompleteQuestions);
                        logger.info("Updated completeQuestions list to {}", currentCompleteQuestions);
                    }
                    existingCourse.setTotalQuestions(newCourse.getTotalQuestions());
                    // Update user's rating and course's rating if new unique questions are added
                    if (newUniqueQuestions > 0) {
                        if (user.getRating() == null) {
                            user.setRating( newCourse.getRating());
                            logger.info("Set user rating to {}",  newCourse.getRating());
                        } else {
                            user.setRating(user.getRating() + newCourse.getRating());
                            logger.info("Updated user rating by adding {}",  newCourse.getRating());
                        }

                        if (existingCourse.getRating() == null) {
                            existingCourse.setRating( newCourse.getRating());
                            logger.info("Set course rating to {}",  newCourse.getRating());
                        } else {
                            existingCourse.setRating(existingCourse.getRating() +  newCourse.getRating());
                            logger.info("Updated course rating by adding {}",  newCourse.getRating());
                        }

                        logger.info("User {} rating updated to {}", username, user.getRating());
                        logger.info("Course {} rating updated to {}", existingCourse.getId(), existingCourse.getRating());
                    }


                    // Save updated user
                    userRepo.save(user);
                    logger.debug("User {} updated successfully", username);

                    // Save updated course
                    Course updatedCourse = courseRepo.save(existingCourse);
                    logger.info("Course {} updated successfully", updatedCourse.getId());
                    return updatedCourse;
                } else {
                    logger.error("User {} does not own course {}", username, id);
                    throw new RuntimeException("Course does not belong to the user");
                }
            } else {
                logger.error("Course with ID {} not found", id);
                throw new RuntimeException("Course not found");
            }
        } catch (Exception e) {
            logger.error("Error updating course with ID {}", id, e);
            throw new RuntimeException("An error occurred while updating the course", e);
        }
    }



}
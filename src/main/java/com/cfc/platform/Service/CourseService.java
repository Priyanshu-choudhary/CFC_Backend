package com.cfc.platform.Service;

import com.cfc.platform.MongoRepo.CourseRepo;
import com.cfc.platform.MongoRepo.PostRepo;
import com.cfc.platform.MongoRepo.UserRepo;
import com.cfc.platform.Pojo.Course.Course;
import com.cfc.platform.Pojo.Course.CourseDTO.CourseDTO;
import com.cfc.platform.Pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PostRepo postRepo;

    @Autowired
    private UserService userService;

    // Paginated method to get all courses with specific fields
    public Page<CourseDTO> getAllCourses(int page, int size) {
        Page<Course> coursesPage = courseRepo.findAll(PageRequest.of(page, size));
        return coursesPage.map(this::convertToDTO);
    }

    // Get courses by userName
    public Page<CourseDTO> getCoursesByUserName(String userName, int page, int size) {
        Page<Course> coursesPage = courseRepo.findByUserName(userName, PageRequest.of(page, size));
        return coursesPage.map(this::convertToDTO);
    }

    // Convert Course to CourseDTO
    private CourseDTO convertToDTO(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getTitle(),
                course.getProgress(),
                course.getTotalQuestions(),
                course.getRating(),
                course.getImage(),
                course.getType(),
                course.getPermission());
    }

    public List<Course> getUserCourses(String username) {
        User users = userService.findByName(username);
        return users.getCourses();
    }

    public List<Course> getUserOneCourses(String username, int skip, int limit) {
        List users = userRepo.findOfficialCoursesByName(username, skip, limit);
        // System.out.println("correct method");
        return users;
    }

    public Optional<Course> getUserCoursesByID(String ID) {
        Optional<Course> userOpt = courseRepo.findById(ID);
        return userOpt;
    }

    // @Transactional
    public String createCourse(Course course, String inputUser) {
        try {
            // Fetch the user
            User myUser = userService.findByName(inputUser);

            // Check if a course with the same title already exists for this user
            Optional<Course> existingCourseOpt = myUser.getCourses().stream()
                    .filter(c -> c.getTitle().equalsIgnoreCase(course.getTitle()))
                    .findFirst();

            if (existingCourseOpt.isPresent()) {
                log.info("createCourse: course '{}' already exists for user {}", course.getTitle(), inputUser);
                return existingCourseOpt.get().getId();
            } else {
                // Associate the course with the user
                // course.setUser(myUser);
                Course savedCourse = courseRepo.save(course);

                // Update user's course list
                myUser.getCourses().add(savedCourse);
                userService.createUser(myUser); // Save the user to update the courses

                // Return the new course ID
                return savedCourse.getId();
            }

        } catch (Exception e) {
            log.error("createCourse error for user {}: {}", inputUser, e.getMessage());
            throw new RuntimeException("An error occurred while saving the entry", e);
        }
    }

    public Course findCourseByTitle(String courseTitle) {
        return courseRepo.findByTitle(courseTitle);
    }

    public Course findCourseByTitleAndUser(String courseTitle, User user) {
        return courseRepo.findByTitle(courseTitle);
    }

    // @Transactional
    public boolean deleteUserById(String id, String name) {
        try {
            User myuser = userService.findByName(name);
            boolean b = myuser.getCourses().removeIf(x -> x.getId().equals(id));
            if (b) {
                userService.createUser(myuser);
                return b;
            }
        } catch (Exception e) {
            log.error("deleteUserById error for id={}, user={}: {}", id, name, e.getMessage());
            return false;
        }
        return false;
    }

    // @Transactional
    public Course updateCourse(String id, Course newCourse, String username) {
        log.info("updateCourse: id={}, user={}", id, username);
        try {
            User user = userService.findByName(username);

            Optional<Course> existingCourseOpt = courseRepo.findById(id);
            if (existingCourseOpt.isPresent()) {
                Course existingCourse = existingCourseOpt.get();
                if (user.getCourses().contains(existingCourse)) {

                    if (newCourse.getPermission() != null) {
                        existingCourse.setPermission(newCourse.getPermission());

                    }
                    if (newCourse.getUserName() != null) {
                        existingCourse.setUserName(newCourse.getUserName());

                    }
                    if (newCourse.getTitle() != null) {
                        existingCourse.setTitle(newCourse.getTitle());
                    }

                    if (newCourse.getTotalQuestions() != null) {
                        existingCourse.setTotalQuestions(newCourse.getTotalQuestions());

                    }

                    if (newCourse.getDescription() != null) {
                        existingCourse.setDescription(newCourse.getDescription());

                    }
                    if (newCourse.getLanguage() != null) {
                        existingCourse.setLanguage(newCourse.getLanguage());

                    }
                    if (newCourse.getImage() != null) {
                        existingCourse.setImage(newCourse.getImage());

                    }

                    int newUniqueQuestions = 0;

                    // Update completeQuestions if provided
                    if (newCourse.getCompleteQuestions() != null) {
                        List<String> currentCompleteQuestions = existingCourse.getCompleteQuestions();
                        if (currentCompleteQuestions == null) {
                            currentCompleteQuestions = new ArrayList<>();
                            existingCourse.setCompleteQuestions(currentCompleteQuestions);
                        }
                        for (String questionId : newCourse.getCompleteQuestions()) {
                            if (!currentCompleteQuestions.contains(questionId)) {
                                currentCompleteQuestions.add(questionId);
                                newUniqueQuestions++;
                            }
                        }
                        existingCourse.setCompleteQuestions(currentCompleteQuestions);
                    }

                    if (newUniqueQuestions > 0) {
                        if (newCourse.getProgress() != null) {
                            Integer newProgress = newCourse.getProgress()
                                    + (existingCourse.getProgress() != null ? existingCourse.getProgress() : 0);
                            existingCourse.setProgress(newProgress);
                        }

                        if (user.getRating() == null) {
                            user.setRating(newCourse.getRating());
                        } else {
                            user.setRating(user.getRating() + newCourse.getRating());
                        }

                        if (existingCourse.getRating() == null) {
                            existingCourse.setRating(newCourse.getRating());
                        } else {
                            existingCourse.setRating(existingCourse.getRating() + newCourse.getRating());
                        }
                    }

                    userRepo.save(user);
                    Course updatedCourse = courseRepo.save(existingCourse);
                    log.info("updateCourse: saved course {}", updatedCourse.getId());
                    return updatedCourse;
                } else {
                    log.error("updateCourse: user {} does not own course {}", username, id);
                    throw new RuntimeException("Course does not belong to the user");
                }
            } else {
                log.error("updateCourse: course {} not found", id);
                throw new RuntimeException("Course not found");
            }
        } catch (Exception e) {
            log.error("updateCourse error for id={}: {}", id, e.getMessage());
            throw new RuntimeException("An error occurred while updating the course", e);
        }
    }

}
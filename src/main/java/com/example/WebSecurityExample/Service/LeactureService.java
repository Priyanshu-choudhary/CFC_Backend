package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.*;
import com.example.WebSecurityExample.Pojo.Lecture.Lecture;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.controller.LectureController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LeactureService {
//    private static final // logger // logger = LoggerFactory.getLogger(Lecture.class);
// logger // logger = LoggerFactory.getLogger(this.getClass());
private static final Logger logger = LoggerFactory.getLogger(LeactureService.class);

    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PostRepo postRepo;

    @Autowired
    private ContestRepo contestRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private LectureRepo lectureRepo;


    public List<Lecture> getAllLecture() {
        return lectureRepo.findAll();
    }


    public List<Lecture> getUserLecture(String username) {
        User users = userService.findByName(username);
        return users.getLectures();

    }
    public Optional<Lecture> getUserLectureWithTitle(String username, String lectureTitle) {
        User users = userService.findByName(username);
//        logger.warn(" found with username '{}'", users);

        return users.getLectures().stream()
                .filter(l -> l.getTitle().equals(lectureTitle))
                .findFirst();
    }

    public Optional<Lecture> getLectureByUserAndTitle(String username, String lectureTitle) {
        return lectureRepo.findLectureByUsernameAndTitle(username, lectureTitle);
    }

    public Optional<Lecture> getUserLectureByID(String ID) {
        Optional<Lecture> userOpt = lectureRepo.findById(ID);
       return userOpt;
    }

    public String createLecture(Lecture lecture, String inputUser) {
        try {
            // Fetch the user
            User myUser = userService.findByName(inputUser);

            // Ensure the user name is not null or empty
            if (myUser.getName() == null || myUser.getName().isEmpty()) {
                throw new IllegalArgumentException("User name cannot be null or empty");
            }

            // Check if a Lecture with the same title already exists for this user
            Optional<Lecture> existingLectureOpt = myUser.getLectures().stream()
                    .filter(c -> c.getId().equalsIgnoreCase(lecture.getId()))
                    .findFirst();

            if (existingLectureOpt.isPresent()) {
                // Lecture already exists, return the existing course ID
                logger.info("Lecture with the same title already exists for this user. Returning existing course ID.");
                return existingLectureOpt.get().getId();
            } else {
                // Associate the Lecture with the user
                // logger.info("Try to save Lecture ");
                Lecture savedLecture = lectureRepo.save(lecture);
                // logger.info("Lecture saving done ");

                // Update user's Lecture list
                // logger.info("Update user's contest list ");
                myUser.getLectures().add(savedLecture);
                // logger.info("Done Update ");

                // logger.info("Create new user ");
                userService.createUser(myUser); // Save the user to update the contests
                // logger.info("Done ");

                // Return the new Lecture ID
                return savedLecture.getId();
            }
        } catch (Exception e) {
            // logger.error("An error occurred while saving the entry of Lecture", e);
            throw new RuntimeException("An error occurred while saving the entry of Lecture", e);
        }
    }



    public boolean deleteLectureById(String id, String name) {
        try {
            User myuser = userService.findByName(name);
            boolean b = myuser.getLectures().removeIf(x -> x.getId().equals(id));
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

    public Lecture updateLecture(String id, Lecture newLecture, String username) {
        try {
            // Fetch user from service
            User user = userService.findByName(username);

            // Find existing lecture
            Optional<Lecture> existingLectureOpt = lectureRepo.findById(id);

            // Check if lecture exists
            if (existingLectureOpt.isPresent()) {
                Lecture existingLecture = existingLectureOpt.get();

                // Check if user owns the lecture
                if (user.getLectures().contains(existingLecture)) {

                    // Update title and subtitle if provided
                    if (newLecture.getTitle() != null && !newLecture.getTitle().isEmpty()) {
                        existingLecture.setTitle(newLecture.getTitle());
                    }

                    if (newLecture.getSubtitle() != null && !newLecture.getSubtitle().isEmpty()) {
                        existingLecture.setSubtitle(newLecture.getSubtitle());
                    }

                    // Update sections if provided
                    if (newLecture.getSections() != null && !newLecture.getSections().isEmpty()) {
                        // Iterate over new sections and update existing ones or add new sections
                        for (Lecture.Section newSection : newLecture.getSections()) {
                            boolean sectionFound = false;

                            // Check if the section exists in the existing lecture
                            for (Lecture.Section existingSection : existingLecture.getSections()) {
                                if (existingSection.getId().equals(newSection.getId())) {
                                    // Update section details
                                    existingSection.setHeading(newSection.getHeading() != null ? newSection.getHeading() : existingSection.getHeading());
                                    existingSection.setContent(newSection.getContent() != null ? newSection.getContent() : existingSection.getContent());
                                    sectionFound = true;
                                    break;
                                }
                            }

                            // If section is new, add it to the list
                            if (!sectionFound) {
                                existingLecture.getSections().add(newSection);
                            }
                        }
                    }

                    // Save and return the updated lecture
                    return lectureRepo.save(existingLecture);

                } else {
                    throw new RuntimeException("User does not own the lecture");
                }
            } else {
                throw new RuntimeException("Lecture not found");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace to understand the error
            throw new RuntimeException("An error occurred while updating the Lecture: " + e.getMessage(), e);
        }
    }


}
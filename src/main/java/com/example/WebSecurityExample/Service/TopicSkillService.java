package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.*;
import com.example.WebSecurityExample.Pojo.TopicWiseSkills.TopicSkill;
import com.example.WebSecurityExample.Pojo.TopicWiseSkills.TopicSkill;
import com.example.WebSecurityExample.Pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TopicSkillService {
    //    private static final // logger // logger = LoggerFactory.getLogger(TopicSkill.class);
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
    private TopicSkillRepo topicSkillRepo;
    
    public List<TopicSkill> getAllTopicSkill() {
        return topicSkillRepo.findAll();
    }


    public List<TopicSkill> getUserTopicSkill(String username) {
        User users = userService.findByName(username);
        return users.getTopicSkill();

    }
    public Optional<TopicSkill> getUserTopicSkillWithTitle(String username, String TopicName) {
        User users = userService.findByName(username);
//        logger.warn(" found with username '{}'", users);

        return users.getTopicSkill().stream()
                .filter(l -> l.getName().equals(TopicName))
                .findFirst();
    }

//    public Optional<TopicSkill> getLectureByUserAndTitle(String username, String lectureTitle) {
//        return topicSkillRepo.findLectureByUsernameAndTitle(username, lectureTitle);
//    }

    public Optional<TopicSkill> getUserTopicSkillByID(String ID) {
        Optional<TopicSkill> userOpt = topicSkillRepo.findById(ID);
        return userOpt;
    }

    public String createTopicSkill(TopicSkill topicSkill, String inputUser) {
        try {
            // Fetch the user
            User myUser = userService.findByName(inputUser);

            // Ensure the user name is not null or empty
            if (myUser.getName() == null || myUser.getName().isEmpty()) {
                throw new IllegalArgumentException("User name cannot be null or empty");
            }

            // Check if a TopicSkill with the same title already exists for this user
            Optional<TopicSkill> existingTopicSkillOpt = myUser.getTopicSkill().stream()
                    .filter(c -> c.getId().equalsIgnoreCase(topicSkill.getId()))
                    .findFirst();

            if (existingTopicSkillOpt.isPresent()) {
                // TopicSkill already exists, return the existing course ID
                logger.info("TopicSkill with the same title already exists for this user. Returning existing course ID.");
                return existingTopicSkillOpt.get().getId();
            } else {
                // Associate the TopicSkill with the user
                // logger.info("Try to save TopicSkill ");
                TopicSkill savedTopicSkill = topicSkillRepo.save(topicSkill);
                // logger.info("TopicSkill saving done ");

                // Update user's TopicSkill list
                // logger.info("Update user's contest list ");
                myUser.getTopicSkill().add(savedTopicSkill);
                // logger.info("Done Update ");

                // logger.info("Create new user ");
                userService.createUser(myUser); // Save the user to update the contests
                // logger.info("Done ");

                // Return the new TopicSkill ID
                return savedTopicSkill.getId();
            }
        } catch (Exception e) {
            // logger.error("An error occurred while saving the entry of TopicSkill", e);
            throw new RuntimeException("An error occurred while saving the entry of TopicSkill", e);
        }
    }



    public boolean deleteTopicSkillById(String id, String name) {
        try {
            User myuser = userService.findByName(name);
            boolean b = myuser.getTopicSkill().removeIf(x -> x.getId().equals(id));
            topicSkillRepo.deleteById(id);
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

    public TopicSkill updateTopicSkill(String id, TopicSkill newTopicSkill, String username) {
        try {
            // Fetch user from service
            User user = userService.findByName(username);

            // Find existing TopicSkill
            Optional<TopicSkill> existingTopicSkillOpt = topicSkillRepo.findById(id);

            if (existingTopicSkillOpt.isPresent()) {
                TopicSkill existingTopicSkill = existingTopicSkillOpt.get();

                // Update the fields of the existing TopicSkill with the new values
                existingTopicSkill.setName(newTopicSkill.getName());
                existingTopicSkill.setChildren(newTopicSkill.getChildren());
                existingTopicSkill.setProblem(newTopicSkill.getProblem());

                // Save the updated TopicSkill back to the repository
                TopicSkill updatedTopicSkill = topicSkillRepo.save(existingTopicSkill);

                return updatedTopicSkill;
            } else {
                throw new RuntimeException("TopicSkill with ID " + id + " not found.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace to understand the error
            throw new RuntimeException("An error occurred while updating the TopicSkill: " + e.getMessage(), e);
        }
    }

}
package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.ContestRepo;
import com.example.WebSecurityExample.MongoRepo.CourseRepo;
import com.example.WebSecurityExample.MongoRepo.PostRepo;
import com.example.WebSecurityExample.MongoRepo.UserRepo;
import com.example.WebSecurityExample.Pojo.Contest;
import com.example.WebSecurityExample.Pojo.Course;
import com.example.WebSecurityExample.Pojo.Posts.Posts;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.controller.CourseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContestService {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

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

    public List<Contest> getAllContest() {
        return contestRepo.findAll();
    }


    public List<Contest> getUserContest(String username) {
        User users = userService.findByName(username);
        return users.getContests();
    }

    public Optional<Contest> getUserContestByID(String ID) {
        Optional<Contest> userOpt = contestRepo.findById(ID);
       return userOpt;
    }

    public String createContest(Contest contest, String inputUser) {
        try {
            // Fetch the user
            User myUser = userService.findByName(inputUser);

            // Ensure the user name is not null or empty
            if (myUser.getName() == null || myUser.getName().isEmpty()) {
                throw new IllegalArgumentException("User name cannot be null or empty");
            }

            // Check if a Contest with the same title already exists for this user
            Optional<Contest> existingContestOpt = myUser.getContests().stream()
                    .filter(c -> c.getNameOfContest().equalsIgnoreCase(contest.getNameOfContest()))
                    .findFirst();

            if (existingContestOpt.isPresent()) {
                // Contest already exists, return the existing course ID
                logger.info("Contest with the same title already exists for this user. Returning existing course ID.");
                return existingContestOpt.get().getId();
            } else {
                // Associate the contest with the user
                logger.info("Try to save contest ");
                Contest savedContest = contestRepo.save(contest);
                logger.info("Contest saving done ");

                // Update user's contest list
                logger.info("Update user's contest list ");
                myUser.getContests().add(savedContest);
                logger.info("Done Update ");

                logger.info("Create new user ");
                userService.createUser(myUser); // Save the user to update the contests
                logger.info("Done ");

                // Return the new contest ID
                return savedContest.getId();
            }
        } catch (Exception e) {
            logger.error("An error occurred while saving the entry of Contest", e);
            throw new RuntimeException("An error occurred while saving the entry of Contest", e);
        }
    }



    public boolean deleteContestById(String id, String name) {
        try {
            User myuser = userService.findByName(name);
            boolean b = myuser.getContests().removeIf(x -> x.getId().equals(id));
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


    public Contest updateContest(String id, Contest newContest, String username) {
        try {
            logger.info("Updating course with ID {} for user {}", id, username);

            // Fetch user from service
            User user = userService.findByName(username);
            logger.info("Fetched user {} for updating course", username);

            // Find existing course
            Optional<Contest> existingContestOpt = contestRepo.findById(id);
            logger.info("Fetched course with ID {}", id);

                // Check if course exists
                if (existingContestOpt.isPresent()) {
                    Contest existingContest = existingContestOpt.get();
                    logger.info("Found existing course with ID {}", id);

                    // Check if user owns the course
                    if (user.getContests().contains(existingContest)) {
                        logger.debug("User {} owns course {}", username, existingContest.getId());


                        existingContest.setNameOfContest(newContest.getNameOfContest() != null && !newContest.getNameOfContest().isEmpty() ? newContest.getNameOfContest() : existingContest.getNameOfContest());
                        existingContest.setNameOfOrganization(newContest.getNameOfOrganization() != null && !newContest.getNameOfOrganization().isEmpty() ? newContest.getNameOfOrganization() : existingContest.getNameOfOrganization());
                        existingContest.setDate(newContest.getDate() != null ? newContest.getDate() : existingContest.getDate());
                        existingContest.setDescription(newContest.getDescription() != null && !newContest.getDescription().isEmpty() ? newContest.getDescription() : existingContest.getDescription());
                        existingContest.setBannerImage(newContest.getBannerImage() != null && !newContest.getBannerImage().isEmpty() ? newContest.getBannerImage() : existingContest.getBannerImage());
                        existingContest.setLogo(newContest.getLogo() != null && !newContest.getLogo().isEmpty() ? newContest.getLogo() : existingContest.getLogo());
                        existingContest.setType(newContest.getType() != null && !newContest.getType().isEmpty() ? newContest.getType() : existingContest.getType());
                        existingContest.setNameOfContest(newContest.getNameOfContest() != null && !newContest.getNameOfContest().isEmpty() ? newContest.getNameOfContest() : existingContest.getNameOfContest());
                        existingContest.setTeam(newContest.getTeam() != null && !newContest.getTeam().isEmpty() ? newContest.getTeam() : existingContest.getTeam());
                        existingContest.setFee(newContest.getFee() != null && !newContest.getFee().isEmpty() ? newContest.getFee() : existingContest.getFee());
                        existingContest.setEligibility(newContest.getEligibility() != null && !newContest.getEligibility().isEmpty() ? newContest.getEligibility() : existingContest.getEligibility());
                        existingContest.setRounds(newContest.getRounds() != null && !newContest.getRounds().isEmpty() ? newContest.getRounds() : existingContest.getRounds());
                        existingContest.setRules(newContest.getRules() != null && !newContest.getRules().isEmpty() ? newContest.getRules() : existingContest.getRules());
                        existingContest.setRewards(newContest.getRewards() != null && !newContest.getRewards().isEmpty() ? newContest.getRewards() : existingContest.getRewards());
                        existingContest.setFaq(newContest.getFaq() != null && !newContest.getFaq().isEmpty() ? newContest.getFaq() : existingContest.getFaq());
                        existingContest.setFaqAnswer(newContest.getFaqAnswer() != null && !newContest.getFaqAnswer().isEmpty() ? newContest.getFaqAnswer() : existingContest.getFaqAnswer());
                        existingContest.setWinners(newContest.getWinners() != null && !newContest.getWinners().isEmpty() ? newContest.getWinners() : existingContest.getWinners());

                        return contestRepo.save(existingContest);
                    } else {
                        logger.error("Contest update logic error ID {}", id);
                        throw new RuntimeException("Contest update logic error");
                    }
                }else {
                    logger.error("Contest does not update {}", id);
                    throw new RuntimeException("Contest does not update");
                }
        }catch(Exception e){
            logger.error("Error updating Contest with ID {}", id, e);
            throw new RuntimeException("An error occurred while updating the Contest", e);
        }




    }


}
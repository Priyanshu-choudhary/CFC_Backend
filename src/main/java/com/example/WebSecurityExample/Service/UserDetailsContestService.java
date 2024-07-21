package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.*;
import com.example.WebSecurityExample.Pojo.Posts.Posts;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.Pojo.UserContestDetails;
import com.example.WebSecurityExample.controller.CourseController;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDetailsContestService {
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

    @Autowired
    private UserContestDetailRepo userContestDetailRepo;

    public List<UserContestDetails> getAllUserDetailsContest() {
        return userContestDetailRepo.findAll();
    }


    public List<UserContestDetails> getUserContestDetailsByContestName(String username, String nameOfContest) {
        User user = userService.findByName(username);
        if (user == null) {
            throw new RuntimeException("User not found with username: " + username);
        }

        return user.getUserContestDetails().stream()
                .filter(contest -> nameOfContest.equals(contest.getNameOfContest()))
                .collect(Collectors.toList());
    }

    public Optional<UserContestDetails> getUserContestDetailsByID(String ID) {
        Optional<UserContestDetails> details = userContestDetailRepo.findById(ID);
       return details;
    }

    public Optional<UserContestDetails> createUserContestDetails(UserContestDetails details, String inputUser) {
        try {
            // Fetch the user
            User myUser = userService.findByName(inputUser);

            // Ensure the user name is not null or empty
            if (myUser.getName() == null || myUser.getName().isEmpty()) {
                throw new IllegalArgumentException("User name cannot be null or empty");
            }

            // Check if a Contest with the same title already exists for this user
            Optional<UserContestDetails> existingUserDetailsContestOpt = myUser.getUserContestDetails().stream()
                    .filter(c -> c.getNameOfContest().equalsIgnoreCase(details.getNameOfContest()))
                    .findFirst();

            if (existingUserDetailsContestOpt.isPresent()) {
                // Contest already exists, return the existing course ID
                logger.info("Contest with the same title already exists for this user. Returning existing course ID.");
                return existingUserDetailsContestOpt;
            } else {
                // Associate the contest with the user
                logger.info("Try to save contest ");
                UserContestDetails savedUserDetailsContest = userContestDetailRepo.save(details);
                logger.info("Contest saving done ");

                // Update user's contest list
                logger.info("Update user's contest list ");
                myUser.getUserContestDetails().add(savedUserDetailsContest);
                logger.info("Done Update ");

                logger.info("Create new user ");
                userService.createUser(myUser); // Save the user to update the contests
                logger.info("Done ");

                Optional<UserContestDetails> saveDetails = Optional.of(savedUserDetailsContest);
                return saveDetails;
            }
        } catch (Exception e) {
            logger.error("An error occurred while saving the entry of Contest", e);
            throw new RuntimeException("An error occurred while saving the entry of Contest", e);
        }
    }



    public boolean deleteUserContestDetailsById(String id) {
        try {
            User myuser = userService.findByName("Contest");
            boolean b = myuser.getUserContestDetails().removeIf(x -> x.getId().equals(id));
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
    public void createPostWithRefUserDetailContest(Posts post, User user, String username) {
        try {

            if (user == null) {
                throw new RuntimeException("(Ref Deatils)User not found");
            }

            postRepo.save(post);
//            user.getPosts().add(post);
            userRepo.save(user);

            UserContestDetails userContestDetails = post.getUserContestDetails();
            if (userContestDetails != null) {
                userContestDetails.getPosts().add(post);
                userContestDetailRepo.save(userContestDetails);
            }

            logger.info("(Ref constest)Post created successfully for user: {}", username);
        } catch (Exception e) {
            logger.error("(Ref constest)Error creating post for user: {}", username, e);
            throw new RuntimeException("(Ref course)Error creating post", e);
        }
    }

    public List<Document> getUsersByContestName(String contestName) {
        return userRepo.findUsersByContestName(contestName);
    }
//    public Contest updateContest(String id, Contest newContest, String username) {
//        try {
//            logger.info("Updating course with ID {} for user {}", id, username);
//
//            // Fetch user from service
//            User user = userService.findByName(username);
//            logger.info("Fetched user {} for updating course", username);
//
//            // Find existing course
//            Optional<Contest> existingContestOpt = contestRepo.findById(id);
//            logger.info("Fetched course with ID {}", id);
//
//                // Check if course exists
//                if (existingContestOpt.isPresent()) {
//                    Contest existingContest = existingContestOpt.get();
//                    logger.info("Found existing course with ID {}", id);
//
//                    // Check if user owns the course
//                    if (user.getContests().contains(existingContest)) {
//                        logger.debug("User {} owns course {}", username, existingContest.getId());
//
//
//                        existingContest.setNameOfContest(newContest.getNameOfContest() != null && !newContest.getNameOfContest().isEmpty() ? newContest.getNameOfContest() : existingContest.getNameOfContest());
//                        existingContest.setNameOfOrganization(newContest.getNameOfOrganization() != null && !newContest.getNameOfOrganization().isEmpty() ? newContest.getNameOfOrganization() : existingContest.getNameOfOrganization());
//                        existingContest.setDate(newContest.getDate() != null ? newContest.getDate() : existingContest.getDate());
//                        existingContest.setDescription(newContest.getDescription() != null && !newContest.getDescription().isEmpty() ? newContest.getDescription() : existingContest.getDescription());
//                        existingContest.setBannerImage(newContest.getBannerImage() != null && !newContest.getBannerImage().isEmpty() ? newContest.getBannerImage() : existingContest.getBannerImage());
//                        existingContest.setLogo(newContest.getLogo() != null && !newContest.getLogo().isEmpty() ? newContest.getLogo() : existingContest.getLogo());
//                        existingContest.setType(newContest.getType() != null && !newContest.getType().isEmpty() ? newContest.getType() : existingContest.getType());
//                        existingContest.setNameOfContest(newContest.getNameOfContest() != null && !newContest.getNameOfContest().isEmpty() ? newContest.getNameOfContest() : existingContest.getNameOfContest());
//                        existingContest.setTeam(newContest.getTeam() != null && !newContest.getTeam().isEmpty() ? newContest.getTeam() : existingContest.getTeam());
//                        existingContest.setFee(newContest.getFee() != null && !newContest.getFee().isEmpty() ? newContest.getFee() : existingContest.getFee());
//                        existingContest.setEligibility(newContest.getEligibility() != null && !newContest.getEligibility().isEmpty() ? newContest.getEligibility() : existingContest.getEligibility());
//                        existingContest.setRounds(newContest.getRounds() != null && !newContest.getRounds().isEmpty() ? newContest.getRounds() : existingContest.getRounds());
//                        existingContest.setRules(newContest.getRules() != null && !newContest.getRules().isEmpty() ? newContest.getRules() : existingContest.getRules());
//                        existingContest.setRewards(newContest.getRewards() != null && !newContest.getRewards().isEmpty() ? newContest.getRewards() : existingContest.getRewards());
//                        existingContest.setFaq(newContest.getFaq() != null && !newContest.getFaq().isEmpty() ? newContest.getFaq() : existingContest.getFaq());
//                        existingContest.setFaqAnswer(newContest.getFaqAnswer() != null && !newContest.getFaqAnswer().isEmpty() ? newContest.getFaqAnswer() : existingContest.getFaqAnswer());
//                        existingContest.setWinners(newContest.getWinners() != null && !newContest.getWinners().isEmpty() ? newContest.getWinners() : existingContest.getWinners());
//                        existingContest.setRegisteredUser(newContest.getRegisteredUser() != null && !newContest.getRegisteredUser().isEmpty() ? newContest.getRegisteredUser() : existingContest.getRegisteredUser());
//                        existingContest.setLanguage(newContest.getLanguage() != null && !newContest.getLanguage().isEmpty() ? newContest.getLanguage() : existingContest.getLanguage());
//                        existingContest.setTimeDuration(newContest.getTimeDuration() != null && !newContest.getTimeDuration().isEmpty() ? newContest.getTimeDuration() : existingContest.getTimeDuration());
//
//                        return contestRepo.save(existingContest);
//                    } else {
//                        logger.error("Contest update logic error ID {}", id);
//                        throw new RuntimeException("Contest update logic error");
//                    }
//                }else {
//                    logger.error("Contest does not update {}", id);
//                    throw new RuntimeException("Contest does not update");
//                }
//        }catch(Exception e){
//            logger.error("Error updating Contest with ID {}", id, e);
//            throw new RuntimeException("An error occurred while updating the Contest", e);
//        }
//
//
//
//
//    }


}
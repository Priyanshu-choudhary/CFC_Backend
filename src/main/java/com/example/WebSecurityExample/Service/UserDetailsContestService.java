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
                logger.info("User Details with the same title already exists for this user. Returning existing course ID.");
                return existingUserDetailsContestOpt;
            } else {
                // Associate the User Details with the user
                logger.info("Try to save User Details ");
                UserContestDetails savedUserDetailsContest = userContestDetailRepo.save(details);
                logger.info("User Details saving done ");

                // Update user's User Details list
                logger.info("Update user's User Details list ");
                myUser.getUserContestDetails().add(savedUserDetailsContest);
                logger.info("Done Update ");

                logger.info("Create new user ");
                userService.createUser(myUser); // Save the user to update the contests
                logger.info("Done ");

                Optional<UserContestDetails> saveDetails = Optional.of(savedUserDetailsContest);
                return saveDetails;
            }
        } catch (Exception e) {
            logger.error("An error occurred while saving the entry of User Details", e);
            throw new RuntimeException("An error occurred while saving the entry of User Details", e);
        }
    }



    public boolean deleteUserContestDetailsById(String id, String name) {
        try {
            User myuser = userService.findByName(name);

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
    
    public UserContestDetails updateContestDetails(String id, UserContestDetails newDetails, String username) {
        try {
            logger.info("Updating course with ID {} for user {}", id, username);

            // Fetch user from service
            User user = userService.findByName(username);
            logger.info("Fetched user {} for updating course", username);

            // Find existing course
            Optional<UserContestDetails> existingDetailsOpt = userContestDetailRepo.findById(id);
            logger.info("Fetched course with ID {}", id);

                // Check if course exists
                if (existingDetailsOpt.isPresent()) {
                    UserContestDetails existingDetails = existingDetailsOpt.get();
                    logger.info("Found existing course with ID {}", id);

                    // Check if user owns the course
                    if (user.getUserContestDetails().contains(existingDetails)) {
                        logger.debug("User {} owns course {}", username, existingDetails.getId());


                        existingDetails.setNameOfContest(newDetails.getNameOfContest() != null && !newDetails.getNameOfContest().isEmpty() ? newDetails.getNameOfContest() : existingDetails.getNameOfContest());
                        existingDetails.setNameOfOrganization(newDetails.getNameOfOrganization() != null && !newDetails.getNameOfOrganization().isEmpty() ? newDetails.getNameOfOrganization() : existingDetails.getNameOfOrganization());
                        existingDetails.setDate(newDetails.getDate() != null ? newDetails.getDate() : existingDetails.getDate());
                        existingDetails.setType(newDetails.getType() != null && !newDetails.getType().isEmpty() ? newDetails.getType() : existingDetails.getType());
                        existingDetails.setNameOfContest(newDetails.getNameOfContest() != null && !newDetails.getNameOfContest().isEmpty() ? newDetails.getNameOfContest() : existingDetails.getNameOfContest());
                        existingDetails.setTeam(newDetails.getTeam() != null && !newDetails.getTeam().isEmpty() ? newDetails.getTeam() : existingDetails.getTeam());
                        existingDetails.setFee(newDetails.getFee() != null && !newDetails.getFee().isEmpty() ? newDetails.getFee() : existingDetails.getFee());
                        existingDetails.setEndTime(newDetails.getEndTime() != null  ? newDetails.getEndTime() : existingDetails.getEndTime());
                        existingDetails.setRounds(newDetails.getRounds() != null && !newDetails.getRounds().isEmpty() ? newDetails.getRounds() : existingDetails.getRounds());
                        existingDetails.setRewards(newDetails.getRewards() != null && !newDetails.getRewards().isEmpty() ? newDetails.getRewards() : existingDetails.getRewards());
                        existingDetails.setWinners(newDetails.getWinners() != null && !newDetails.getWinners().isEmpty() ? newDetails.getWinners() : existingDetails.getWinners());
                        existingDetails.setLanguage(newDetails.getLanguage() != null && !newDetails.getLanguage().isEmpty() ? newDetails.getLanguage() : existingDetails.getLanguage());

                        return userContestDetailRepo.save(existingDetails);
                    } else {
                        logger.error("User Details update logic error ID {}", id);
                        throw new RuntimeException("User Details update logic error");
                    }
                }else {
                    logger.error("User Details does not update {}", id);
                    throw new RuntimeException("User Details does not update");
                }
        }catch(Exception e){
            logger.error("Error updating User Details with ID {}", id, e);
            throw new RuntimeException("An error occurred while updating the User Details", e);
        }




    }


}
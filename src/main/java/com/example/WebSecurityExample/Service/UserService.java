package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.UserRepo;
import com.example.WebSecurityExample.Pojo.Posts.UserDTO.UserDTO;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.controller.PostController;
//import org.slf4j.// logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
//    private static final // logger // logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private UserRepo userRepository;

    private static final PasswordEncoder passwordEncoder= new BCryptPasswordEncoder();

//    @Cacheable("users")
public Page<UserDTO> getAllUsers(Pageable pageable) {
    Page<User> usersPage = userRepository.findAll(pageable);

    return usersPage.map(user -> {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setProfileImg(user.getProfileImg());
        userDTO.setRating(user.getRating());
        userDTO.setPostCount(user.getPosts().size());
        return userDTO;
    });
}



    public Optional<User> getUserById(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        userOpt.ifPresent(user -> user.setPostCount(user.getPosts().size()));
        return userOpt;
    }


    public User createNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setRoles(Arrays.asList("USER"));

        return userRepository.save(user);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }


    public void deleteUserById(String id) {
        userRepository.deleteById(id);
    }


    public User findByName(String username){
        // logger.info("user service findByName {}",username);
        User user = userRepository.findByName(username);
        // logger.info("get user from service findByName {}",username);
        if (user != null) {
            user.setPostCount(user.getPosts().size());
        }
        return user;
    }



    public void setLastdate(String name){
        User user = userRepository.findByName(name);
        if (user != null) {
            // logger.info("Set new date in user");
            user.setLastModifiedUser(new Date());
            userRepository.save(user);
            // logger.info("Updated!");

        }
    }




//    public void updateUser(User user) {
//        userRepository.save(user);
//    }
     // New method to check if a user exists by username
     public boolean existsByName(String username) {
        return userRepository.findByName(username) != null;
    }

}

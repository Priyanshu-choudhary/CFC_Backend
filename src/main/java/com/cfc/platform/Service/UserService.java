package com.cfc.platform.Service;

import com.cfc.platform.MongoRepo.PostRepo;
import com.cfc.platform.MongoRepo.UserRepo;
import com.cfc.platform.Pojo.Posts.UserDTO.UserDTO;
import com.cfc.platform.Pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private PostRepo postRepo;

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
        userDTO.setPostCount((int) postRepo.countByUserName(user.getName()));
        return userDTO;
    });
}



    public Optional<User> getUserById(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        userOpt.ifPresent(user -> user.setPostCount((int) postRepo.countByUserName(user.getName())));
        return userOpt;
    }


    public User createNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(java.util.Arrays.asList("USER"));
        }
        return userRepository.save(user);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }


    public void deleteUserById(String id) {
        userRepository.deleteById(id);
    }


    public User findByName(String username){
        log.info("findByName: {}", username);
        User user = userRepository.findByName(username);
        if (user != null) {
            user.setPostCount((int) postRepo.countByUserName(username));
        }
        return user;
    }



    public void setLastdate(String name){
        User user = userRepository.findByName(name);
        if (user != null) {
            user.setLastModifiedUser(new Date());
            userRepository.save(user);
            log.info("setLastdate: updated timestamp for {}", name);

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

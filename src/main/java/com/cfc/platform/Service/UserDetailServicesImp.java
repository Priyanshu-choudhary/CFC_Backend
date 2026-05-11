package com.cfc.platform.Service;

import com.cfc.platform.MongoRepo.UserRepo;
import com.cfc.platform.Pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServicesImp implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByName(username);
//        System.out.println("security Using Find By name....");
        if (user != null) {
            String[] roles = (user.getRoles() != null && !user.getRoles().isEmpty())
                    ? user.getRoles().toArray(new String[0])
                    : new String[]{"USER"};
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getName())
                    .password(user.getPassword())
                    .roles(roles)
                    .build();
        }
        throw new UsernameNotFoundException("User not found from security: " + username);
    }
}

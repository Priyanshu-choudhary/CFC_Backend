package com.cfc.platform.Service;

import com.cfc.platform.MongoRepo.UserRepo;
import com.cfc.platform.Pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailServicesImp implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByName(username);
        if (user != null) {
            String[] roles = normalizeRoles(user.getRoles());
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getName())
                    .password(user.getPassword())
                    .roles(roles)
                    .build();
        }
        throw new UsernameNotFoundException("User not found from security: " + username);
    }

    private String[] normalizeRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return new String[]{"USER"};
        }

        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toArray(String[]::new);
    }
}

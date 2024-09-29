package com.userAuthentication.service;

import com.userAuthentication.dao.MongoService;
import com.userAuthentication.model.user.UserPrincipal;
import com.userAuthentication.model.user.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private MongoService mongoService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserRegistry userRegistry = mongoService.getUserByUsername(username);
        if (null == userRegistry) {
            System.out.println("User not found.");
            throw new UsernameNotFoundException("user not found.");
        }

        return new UserPrincipal(userRegistry);
    }
}
package com.mountblue.BlogApplication.service;

import com.mountblue.BlogApplication.entity.User;
import com.mountblue.BlogApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        User user1 = userRepository.findByName(user.getName());
        Optional<User> user2 = userRepository.findByEmail(user.getEmail());
        if (user1 != null || user2.isPresent()) throw new RuntimeException("User is already present!");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User loginUser(String userName, String password) {
        User user = userRepository.findByName(userName);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPassword())) return user;
        }
        throw new RuntimeException("Invalid credentials");
   }
}

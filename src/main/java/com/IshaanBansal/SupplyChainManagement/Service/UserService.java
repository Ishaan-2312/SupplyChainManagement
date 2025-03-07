package com.IshaanBansal.SupplyChainManagement.Service;


import com.IshaanBansal.SupplyChainManagement.Model.User;
import com.IshaanBansal.SupplyChainManagement.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public User registerUser(User user) {
        User newUser=new User();
        String newPassword= passwordEncoder.encode(user.getPassword());
        newUser.setPassword(newPassword);
        newUser.setUserId(user.getUserId());
        newUser.setUserName(user.getUserName());
        newUser.setRoles(user.getRoles());
        newUser.setEmail(user.getEmail());
        return userRepository.save(newUser);

    }

    public Optional<User> getUserById(Long userId) {
        Optional<User> user=userRepository.findById(userId);
        return user;    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }
}


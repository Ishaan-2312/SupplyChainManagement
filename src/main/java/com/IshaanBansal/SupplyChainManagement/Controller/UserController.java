package com.IshaanBansal.SupplyChainManagement.Controller;


import com.IshaanBansal.SupplyChainManagement.Model.User;
import com.IshaanBansal.SupplyChainManagement.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/Users")
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    Logger log= LoggerFactory.getLogger(UserController.class);


    @PostMapping("/registerUser")
    public ResponseEntity<User> registerUser(@RequestBody User user){
        User newUser=userService.registerUser(user);
        if(newUser!=null){
            log.info("User Registered Succesfully");
            return ResponseEntity.ok(newUser);
        }
        log.info("Some Field in User Registered was Null");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @GetMapping("/getUserById")
    public ResponseEntity<Optional<User>> getUserById(@RequestParam Long userId){
        Optional<User> user=userService.getUserById(userId);
        if(user.isPresent()){
            return ResponseEntity.ok(user);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getAllUsers")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }
    @GetMapping("/getUserByEmail")
    public ResponseEntity<User> getUserByEmail(@RequestParam("email") String email){
        User user=userService.getUserByEmail(email);
        if(user!=null){
            return ResponseEntity.ok(user);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }




}


package com.example.webapp.controller;

import com.example.webapp.model.UserModel;
import com.example.webapp.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.regex.Pattern;

@RestController
public class UserController {
    @Autowired
    UserServiceImpl userServiceImpl;

    @GetMapping(path="/healthz")
    public ResponseEntity<UserModel> healthz(){
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path="/v1/user/{userId}")
    @ResponseBody
    public ResponseEntity<UserModel> getUserInfo(@PathVariable Integer userId){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserModel user = new UserModel();
        user = userServiceImpl.getUserDataById(userId);

        if(user != null){
            if(user.getUsername().equals(username)){
                return new ResponseEntity<>(user, HttpStatus.OK);
            }else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }else{
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(path="/v1/user")
    @ResponseBody
    public ResponseEntity<UserModel> AddUserInfo(@RequestBody UserModel userModel){
        if(userModel.getUsername()==null || userModel.getPassword()==null || userModel.getFirstName()==null || userModel.getLastName()==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            if(Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)" +
                            "*@[^-][A-Za-z0-9-]" +
                            "+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
                    .matcher(userModel.getUsername())
                    .matches()) {
                UserModel user = userServiceImpl.getUserDataByUsername(userModel.getUsername());
                if (user == null) {
                    UserModel userNew = userServiceImpl.addUserData(userModel);
                    return new ResponseEntity<>(userNew, HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }else{
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PutMapping(path="/v1/user/{userId}")
    public ResponseEntity<UserModel> updateUserInfo(@PathVariable Integer userId, @RequestBody UserModel userModel, Principal principal){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserModel user = new UserModel();
        user = userServiceImpl.getUserDataById(userId);

        if(user != null){
            if(user.getUsername().equals(username)){
                if(userModel.getUsername() == null || userModel.getFirstName() == null || userModel.getLastName() == null){
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }else{
                    if(user.getUsername().equals(userModel.getUsername())){
                        if(userModel.getAccountCreated()!=null){
//                        if(!user.getAccountCreated().equals(userModel.getAccountCreated())){
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//                        }
                        }

                        if(userModel.getAccountUpdated()!=null){
//                        if(!user.getAccountUpdated().equals(userModel.getAccountUpdated())){
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//                        }
                        }

                        UserModel userUdpate = userServiceImpl.updateUserData(userId, userModel);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

                    }else {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                }
            }else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }else{
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}

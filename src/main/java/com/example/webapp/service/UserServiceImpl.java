package com.example.webapp.service;

import com.example.webapp.model.UserModel;
import com.example.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserModel getUserDataById(Integer id) {
        UserModel userModel = userRepository.findByUserId(id);
        return userModel;
    }

    public UserModel getUserDataByUsername(String username) {
        UserModel userModel = userRepository.findByUsername(username);
        return userModel;
    }

    @Override
    public UserModel addUserData(UserModel userModel) {
        String bcryptedPass = passwordEncoder.encode(userModel.getPassword());
        userRepository.saveUser(userModel.getFirstName(), userModel.getLastName(), userModel.getUsername(), bcryptedPass, LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC));

        UserModel userModelNew = userRepository.findByUsername(userModel.getUsername());
        return userModelNew;
    }

    @Override
    public UserModel updateUserData(Integer id, UserModel userModel) {
        UserModel checkUser = userRepository.findByUserId(id);

        String bcryptedPass = passwordEncoder.encode(userModel.getPassword());
        userRepository.updateUser(userModel.getFirstName(), userModel.getLastName(), userModel.getUsername(), bcryptedPass, LocalDateTime.now(ZoneOffset.UTC), id);

        UserModel userModelNew = userRepository.findByUsername(userModel.getUsername());
        return userModelNew;
    }

}

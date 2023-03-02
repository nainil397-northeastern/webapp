package com.example.webapp.service;

import com.example.webapp.model.UserAccountModel;
import com.example.webapp.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserAccountModel getUserDataById(Integer id) {
        UserAccountModel userAccountModel = userAccountRepository.findByUserId(id);
        return userAccountModel;
    }

    public UserAccountModel getUserDataByUsername(String username) {
        UserAccountModel userAccountModel = userAccountRepository.findByUsername(username);
        return userAccountModel;
    }

    @Override
    public UserAccountModel addUserData(UserAccountModel userAccountModel) {
        String bcryptedPass = passwordEncoder.encode(userAccountModel.getPassword());
        userAccountRepository.saveUser(userAccountModel.getFirstName(), userAccountModel.getLastName(), userAccountModel.getUsername(), bcryptedPass, LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC));

        UserAccountModel userAccountModelNew = userAccountRepository.findByUsername(userAccountModel.getUsername());
        return userAccountModelNew;
    }

    @Override
    public UserAccountModel updateUserData(Integer id, UserAccountModel userAccountModel) {
        UserAccountModel checkUser = userAccountRepository.findByUserId(id);

        String bcryptedPass = passwordEncoder.encode(userAccountModel.getPassword());
        userAccountRepository.updateUser(userAccountModel.getFirstName(), userAccountModel.getLastName(), userAccountModel.getUsername(), bcryptedPass, LocalDateTime.now(ZoneOffset.UTC), id);

        UserAccountModel userAccountModelNew = userAccountRepository.findByUsername(userAccountModel.getUsername());
        return userAccountModelNew;
    }

}

package com.example.webapp.service;

import com.example.webapp.model.UserAccountModel;
import com.example.webapp.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Logger logger = LoggerFactory.getLogger(UserAccountServiceImpl.class);
        @Override
    public UserAccountModel getUserDataById(Integer id) {
            logger.info("Searching for user with userID.");
            UserAccountModel userAccountModel = userAccountRepository.findByUserId(id);
            return userAccountModel;
    }

    public UserAccountModel getUserDataByUsername(String username) {
        logger.info("Searching for user in database using username.");
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

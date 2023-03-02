package com.example.webapp.service;

import com.example.webapp.model.UserDataModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

public interface UserDataService {

    public UserDataModel getUserDataById(Integer id);

    public UserDataModel getUserDataByUsername(String username);

    public UserDataModel addUserData(UserDataModel userDataModel);

    public UserDataModel updateUserData(Integer id, UserDataModel userDataModel);

    public ResponseEntity<Object> healthz();
    public ResponseEntity<Object> addUserInfo(UserDataModel userDataModel);
    public ResponseEntity<Object> getUserInfo(String userIdStr, String username);
    public ResponseEntity<Object> updateUserInfo(String userIdStr, UserDataModel userDataModel, String username);
}

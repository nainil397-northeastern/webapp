package com.example.webapp.service;

import com.example.webapp.model.UserModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserService {

    public UserModel getUserById(Integer id);

    public UserModel getUserByUsername(String username);

    public UserModel addUser(UserModel userModel);

    public UserModel updateUser(Integer id, UserModel userModel);



    public ResponseEntity<Object> healthz();
    public ResponseEntity<Object> addUserInfo(UserModel userDataModel);
    public ResponseEntity<Object> getUserInfo(String userIdStr, String username);
    public ResponseEntity<Object> updateUserInfo(String userIdStr, UserModel userDataModel, String username);
}

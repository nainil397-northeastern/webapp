package com.example.webapp.service;

import com.example.webapp.model.UserModel;

public interface UserService {

    public UserModel getUserDataById(Integer id);

    public UserModel getUserDataByUsername(String username);

    public UserModel addUserData(UserModel userModel);

    public UserModel updateUserData(Integer id, UserModel userModel);

}

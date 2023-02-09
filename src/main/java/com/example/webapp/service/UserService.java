package com.example.webapp.service;

import com.example.webapp.model.UserModel;

public interface UserService {

    public UserModel getUserById(Integer id);

    public UserModel getUserByUsername(String username);

    public UserModel addUser(UserModel userModel);

    public UserModel updateUser(Integer id, UserModel userModel);

}

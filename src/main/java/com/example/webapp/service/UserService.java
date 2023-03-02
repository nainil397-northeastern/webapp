package com.example.webapp.service;

import com.example.webapp.model.UserAccountModel;

public interface UserAccountService {

    public UserAccountModel getUserDataById(Integer id);

    public UserAccountModel getUserDataByUsername(String username);

    public UserAccountModel addUserData(UserAccountModel userAccountModel);

    public UserAccountModel updateUserData(Integer id, UserAccountModel userAccountModel);

}

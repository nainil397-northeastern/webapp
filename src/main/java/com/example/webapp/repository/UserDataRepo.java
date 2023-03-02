package com.example.webapp.repository;

import com.example.webapp.model.UserDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UserDataRepo extends JpaRepository<UserDataModel,Integer> {

    //function to search user by the user's id
    UserDataModel findByUserId(Integer id);

    //function to search user by the user's username
    UserDataModel findByUsername(String username);

    //function to save a new user's data into the mysql database
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO webapp (first_name, last_name, username, psswrd, account_created, account_updated, authority, enabled) VALUES ( ?1 , ?2 , ?3 , ?4 , ?5, ?6, 'USER', 1)", nativeQuery = true)
    void saveUser(String firstName, String lastName, String username, String password, LocalDateTime accountCreated, LocalDateTime accountUpdated);

    //function to update a user's data into the mysql database
    @Modifying
    @Transactional
    @Query(value = "UPDATE webapp SET first_name = ?1, last_name = ?2, username = ?3, psswrd = ?4, account_updated = ?5 WHERE id = ?6", nativeQuery = true)
    void updateUser(String firstName, String lastName, String username, String password, LocalDateTime accountUpdated, Integer id);


}

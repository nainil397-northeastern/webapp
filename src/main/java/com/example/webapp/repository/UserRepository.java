package com.example.webapp.repository;

import com.example.webapp.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public interface UserRepository extends JpaRepository<UserModel,Integer> {

    UserModel findByUserId(Integer id);

    UserModel findByUsername(String username);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user (first_name, last_name, username, psswrd, account_created, account_updated, authority, enabled) VALUES ( ?1 , ?2 , ?3 , ?4 , ?5, ?6, 'USER', 1)", nativeQuery = true)
    void saveUser(String firstName, String lastName, String username, String password, LocalDateTime accountCreated, LocalDateTime accountUpdated);

    @Modifying
    @Transactional
    @Query(value = "UPDATE webapp SET first_name = ?1, last_name = ?2, username = ?3, psswrd = ?4, account_updated = ?5 WHERE id = ?6", nativeQuery = true)
    void updateUser(String firstName, String lastName, String username, String password, LocalDateTime accountUpdated, Integer id);


}

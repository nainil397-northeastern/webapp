package com.example.webapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Getter
@Setter
@Entity
@Table(name = "assignment_webapplication")
public class UserModel {
    @Id
    @Column(name="id")
    @JsonProperty(value = "id")
    private Integer userId;

    @Column(name = "first_name")
    @JsonProperty(value = "first_name")
    private String firstName;

    @JsonProperty(value = "last_name")
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "psswrd")
    @JsonProperty(value = "password", access = WRITE_ONLY)
    private String password;

    @JsonProperty(value = "username")
    @Column(name = "username")
    private String username;

    @Column(name = "account_created")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty(value = "account_created")
    private ZonedDateTime accountCreated;

    @Column(name = "account_updated")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty(value = "account_updated")
    private ZonedDateTime accountUpdated;
}

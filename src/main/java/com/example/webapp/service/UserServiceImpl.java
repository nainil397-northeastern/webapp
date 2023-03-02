package com.example.webapp.service;

import com.example.webapp.model.ErrorResponseModel;
import com.example.webapp.model.UserModel;
import com.example.webapp.repository.UserRepository;
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
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserModel getUserById(Integer id) {
        UserModel userModel = userRepository.findByUserId(id);
        return userModel;
    }

    public UserModel getUserByUsername(String username) {
       UserModel userModel = userRepository.findByUsername(username);
        return userModel;
    }

    @Override
    public UserModel addUser(UserModel userModel) {
        String bcryptedPass = passwordEncoder.encode(userModel.getPassword());
        userRepository.saveUser(userModel.getFirstName(), userModel.getLastName(), userModel.getUsername(), bcryptedPass, LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC));

        UserModel userModelNew = userRepository.findByUsername(userModel.getUsername());
        return userModelNew;
    }

    @Override
    public UserModel updateUser(Integer id, UserModel userModel) {
        UserModel checkUser = userRepository.findByUserId(id);

        String bcryptedPass = passwordEncoder.encode(userModel.getPassword());
        userRepository.updateUser(userModel.getFirstName(), userModel.getLastName(), userModel.getUsername(), bcryptedPass, LocalDateTime.now(ZoneOffset.UTC), id);

        UserModel userModelNew = userRepository.findByUsername(userModel.getUsername());
        return userModelNew;
    }


    @Override
    public ResponseEntity<Object> healthz(){
        /*This function returns the health of the application*/

        //Returns 200 OK, if application is up and running
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> addUserInfo(UserModel userModel){
        /*This function adds a new user to the database and also handles any other possible errors*/

        //Check if any mandatory field is set null
        if(userModel.getUsername()==null || userModel.getPassword()==null || userModel.getFirstName()==null || userModel.getLastName()==null){

            //If mandatory field is missing, return 400 Bad Request
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One or more fields are null");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            //Check for email validation
            if(Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)" +
                            "*@[^-][A-Za-z0-9-]" +
                            "+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
                    .matcher(userModel.getUsername())
                    .matches()) {
                //Check if the email already exists as a username
                UserModel user = getUserByUsername(userModel.getUsername());
                if (user == null) {
                    if(userModel.getFirstName().length()<1 || userModel.getLastName().length()<1 || userModel.getPassword().length()<4){

                        //If username does not exist in database but any mandatory field is very short in length, return 400 Bad Request
                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("One or more fields are too short");

                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }else{
                        //If username does not exist in the database and all fields are okay, save the user to database
                        UserModel newUserData = addUser(userModel);
                        return new ResponseEntity<>(newUserData, HttpStatus.CREATED);
                    }
                } else {
                    //If username already exists in database, return 400 Bad Request
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Username already exists");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else{
                //If any issues validating the email address, return 400 Bad Request
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("Enter a valid email address");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Override
    public ResponseEntity<Object> getUserInfo(String userIdStr, String username){
        /*This function is used to get information for authenticated user*/

        //Check if the user id is an integer, else return 400 Bad Request
        Integer userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("User id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        //Fetch the user details
        UserModel userData = new UserModel();
        userData = getUserById(userId);

        if(userData != null){
            if(userData.getUsername().equals(username)){
                //If user details exists and are those of authenticated user, return response body
                return new ResponseEntity<>(userData, HttpStatus.OK);
            }else {
                //If user details exists but are not of the authenticated user, return Forbidden error
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }
        }else{
            //If details do not exist, still return a Forbidden error
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public ResponseEntity<Object> updateUserInfo(String userIdStr, UserModel userModel, String username){
        /*This function is used to update information of authenticated user*/

        //Check if user id is an integer, else return Bad Request error
        Integer userId;
        try {
            userId = Integer.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("User id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        //Get user information from user id
        UserModel userData = new UserModel();
        userData = getUserById(userId);

        if(userData != null){
            if(userData.getUsername().equals(username)){
                //If user exists and details are same as that of the authenticated user
                if(userModel.getUsername() == null || userModel.getFirstName() == null || userModel.getLastName() == null || userModel.getPassword()== null){
                    //If one or more mandatory fields are missing, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are null");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else{
                    if(userData.getUsername().equals(userModel.getUsername())){

                        if(userModel.getFirstName().length()<1 || userModel.getLastName().length()<1 || userModel.getPassword().length()<4){
                            //If one or more fields are very short in length, return Bad Request error

                            ErrorResponseModel errorResponse = new ErrorResponseModel();
                            errorResponse.setErr("Bad Request");
                            errorResponse.setStatus(400);
                            errorResponse.setMessage("One or more fields are too short");

                            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                        }else{
                            //If all conditions are satisfied, update the user information

                            UserModel updatedUserData = updateUser(userId, userModel);
                            return new ResponseEntity<>(updatedUserData, HttpStatus.NO_CONTENT);
                        }


                    }else {
                        //If username is incorrect in the request body, issue Bad Request error

                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("Please enter correct username in request body");

                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }
                }
            }else {
                //If Usernames do not match, return Forbidden error

                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }
        }else{
            //If user id does not exist still return a Forbidden error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }
    }
}



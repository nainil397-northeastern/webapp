package com.example.webapp.service;

import com.example.webapp.model.ErrorResponseModel;
import com.example.webapp.model.UserDataModel;
import com.example.webapp.repository.UserDataRepo;
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
public class UserDataServiceImpl implements UserDataService {

    @Autowired
    UserDataRepo userDataRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDataModel getUserDataById(Integer id) {
        /*This function searches the database by the user id and returns the
        * user data, if it exists*/

        //searching for the user by their id
        UserDataModel userDataModel = userDataRepo.findByUserId(id);
        return userDataModel;
    }

    public UserDataModel getUserDataByUsername(String username) {
        /*This function searches the database by the user's username and returns the
         * user data, if it exists*/

        //searching for the user by their username
        UserDataModel userDataModel = userDataRepo.findByUsername(username);
        return userDataModel;
    }

    @Override
    public UserDataModel addUserData(UserDataModel userDataModel) {
        /*This function saves the new user's data in the database. It then
        * returns the saved data back in the endpoint for displaying to the
        * user*/

        //Encrypting the password by using BCrypt algorithm with salt
        //The salt is stored in the generated BCrypt hash string which is
        //then subsequently stored in the database. This password would then
        //be used for authenticating the users at the authenticated endpoints
        String bcryptedPass = passwordEncoder.encode(userDataModel.getPassword());

        //Saving the user data (first name, last name, username, password, account
        //creation timestamp, and account updation timestamp) in the database
        userDataRepo.saveUser(userDataModel.getFirstName(), userDataModel.getLastName(), userDataModel.getUsername(), bcryptedPass, LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC));

        //searching
        UserDataModel userDataModelNew = userDataRepo.findByUsername(userDataModel.getUsername());
        return userDataModelNew;
    }

    @Override
    public UserDataModel updateUserData(Integer id, UserDataModel userDataModel) {
        /*This function updates the data of the selected user*/

        //check if the user exists in the DB
        UserDataModel checkUser = userDataRepo.findByUserId(id);

        //Encrypt password using BCrypt algorithm
        String bcryptedPass = passwordEncoder.encode(userDataModel.getPassword());

        //Save the updated user information to the database
        userDataRepo.updateUser(userDataModel.getFirstName(), userDataModel.getLastName(), userDataModel.getUsername(), bcryptedPass, LocalDateTime.now(ZoneOffset.UTC), id);

        //Return the updated user information
        UserDataModel userDataModelNew = userDataRepo.findByUsername(userDataModel.getUsername());
        return userDataModelNew;
    }

    @Override
    public ResponseEntity<Object> healthz(){
        /*This function returns the health of the application*/

        //Returns 200 OK, if application is up and running
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> addUserInfo(UserDataModel userDataModel){
        /*This function adds a new user to the database and also handles any other possible errors*/

        //Check if any mandatory field is set null
        if(userDataModel.getUsername()==null || userDataModel.getPassword()==null || userDataModel.getFirstName()==null || userDataModel.getLastName()==null){

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
                    .matcher(userDataModel.getUsername())
                    .matches()) {
                //Check if the email already exists as a username
                UserDataModel user = getUserDataByUsername(userDataModel.getUsername());
                if (user == null) {
                    if(userDataModel.getFirstName().length()<1 || userDataModel.getLastName().length()<1 || userDataModel.getPassword().length()<4){

                        //If username does not exist in database but any mandatory field is very short in length, return 400 Bad Request
                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("One or more fields are too short");

                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }else{
                        //If username does not exist in the database and all fields are okay, save the user to database
                        UserDataModel newUserData = addUserData(userDataModel);
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
        UserDataModel userData = new UserDataModel();
        userData = getUserDataById(userId);

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
    public ResponseEntity<Object> updateUserInfo(String userIdStr, UserDataModel userDataModel, String username){
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
        UserDataModel userData = new UserDataModel();
        userData = getUserDataById(userId);

        if(userData != null){
            if(userData.getUsername().equals(username)){
                //If user exists and details are same as that of the authenticated user
                if(userDataModel.getUsername() == null || userDataModel.getFirstName() == null || userDataModel.getLastName() == null || userDataModel.getPassword()== null){
                    //If one or more mandatory fields are missing, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are null");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else{
                    if(userData.getUsername().equals(userDataModel.getUsername())){

                        if(userDataModel.getFirstName().length()<1 || userDataModel.getLastName().length()<1 || userDataModel.getPassword().length()<4){
                            //If one or more fields are very short in length, return Bad Request error

                            ErrorResponseModel errorResponse = new ErrorResponseModel();
                            errorResponse.setErr("Bad Request");
                            errorResponse.setStatus(400);
                            errorResponse.setMessage("One or more fields are too short");

                            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                        }else{
                            //If all conditions are satisfied, update the user information

                            UserDataModel updatedUserData = updateUserData(userId, userDataModel);
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

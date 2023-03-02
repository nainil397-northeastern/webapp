package com.example.webapp.controller;

import com.example.webapp.model.ErrorResponseModel;
import com.example.webapp.model.ProductDataModel;
import com.example.webapp.model.UserDataModel;
import com.example.webapp.service.ImageDataServiceImpl;
import com.example.webapp.service.ProductDataServiceImpl;
import com.example.webapp.service.UserDataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
public class UserDataController {
    @Autowired
    UserDataServiceImpl userServiceImpl;

    @Autowired
    ProductDataServiceImpl productServiceImpl;

    @Autowired
    ImageDataServiceImpl imageServiceImpl;

    @GetMapping(path="/healthz")
    public ResponseEntity<Object> healthz(){
        /*This endpoint tests whether your web application is up and running.
        * It returns Http Status code 200 if the above condition is satisfied*/

        return userServiceImpl.healthz();
    }

    @PostMapping(path="/v1/user")
    @ResponseBody
    public ResponseEntity<Object> addUserInfo(@RequestBody UserDataModel userDataModel){
        /*This endpoint is used to add new users to the database
        * It takes in request body in json format which include four
        * fields namely, first_name, last_name, username and password.
        *
        * The username field should always be unique and two users with the
        * same usernames are not allowed.
        *
        * Similarly, there are restrictions on the minimum lengths of
        * first_name, last_name and password fields. Any other fields
        * that would be passed in the json are completely ignored.
        *
        * The application returns a response json upon successful
        * addition of the new user in the database. It excludes the user's
        * password but includes two additional fields account_created and
        * account_updated which contains the timestamp when the user was
        * created*/

        return userServiceImpl.addUserInfo(userDataModel);
    }

    @GetMapping(path="/v1/user/{userIdStr}")
    @ResponseBody
    public ResponseEntity<Object> getUserInfo(@PathVariable String userIdStr){
        /* This endpoint returns the data of the requested user. It is an
        * authenticated endpoint. It needs to username and password of the
        * user to be included in the header token and validates the same
        * using Basic Auth authentication. Once authenticated the user can
        * only access the user's own data and not anyone else's data.
        *
        * If the userId included in the URL params matches with that of the
        * username passed in the header token, an http 200 status code is
        * returned along with a response json containing the id, first_name,
        * last_name, username, account_created and account_updated field
        * values. The password field is not returned for security reasons.
        *
        * In case the userId from the URL params does not match with id of
        * the username passed in the header token, an http 403 forbidden
        * status code is returned.
        *
        * In any other case, when the endpoint is unable to authenticate the
        * user, an http 401 unauthorized status code is returned. */

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return userServiceImpl.getUserInfo(userIdStr, username);
    }


    @PutMapping(path="/v1/user/{userIdStr}")
    public ResponseEntity<Object> updateUserInfo(@PathVariable String userIdStr, @RequestBody UserDataModel userDataModel){
        /* This endpoint updates the data of the requested user. It is an
         * authenticated endpoint. It needs to username and password of the
         * user to be included in the header token and validates the same
         * using Basic Auth authentication. Once authenticated the user can
         * only access the user's own data and not anyone else's data.
         *
         * The endpoint needs a request body in json format in order to update
         * the details of the user. The json should have 4 fields for first_name,
         * last_name, username, password. The username field should have the same
         * value as that of the user details included in the basic auth token. In
         * case any discrepancy is found, the endpoint returns http status code
         * 403 forbidden and no update is performed. Thus, the user can only update
         * first_name, last_name and password fields using this endpoint.
         *
         * If the userId included in the URL params matches with that of the
         * username passed in the header token, an http 200 status code is
         * returned along with a response json containing the id, first_name,
         * last_name, username, account_created and account_updated field
         * values. The password field is not returned for security reasons.
         * The account_updated field has the timestamp of the last update time
         * for the given user
         *
         * In case the userId from the URL params does not match with id of
         * the username passed in the header token, a http 403 forbidden
         * status code is returned.
         *
         * In any other case, when the endpoint is unable to authenticate the
         * user, a http 401 unauthorized status code is returned. */

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return userServiceImpl.updateUserInfo(userIdStr, userDataModel, username);
    }

    @PostMapping(path="/v1/product")
    @ResponseBody
    public ResponseEntity<Object> addProductData(@RequestBody ProductDataModel productDataModel) throws Exception {
        /* This endpoint is used to add new products to the database.
         * The endpoint only allows authenticated users to add new products
         * It takes in request body in json format which include five
         * fields namely, name, description, sku, manufacturer and quantity
         *
         * The name, description, sku and manufacturer fields have minimum
         * restrictions and must be longer than 0 letters.
         *
         * Similarly, the sku should be unique for each newly added product
         * and two users cannot add products with the same sku
         *
         * The quantity attribute must be an integer and must be between 0 to
         * 100, both inclusive. Any negative value or string would result in
         * an error.
         *
         * The application returns a response json upon successful
         * addition of the new product in the database. It includes
         * 4 additional attributes, the id, owner_user_id along with the
         * date_added and date_last_updated which contains the timestamp
         * when the product was created*/

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return productServiceImpl.addProductData(productDataModel, username);
    }

    @GetMapping(path="/v1/product/{productIdStr}")
    @ResponseBody
    public ResponseEntity<Object> getProductData(@PathVariable String productIdStr){
        /* This endpoint returns the data of the requested product. It is an
         * unauthenticated endpoint.
         *
         * If the productId included in the URL params matches with that of any
         * product in the database, an http 200 status code is returned along with
         * a json response containing the id, name, description, sku, manufacturer,
         * quantity, date_added, date_last_updated, and owner_user_id.
         *
         * In case the productId from the URL params does not match with id of
         * any product in the database, a 404 Not Found response is returned.*/

        return productServiceImpl.getProductData(productIdStr);

    }

    @PutMapping(path="/v1/product/{productIdStr}")
    public ResponseEntity<Object> updateProductDataPut(@PathVariable String productIdStr, @RequestBody ProductDataModel productDataModel){
        /* This PUT endpoint is used to update existing products to the database.
         * The endpoint only allows authenticated users to update the products
         * It takes in request body in json format which include five
         * fields namely, name, description, sku, manufacturer and quantity
         *
         * The name, description, sku and manufacturer fields have minimum
         * restrictions and must be longer than 0 letters.
         *
         * Similarly, the sku should be unique for each newly added product
         * and two users cannot add products with the same sku
         *
         * The quantity attribute must be an integer and must be between 0 to
         * 100, both inclusive. Any negative value or string would result in
         * an error.
         *
         * The application returns a response json upon successful
         * updation of new product in the database. It includes
         * 4 additional attributes, the id, owner_user_id along with the
         * date_added and date_last_updated which contains the timestamp
         * when the product was created and updated*/

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return productServiceImpl.updateProductDataPut(productIdStr, productDataModel, username);
    }

    @PatchMapping(path="/v1/product/{productIdStr}")
    public ResponseEntity<Object> updateProductDataPatch(@PathVariable String productIdStr, @RequestBody ProductDataModel productDataModel){
        /* This PATCH endpoint is used to update existing products to the database.
         * The endpoint only allows authenticated users to update the products
         * It takes in request body in json format which include any of the five optional
         * fields namely, name, description, sku, manufacturer and quantity
         *
         * The name, description, sku and manufacturer fields have minimum
         * restrictions and must be longer than 0 letters.
         *
         * Similarly, the sku should be unique for each newly added product
         * and two users cannot add products with the same sku
         *
         * The quantity attribute must be an integer and must be between 0 to
         * 100, both inclusive. Any negative value or string would result in
         * an error.
         *
         * The application returns a response json upon successful
         * updation of new product in the database. It includes
         * 4 additional attributes, the id, owner_user_id along with the
         * date_added and date_last_updated which contains the timestamp
         * when the product was created and updated*/

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return productServiceImpl.updateProductDataPatch(productIdStr, productDataModel, username);
    }

    @DeleteMapping(path="/v1/product/{productIdStr}")
    public ResponseEntity<Object> deleteProductData(@PathVariable String productIdStr){

        /*This endpoint enables deleting a product by the authenticated user. It takes
        * in the product id and checks if the product was created by the authenticated user
        * If yes, the product is successfully deleted and No Content status is returned.
        *
        * In case, the product was created by a different user, a Forbidden error is returned.
        *
        * In case, a particular product id does not exist in the database, a Not Found Error
        * is returned*/

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return productServiceImpl.deleteProductData(productIdStr, username);
    }

    @ExceptionHandler
    public final ResponseEntity handleException(Exception e, WebRequest req){
        //If exception parsing quantity as it was string, return custom error

        ErrorResponseModel errorResponse = new ErrorResponseModel();
        errorResponse.setErr("Bad Request");
        errorResponse.setStatus(400);
        errorResponse.setMessage("Quantity should be an integer");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(path = "/v1/product/{productIdStr}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadFile(@PathVariable String productIdStr, @RequestPart(value = "file") MultipartFile multipartFile) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        String fileUrl = "";
        String  status = null;
        try {
            return imageServiceImpl.uploadFileTos3bucket(multipartFile, productIdStr, username);
        } catch (Exception e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Image cannot be uploaded. Please try again");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/v1/product/{productIdStr}/image/{imageIdStr}")
    public ResponseEntity<Object> getDetailsByImageId(@PathVariable String productIdStr, @PathVariable String imageIdStr) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return imageServiceImpl.getImageById(productIdStr, username, imageIdStr);
    }

    @GetMapping(path = "/v1/product/{productIdStr}/image")
    public ResponseEntity<Object> getDetailsByImageId(@PathVariable String productIdStr) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return imageServiceImpl.getImagesByProductId(productIdStr, username);
    }

    @DeleteMapping(path = "/v1/product/{productIdStr}/image/{imageIdStr}")
    public ResponseEntity<Object> deleteDetailsByImageId(@PathVariable String productIdStr, @PathVariable String imageIdStr) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return imageServiceImpl.deleteImageById(productIdStr, username, imageIdStr);
    }
}

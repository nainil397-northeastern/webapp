package com.example.webapp.controller;

import com.example.webapp.model.ErrorResponseModel;
import com.example.webapp.model.ProductModel;
import com.example.webapp.model.UserAccountModel;
import com.example.webapp.service.ImageDataServiceImpl;
import com.example.webapp.service.ProductServiceImpl;
import com.example.webapp.service.UserAccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.regex.Pattern;
import java.io.File;

@RestController
//@ControllerAdvice
public class UserAccountController {
    @Autowired
    UserAccountServiceImpl userServiceImpl;

    @Autowired
    ImageDataServiceImpl imageServiceImpl;

    @Autowired
    ProductServiceImpl productServiceImpl;
    @GetMapping(path="/healthz")
    public ResponseEntity<UserAccountModel> healthz(){
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path="/v1/user/{strUserId}")
    @ResponseBody
    public ResponseEntity<Object> getUserInfo(@PathVariable String strUserId){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Integer userId;
        try {
            userId = Integer.parseInt(strUserId);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("User id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        UserAccountModel user = new UserAccountModel();
        user = userServiceImpl.getUserDataById(userId);

        if(user != null){
            if(user.getUsername().equals(username)){
                return new ResponseEntity<>(user, HttpStatus.OK);
            }else {
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }
        }else{
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(path="/v1/user")
    @ResponseBody
    public ResponseEntity<Object> AddUserInfo(@RequestBody UserAccountModel userAccountModel){
        if(userAccountModel.getUsername()==null || userAccountModel.getPassword()==null || userAccountModel.getFirstName()==null || userAccountModel.getLastName()==null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One or more fields are null");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            if(Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)" +
                            "*@[^-][A-Za-z0-9-]" +
                            "+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
                    .matcher(userAccountModel.getUsername())
                    .matches()) {
                UserAccountModel user = userServiceImpl.getUserDataByUsername(userAccountModel.getUsername());
                if (user == null) {
                    if(userAccountModel.getFirstName().length()==0 || userAccountModel.getLastName().length()==0 || userAccountModel.getPassword().length()<3){
                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("One or more fields are too short");
                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }else{
                        UserAccountModel userNew = userServiceImpl.addUserData(userAccountModel);
                        return new ResponseEntity<>(userNew, HttpStatus.CREATED);
                    }
                } else {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Username already exists");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else{
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("Enter a valid email address");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PutMapping(path="/v1/user/{strUserId}")
    public ResponseEntity<Object> updateUserInfo(@PathVariable String strUserId, @RequestBody UserAccountModel userAccountModel, Principal principal){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Integer userId;
        try {
            userId = Integer.parseInt(strUserId);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("User id should be an integer");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        UserAccountModel user = new UserAccountModel();
        user = userServiceImpl.getUserDataById(userId);

        if(user != null){
            if(user.getUsername().equals(username)){
                if(userAccountModel.getUsername() == null || userAccountModel.getFirstName() == null || userAccountModel.getLastName() == null || userAccountModel.getPassword() == null){
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are null");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else{
                    if(user.getUsername().equals(userAccountModel.getUsername())){
//                        if(userAccountModel.getAccountCreated()!=null){
////                        if(!user.getAccountCreated().equals(userModel.getAccountCreated())){
//                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
////                        }
//                        }
//
//                        if(userAccountModel.getAccountUpdated()!=null){
////                        if(!user.getAccountUpdated().equals(userModel.getAccountUpdated())){
//                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
////                        }
//                        }
                        if(userAccountModel.getFirstName().length()<1 || userAccountModel.getLastName().length()<1 || userAccountModel.getPassword().length()<4){
                            ErrorResponseModel errorResponse = new ErrorResponseModel();
                            errorResponse.setErr("Bad Request");
                            errorResponse.setStatus(400);
                            errorResponse.setMessage("One or more fields are too short");

                            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                        }else {
                            UserAccountModel userUdpate = userServiceImpl.updateUserData(userId, userAccountModel);
                            return new ResponseEntity<>(userUdpate, HttpStatus.NO_CONTENT);
                        }
                    }else {
                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("Please enter correct username in request body");

                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }
                }
            }else {
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }
        }else{
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping(path="/v1/product/{strProductId}")
    @ResponseBody
    public ResponseEntity<Object> getProductInfo(@PathVariable String strProductId){

        Integer productId;
        try {
            productId = Integer.valueOf(strProductId);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel productData = new ProductModel();
        productData = productServiceImpl.getProductByProductId(productId);

        if(productData == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Not Found");
            errorResponse.setStatus(404);
            errorResponse.setMessage("Product Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(productData, HttpStatus.OK);
        }

    }

    @PostMapping(path="/v1/product")
    @ResponseBody
    public ResponseEntity<Object> AddProductInfo(@RequestBody ProductModel productModel) throws Exception{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserAccountModel userData = userServiceImpl.getUserDataByUsername(username);

        if(userData == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        productModel.setUser(userData);

        Integer quantity;
        try {
            quantity = Integer.valueOf(productModel.getQuantity());
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Quantity should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        if(productModel.getName() == null || productModel.getDescription() == null || productModel.getSku() == null || productModel.getManufacturer() == null || productModel.getQuantity() == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One or more fields are null");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            ProductModel searchProduct = new ProductModel();
            searchProduct = productServiceImpl.searchProductDataBySku(productModel.getSku());

            if(searchProduct == null){
                if(productModel.getName().length()<1 || productModel.getDescription().length()<1 || productModel.getSku().length()<1 || productModel.getManufacturer().length()<1) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else if(productModel.getQuantity()<0 || productModel.getQuantity() > 100){
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Quantity should be between 0 and 100, both inclusive");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else{
                    ProductModel newProductData = productServiceImpl.addProductData(productModel);
                    return new ResponseEntity<>(newProductData, HttpStatus.CREATED);
                }

            }else{
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("SKU already exists");

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PutMapping(path="/v1/product/{strProductId}")
    public ResponseEntity<Object> updateProductInfo(@PathVariable String strProductId, @RequestBody ProductModel productModel){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserAccountModel userData = userServiceImpl.getUserDataByUsername(username);

        if(userData == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        productModel.setUser(userData);

        Integer productId;
        try {
            productId = Integer.valueOf(strProductId);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        if(productModel.getName() == null || productModel.getDescription() == null || productModel.getSku() == null || productModel.getManufacturer() == null || productModel.getQuantity() == null ){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One or more fields are null");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            ProductModel searchProduct = new ProductModel();
            searchProduct = productServiceImpl.searchProductDataById(productId);

            if(searchProduct == null){
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }else if(searchProduct.getUser().getUserId() != userData.getUserId()){
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbideen");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }else{
                if(productModel.getName().length()<1 || productModel.getDescription().length()<1 || productModel.getSku().length()<1 || productModel.getManufacturer().length()<1) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else if(productModel.getQuantity()<0 || productModel.getQuantity() > 100){
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Quantity should be between 0 and 100, both inclusive");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else{
                    if(searchProduct.getSku() != productModel.getSku()){
                        ProductModel searchProductSku = new ProductModel();
                        searchProductSku = productServiceImpl.searchProductDataBySku(productModel.getSku());
                        if(searchProductSku == null){
                            ProductModel updatedProductData = productServiceImpl.updateProductData(productId,productModel);
                            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                        }else{
                            if(searchProductSku.getProductId() != productId){
                                ErrorResponseModel errorResponse = new ErrorResponseModel();
                                errorResponse.setErr("Bad Request");
                                errorResponse.setStatus(400);
                                errorResponse.setMessage("SKU already exists");

                                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                            }else{
                                ProductModel updatedProductData = productServiceImpl.updateProductData(productId, productModel);
                                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                            }
                        }
                    }else{
                        ProductModel updatedProductData = productServiceImpl.updateProductData(productId, productModel);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    }
                }
            }
        }
    }

    @PatchMapping(path="/v1/product/{strProductId}")
    public ResponseEntity<Object> updateProductInfoUsingPatch(@PathVariable String strProductId, @RequestBody ProductModel productModel){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserAccountModel userData = userServiceImpl.getUserDataByUsername(username);

        if(userData == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        productModel.setUser(userData);

        Integer productId;
        try {
            productId = Integer.valueOf(strProductId);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel searchProduct = new ProductModel();
        searchProduct = productServiceImpl.searchProductDataById(productId);

        if(searchProduct == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }else if(searchProduct.getUser().getUserId() != userData.getUserId()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }else{
            if(productModel.getName() == null && productModel.getDescription() == null && productModel.getSku() == null && productModel.getManufacturer() == null && productModel.getQuantity() == null ){
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("Please enter at least one mandatory field");

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            if(productModel.getName()!= null) {
                if (productModel.getName().length() < 1) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                productModel.setName(searchProduct.getName());
            }

            if(productModel.getDescription()!= null) {
                if (productModel.getDescription().length() < 1) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                productModel.setDescription(searchProduct.getDescription());
            }

            if(productModel.getSku() != null) {
                if (productModel.getSku().length() < 1) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                productModel.setSku(searchProduct.getSku());
            }

            if(productModel.getSku() != null) {
                if (productModel.getSku().length() < 1) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                productModel.setSku(searchProduct.getSku());
            }

            if(productModel.getManufacturer() != null) {
                if (productModel.getManufacturer().length() < 1) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                productModel.setManufacturer(searchProduct.getManufacturer());
            }

            if(productModel.getQuantity() != null) {
                if (productModel.getQuantity() < 0 || productModel.getQuantity() > 100) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Quantity should be between 0 and 100, both inclusive");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                productModel.setQuantity(searchProduct.getQuantity());
            }


            if(searchProduct.getSku() != productModel.getSku()){
                ProductModel searchProductSku = new ProductModel();
                searchProductSku = productServiceImpl.searchProductDataBySku(productModel.getSku());
                if(searchProductSku == null){
                    ProductModel updatedProductData = productServiceImpl.updateProductData(productId, productModel);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }else{
                    if(searchProductSku.getProductId() != productId){
                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("SKU already exists");

                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }else{
                        ProductModel updatedProductData = productServiceImpl.updateProductData(productId, productModel);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    }
                }
            }else{
                ProductModel updatedProductData = productServiceImpl.updateProductData(productId, productModel);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

        }
    }

    @DeleteMapping(path="/v1/product/{strProductId}")
    public ResponseEntity<Object> deleteProductInfo(@PathVariable String strProductId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserAccountModel userData = userServiceImpl.getUserDataByUsername(username);

        if(userData == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        Integer productId;
        try {
            productId = Integer.valueOf(strProductId);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel searchProduct = new ProductModel();
        searchProduct = productServiceImpl.searchProductDataById(productId);

        if(searchProduct == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Not Found");
            errorResponse.setStatus(404);
            errorResponse.setMessage("Resource not found");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }else{
            if(searchProduct.getUser().getUserId() == userData.getUserId()){
                productServiceImpl.deleteProductData(productId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }
        }
    }

    @ExceptionHandler
    public final ResponseEntity handleException(Exception e, WebRequest req){
        ErrorResponseModel errorResponse = new ErrorResponseModel();
        errorResponse.setErr("Bad Request");
        errorResponse.setStatus(400);
        errorResponse.setMessage("Quantity should be integer");

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
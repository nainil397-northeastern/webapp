package com.example.webapp.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.example.webapp.model.*;
import com.example.webapp.repository.ImageDataRepo;
import com.example.webapp.service.ImageDataServiceImpl;
import com.example.webapp.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.regex.Pattern;

import com.example.webapp.model.UserModel;
import com.example.webapp.service.ProductServiceImpl;
import com.example.webapp.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.security.Principal;
import java.util.regex.Pattern;


@RestController
@ControllerAdvice
public class UserController {
    @Autowired
    UserServiceImpl userServiceImpl;

    @Autowired
    ProductServiceImpl productServiceImpl;

    @Autowired
    ImageDataServiceImpl imageServiceImpl;

    @Autowired
    ImageDataRepo imageDataRepo;

    //@Autowired
    //private AmazonS3 s3_client;

    private AmazonS3 s3_client = AmazonS3ClientBuilder.defaultClient();

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @GetMapping(path = "/healthz")
    public ResponseEntity<UserModel> healthz() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/v1/user")
    @ResponseBody
    public ResponseEntity<Object> AddUserInfo(@RequestBody UserModel userModel) {
        if (userModel.getUsername() == null || userModel.getPassword() == null || userModel.getFirstName() == null || userModel.getLastName() == null) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One/more fields are null");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } else {
            if (Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)" +
                            "*@[^-][A-Za-z0-9-]" +
                            "+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
                    .matcher(userModel.getUsername())
                    .matches()) {
                UserModel user = userServiceImpl.getUserByUsername(userModel.getUsername());
                if (user == null) {
                    if (userModel.getFirstName().length() < 1 || userModel.getLastName().length() < 1 || userModel.getPassword().length() < 4) {
                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("One or more fields are too short");
                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    } else {
                        UserModel newUser = userServiceImpl.addUser(userModel);
                        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
                    }
                } else {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Username already exists");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            } else {
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("Enter a valid email address");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        }
    }

    @GetMapping(path = "/v1/user/{userIdStr}")
    @ResponseBody
    public ResponseEntity<Object> getUserInfo(@PathVariable String userIdStr) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Integer userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("UserId should be int");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        UserModel user = new UserModel();
        user = userServiceImpl.getUserById(userId);

        if (user != null) {
            if (user.getUsername().equals(username)) {
                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User can't access the resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);

            }
        } else {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User can't access the resource");
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping(path = "/v1/user/{userIdStr}")
    public ResponseEntity<Object> updateUserInfo(@PathVariable String userIdStr, @RequestBody UserModel userModel, Principal principal) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Integer userId;
        try {
            userId = Integer.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("UserId should be int");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        UserModel user = new UserModel();
        user = userServiceImpl.getUserById(userId);

        if (user != null) {
            if (user.getUsername().equals(username)) {
                if (userModel.getUsername() == null || userModel.getFirstName() == null || userModel.getLastName() == null || userModel.getPassword() == null) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One/more fields are null ");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                } else {
                    if (user.getUsername().equals(userModel.getUsername())) {
                        if (userModel.getFirstName().length() < 1 || userModel.getLastName().length() < 1 || userModel.getPassword().length() < 4) {
                            ErrorResponseModel errorResponse = new ErrorResponseModel();
                            errorResponse.setErr("Bad Request");
                            errorResponse.setStatus(400);
                            errorResponse.setMessage("One/more fields are short");

                            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                        } else {
                            UserModel updatedUser = userServiceImpl.updateUser(userId, userModel);
                            return new ResponseEntity<>(updatedUser, HttpStatus.NO_CONTENT);
                        }

                    } else {
                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("Please enter correct username ");

                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }
                }
            } else {
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User can't access the resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }
        } else {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User can't access the resource ");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }
    }


    @GetMapping(path = "/v1/product/{productIdStr}")
    @ResponseBody
    public ResponseEntity<Object> getProduct(@PathVariable String productIdStr) {


        Integer productId;
        try {
            productId = Integer.valueOf(productIdStr);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("ProductId should be int ");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel product = new ProductModel();
        product = productServiceImpl.getProductByProductId(productId);

        if (product == null) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Not Found");
            errorResponse.setStatus(404);
            errorResponse.setMessage("Product Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(product, HttpStatus.OK);
        }

    }

    @PostMapping(path = "/v1/product")
    @ResponseBody
    public ResponseEntity<Object> AddProduct(@RequestBody ProductModel productModel) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserModel user = userServiceImpl.getUserByUsername(username);

        if (user == null) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Access denied ! Invalid credentials.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        productModel.setUser(user);

        Integer quantity;
        try {
            quantity = Integer.valueOf(productModel.getQuantity());
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Quantity should be int");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        if (productModel.getName() == null || productModel.getDescription() == null || productModel.getSku() == null || productModel.getManufacturer() == null || productModel.getQuantity() == null) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One/more fields are null");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } else {
            ProductModel searchProduct = new ProductModel();
            searchProduct = productServiceImpl.searchProductBySku(productModel.getSku());

            if (searchProduct == null) {
                if (productModel.getName().length() < 1 || productModel.getDescription().length() < 1 || productModel.getSku().length() < 1 || productModel.getManufacturer().length() < 1) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One/more fields values are short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                } else if (productModel.getQuantity() < 0 || productModel.getQuantity() > 100) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Quantity should be between 0 and 100 ");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                } else {
                    ProductModel newProduct = productServiceImpl.addNewProduct(productModel);
                    return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
                }

            } else {
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("SKU already exists");

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        }

    }

    @PutMapping(path="/v1/product/{productIdStr}")
    public ResponseEntity<Object> updateProductPut(@PathVariable String productIdStr, @RequestBody ProductModel productModel){


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserModel user = userServiceImpl.getUserByUsername(username);

        if(user == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Access denied! Invalid credentials.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        productModel.setUser(user);

        Integer productId;
        try {
            productId = Integer.valueOf(productIdStr);
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
            errorResponse.setMessage("One/more fields are null");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            ProductModel searchProduct = new ProductModel();
            searchProduct = productServiceImpl.searchProductById(productId);

            if(searchProduct == null){
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User can't access the resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }else if(searchProduct.getUser().getUserId() != user.getUserId()){
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User can't access the resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }else{
                if(productModel.getName().length()<1 || productModel.getDescription().length()<1 || productModel.getSku().length()<1 || productModel.getManufacturer().length()<1) {
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One/more fields are short");

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
                        searchProductSku = productServiceImpl.searchProductBySku(productModel.getSku());
                        if(searchProductSku == null){
                            ProductModel updatedProduct = productServiceImpl.updateOldProduct(productModel, productId);
                            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                        }else{
                            if(searchProductSku.getProductId() != productId){
                                ErrorResponseModel errorResponse = new ErrorResponseModel();
                                errorResponse.setErr("Bad Request");
                                errorResponse.setStatus(400);
                                errorResponse.setMessage("SKU already exists");

                                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                            }else{
                                ProductModel updatedProductData = productServiceImpl.updateOldProduct(productModel, productId);
                                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                            }
                        }
                    }else{
                        ProductModel updatedProduct = productServiceImpl.updateOldProduct(productModel, productId);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    }
                }
            }
        }
    }

    @PatchMapping(path="/v1/product/{productIdStr}")
    public ResponseEntity<Object> updateProductPatch(@PathVariable String productIdStr, @RequestBody ProductModel productModel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserModel user = userServiceImpl.getUserByUsername(username);

        if(user == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        productModel.setUser(user);

        Integer productId;
        try {
            productId = Integer.valueOf(productIdStr);
        } catch (NumberFormatException e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel searchProduct = new ProductModel();
        searchProduct = productServiceImpl.searchProductById(productId);

        if(searchProduct == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }else if(searchProduct.getUser().getUserId() != user.getUserId()){
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
                searchProductSku = productServiceImpl.searchProductBySku(productModel.getSku());
                if(searchProductSku == null){
                    ProductModel updatedProduct = productServiceImpl.updateOldProduct(productModel, productId);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }else{
                    if(searchProductSku.getProductId() != productId){
                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("SKU already exists");

                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }else{
                        ProductModel updatedProductData = productServiceImpl.updateOldProduct(productModel, productId);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    }
                }
            }else{
                ProductModel updatedProductData = productServiceImpl.updateOldProduct(productModel, productId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

        }
    }

        @DeleteMapping(path="/v1/product/{productIdStr}")
        public ResponseEntity<Object> deleteProductData(@PathVariable String productIdStr){


            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            UserModel user = userServiceImpl.getUserByUsername(username);

            if(user == null){
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Unauthorized");
                errorResponse.setStatus(401);
                errorResponse.setMessage("Invalid credentials. User access denied.");

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
            }

            Integer productId;
            try {
                productId = Integer.valueOf(productIdStr);
            } catch (NumberFormatException e) {
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("ProductId should be int");

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            ProductModel searchProduct = new ProductModel();
            searchProduct = productServiceImpl.searchProductById(productId);

            if(searchProduct == null){
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Not Found");
                errorResponse.setStatus(404);
                errorResponse.setMessage("Resource not found");

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }else{
                if(searchProduct.getUser().getUserId() == user.getUserId()){
                    productServiceImpl.deleteProduct(productId);
                    List<ImageModel> imageModels = imageDataRepo.findByProductProductId(productId);
                    for(ImageModel image : imageModels){
                        imageDataRepo.deleteById(image.getImageId());
                        s3_client.deleteObject(bucketName, image.getFileName());
                    }

                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }else{
                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Forbidden");
                    errorResponse.setStatus(403);
                    errorResponse.setMessage("User can't access the resource");

                    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
                }
            }
        }


    @ExceptionHandler
    public final ResponseEntity handleException(Exception e, WebRequest req){
        ErrorResponseModel errorResponse = new ErrorResponseModel();
        errorResponse.setErr("Bad Request");
        errorResponse.setStatus(400);
        errorResponse.setMessage("Quantity should be an integer ");

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

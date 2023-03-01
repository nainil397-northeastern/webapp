package com.example.webapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.example.webapp.model.ErrorResponseModel;
import com.example.webapp.model.ImageModel;
import com.example.webapp.model.ProductModel;
import com.example.webapp.model.ProductModel;
import com.example.webapp.model.UserModel;
import com.example.webapp.model.UserModel;
import com.example.webapp.repository.ImageDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ImageDataServiceImpl implements ImageDataService {

    @Autowired
    UserServiceImpl userServiceImpl;

    @Autowired
    ProductServiceImpl productServiceImpl;

    @Autowired
    ImageDataRepo imageDataRepo;

    //@Autowired
    //private AmazonS3 s3_client;

    private AmazonS3 s3_client = AmazonS3ClientBuilder.defaultClient();

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Override
    public ResponseEntity<Object> uploadFileTos3bucket(MultipartFile multipartFile, String productIdStr, String username) {

        //Check if user exists
        UserModel userData = userServiceImpl.getUserByUsername(username);

        if(userData == null){
            //If User does not exist, return Unauthorized error
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
            //If product id is not an integer, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel productData = new ProductModel();
        productData = productServiceImpl.getProductByProductId(productId);

        if(productData == null){
            //If product with this product id does not exist, return Not Found error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if(productData.getUser().getUserId() != userData.getUserId()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("Access denied. User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        try (InputStream input = multipartFile.getInputStream()) {
            try {
                ImageIO.read(input).toString();
            } catch (Exception e) {
                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("Please upload an image");

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        }catch(Exception e){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Please upload again");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        File file;
        String fileName;
        try {
            //converting multipart file to file
            file = convertMultiPartToFile(multipartFile);

            //filename
            fileName = userData.getUserId().toString() + "-" + productData.getProductId().toString() + "-" + LocalDateTime.now(ZoneOffset.UTC) + "-" + multipartFile.getOriginalFilename();
        } catch (Exception e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Image cannot be uploaded. Please try again");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        //Save to S3
        s3_client.putObject(bucketName, fileName, file);
        s3_client.getUrl(bucketName, fileName);

        //Save data to db
        imageDataRepo.saveProductData(productId, fileName, LocalDateTime.now(ZoneOffset.UTC), String.valueOf(s3_client.getUrl(bucketName, fileName)));

        ImageModel imageModel = new ImageModel();
        imageModel = imageDataRepo.getLastAddedImageForProductId(productId);

        file.delete();
        return new ResponseEntity<>(imageModel, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> getImageById(String productIdStr, String username, String imageIdStr) {

//        String bucketName = "csye-6225-mytest";

        //Check if user exists
        UserModel userData = userServiceImpl.getUserByUsername(username);

        if(userData == null){
            //If User does not exist, return Unauthorized error
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
            //If product id is not an integer, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Integer imageId;
        try {
            imageId = Integer.valueOf(imageIdStr);
        } catch (NumberFormatException e) {
            //If product id is not an integer, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Image Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel productData = new ProductModel();
        productData = productServiceImpl.getProductByProductId(productId);

        if(productData == null){
            //If product with this product id does not exist, return Not Found error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if(productData.getUser().getUserId() != userData.getUserId()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("Access denied. User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        ImageModel imageData = new ImageModel();
        imageData = imageDataRepo.findByImageId(imageId);

        if(imageData == null){
            //If product with this product id does not exist, return Not Found error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Image Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if(productData.getProductId() != imageData.getProduct().getProductId()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("Access denied. User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        ImageModel imageModel = imageDataRepo.findByImageId(imageId);

        return new ResponseEntity<>(imageModel, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> getImagesByProductId(String productIdStr, String username) {

//        String bucketName = "csye-6225-mytest";

        //Check if user exists
        UserModel userData = userServiceImpl.getUserByUsername(username);

        if(userData == null){
            //If User does not exist, return Unauthorized error
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
            //If product id is not an integer, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel productData = new ProductModel();
        productData = productServiceImpl.getProductByProductId(productId);

        if(productData == null){
            //If product with this product id does not exist, return Not Found error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if(productData.getUser().getUserId() != userData.getUserId()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("Access denied. User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        List<ImageModel> imageModels = imageDataRepo.findByProductProductId(productId);

        return new ResponseEntity<>(imageModels, HttpStatus.CREATED);
    }

    public ResponseEntity<Object> deleteImageById(String productIdStr, String username, String imageIdStr) {

//        String bucketName = "csye-6225-mytest";

        //Check if user exists
        UserModel userData = userServiceImpl.getUserByUsername(username);

        if(userData == null){
            //If User does not exist, return Unauthorized error
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
            //If product id is not an integer, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Integer imageId;
        try {
            imageId = Integer.valueOf(imageIdStr);
        } catch (NumberFormatException e) {
            //If product id is not an integer, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Image Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel productData = new ProductModel();
        productData = productServiceImpl.getProductByProductId(productId);

        if(productData == null){
            //If product with this product id does not exist, return Not Found error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if(productData.getUser().getUserId() != userData.getUserId()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("Access denied. User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        ImageModel imageData = new ImageModel();
        imageData = imageDataRepo.findByImageId(imageId);

        if(imageData == null){
            //If product with this product id does not exist, return Not Found error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Image Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if(productData.getProductId() != imageData.getProduct().getProductId()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("Access denied. User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        ImageModel imageModel = imageDataRepo.findByImageId(imageId);
        imageDataRepo.deleteById(imageId);

        s3_client.deleteObject(bucketName, imageModel.getFileName());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}

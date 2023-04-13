package com.example.webapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.webapp.model.ErrorResponseModel;
import com.example.webapp.model.ImageModel;
import com.example.webapp.model.ProductModel;
import com.example.webapp.model.ProductModel;
import com.example.webapp.model.UserAccountModel;
import com.example.webapp.model.UserAccountModel;
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
    UserAccountServiceImpl userServiceImpl;

    @Autowired
    ProductServiceImpl productServiceImpl;

    @Autowired
    ImageDataRepo imageDataRepo;

    private AmazonS3 s3_client = AmazonS3ClientBuilder.defaultClient();

    @Value("${AWS_REGION}")
    private String region;

    @Value("${AWS_BUCKET_NAME}")
    private String bucketName;

    @Override
    public ResponseEntity<Object> uploadFileTos3bucket(MultipartFile multipartFile, String productIdStr, String username) {

        if(multipartFile.getContentType() == null || multipartFile.isEmpty()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Please Upload Image Again");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

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
            productId = Integer.valueOf(productIdStr);
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
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if(productData.getUser().getUserId() != userData.getUserId()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("Access denied. User can't access resource");

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

        String fileName;
        try {
            fileName = userData.getUserId().toString() + "-" + productData.getProductId().toString() + "-" + LocalDateTime.now(ZoneOffset.UTC) + "-" + multipartFile.getOriginalFilename();
            System.out.println(fileName);
            InputStream file = multipartFile.getInputStream();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(multipartFile.getSize());
            objectMetadata.setContentType(multipartFile.getContentType());
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,fileName,file,objectMetadata);
            s3_client.putObject(putObjectRequest);

        } catch (Exception e) {
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Image not uploaded. Retry again");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

             s3_client.getUrl(bucketName, fileName);


        imageDataRepo.saveProductData(productId, fileName, LocalDateTime.now(ZoneOffset.UTC), String.valueOf(s3_client.getUrl(bucketName, fileName)));

        ImageModel imageModel = new ImageModel();

        imageModel = imageDataRepo.getLastAddedImageForProductId(productId);

        return new ResponseEntity<>(imageModel, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> getImageById(String productIdStr, String username, String imageIdStr) {

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
            productId = Integer.valueOf(productIdStr);
        } catch (NumberFormatException e) {
            //If product id is not an integer, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("ProductID should be integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Integer imageId;
        try {
            imageId = Integer.valueOf(imageIdStr);
        } catch (NumberFormatException e) {

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("ImageID should be integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel productData = new ProductModel();
        productData = productServiceImpl.getProductByProductId(productId);

        if(productData == null){

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
            errorResponse.setMessage("Access denied.Can't access resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        ImageModel imageData = new ImageModel();
        imageData = imageDataRepo.findByImageId(imageId);

        if(imageData == null){

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("ImageID doesn't exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if(productData.getProductId() != imageData.getProduct().getProductId()){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("Access denied. Can't access resource");
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }
        ImageModel imageModel = imageDataRepo.findByImageId(imageId);
        return new ResponseEntity<>(imageModel, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> getImagesByProductId(String productIdStr, String username) {

        UserAccountModel userData = userServiceImpl.getUserDataByUsername(username);

        if(userData == null){
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. Access denied.");

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
            errorResponse.setMessage("ProductID should be integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductModel productData = new ProductModel();
        productData = productServiceImpl.getProductByProductId(productId);

        if(productData == null){
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
            errorResponse.setMessage("Access denied. User can't access resource");
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        List<ImageModel> imageModels = imageDataRepo.findByProductProductId(productId);
        return new ResponseEntity<>(imageModels, HttpStatus.OK);
    }

    public ResponseEntity<Object> deleteImageById(String productIdStr, String username, String imageIdStr) {

        UserAccountModel userData = userServiceImpl.getUserDataByUsername(username);

        if(userData == null){

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. Access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

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

        Integer imageId;
        try {
            imageId = Integer.valueOf(imageIdStr);
        } catch (NumberFormatException e) {

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
            errorResponse.setMessage("Access denied. User can't access resource");
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        ImageModel imageModel = imageDataRepo.findByImageId(imageId);
        imageDataRepo.deleteById(imageId);

        s3_client.deleteObject(bucketName, imageModel.getFileName());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

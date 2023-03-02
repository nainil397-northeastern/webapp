package com.example.webapp.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.webapp.model.ErrorResponseModel;
import com.example.webapp.model.ImageModel;
import com.example.webapp.model.ProductDataModel;
import com.example.webapp.model.UserDataModel;
import com.example.webapp.repository.ImageDataRepo;
import com.example.webapp.repository.ProductDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
public class ProductDataServiceImpl implements ProductDataService{

    @Autowired
    ProductDataRepo productDataRepo;

    @Autowired
    UserDataServiceImpl userServiceImpl;

    @Autowired
    ImageDataRepo imageDataRepo;

//    @Autowired
//    private AmazonS3 s3_client;

    private AmazonS3 s3_client = AmazonS3ClientBuilder.defaultClient();

    @Value("${AWS_REGION}")
    private String region;

    @Value("${AWS_BUCKET_NAME}")
    private String bucketName;

    @Override
    public ProductDataModel getProductDataByProductId(Integer productId) {
        /*This function finds a product by product id*/

        ProductDataModel productData = productDataRepo.findByProductId(productId);
        return productData;
    }

    @Override
    public List<ProductDataModel> getProductDataByUserId(Integer userId) {
        /*This function find the product data by the id of the user*/

        List<ProductDataModel> productsByUserId = productDataRepo.findByUserUserId(userId);
        return productsByUserId;
    }

    @Override
    public ProductDataModel addNewProductData(ProductDataModel productModel) {
        /*This function saves data of the new product*/

        //Save new product data
        productDataRepo.saveProductData(productModel.getName(),
                productModel.getDescription(),
                productModel.getSku(),
                productModel.getManufacturer(),
                productModel.getQuantity(),
                LocalDateTime.now(ZoneOffset.UTC),
                LocalDateTime.now(ZoneOffset.UTC),
                productModel.getUser().getUserId());

        //Fetch the newly saved product to return
        ProductDataModel newProductData = productDataRepo.getLastAddedProductForUserId(productModel.getUser().getUserId());
        return newProductData;
    }

    @Override
    public ProductDataModel updateOldProductData(ProductDataModel productModel, Integer productId) {
        /*This product updates the previously saved product's details*/

        productDataRepo.updateProductData(productModel.getName(),
                productModel.getDescription(),
                productModel.getSku(),
                productModel.getManufacturer(),
                productModel.getQuantity(),
                LocalDateTime.now(ZoneOffset.UTC),
                productId);

        //Fetch the updated product for display
        ProductDataModel updatedProductData = productDataRepo.findByProductId(productId);
        return updatedProductData;
    }

    @Override
    public void deleteProductData(Integer productId) {
        /*This function deletes a previously saved product*/

        productDataRepo.deleteById(productId);
    }

    @Override
    public ProductDataModel searchProductDataBySku(String sku){
        /*This function searches a product by SKU*/

        ProductDataModel searchProduct = productDataRepo.findBySku(sku);
        return searchProduct;

    }

    @Override
    public ProductDataModel searchProductDataById(Integer productId){
        /*This function searches a product by its product id*/

        ProductDataModel searchProduct = productDataRepo.findByProductId(productId);
        return searchProduct;
    }

    @Override
    public ResponseEntity<Object> addProductData(ProductDataModel productDataModel, String username){
        /*This function adds a new product for authenticated users*/

        //Check if user exists
        UserDataModel userData = userServiceImpl.getUserDataByUsername(username);

        if(userData == null){
            //If User does not exist, return Unauthorized error
            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        //Else set the user in the product object
        productDataModel.setUser(userData);

        //Check if quantity is an integer
        Integer quantity;
        try {
            quantity = Integer.valueOf(productDataModel.getQuantity());
        } catch (NumberFormatException e) {
            //If quantity is not an integer, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Quantity should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        //Check if all mandatory fields are non null
        if(productDataModel.getName() == null || productDataModel.getDescription() == null || productDataModel.getSku() == null || productDataModel.getManufacturer() == null || productDataModel.getQuantity() == null){
            //If any field is null, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One or more fields are null");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            //Else initialize a new object and search for the sku in the request body

            ProductDataModel searchProduct = new ProductDataModel();
            searchProduct = searchProductDataBySku(productDataModel.getSku());

            if(searchProduct == null){
                //If sku does not exist, proceed further
                if(productDataModel.getName().length()<1 || productDataModel.getDescription().length()<1 || productDataModel.getSku().length()<1 || productDataModel.getManufacturer().length()<1) {
                    //If any mandatory field is very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else if(productDataModel.getQuantity()<0 || productDataModel.getQuantity() > 100){
                    //If Quantity is not between 0 and 100, both inclusive, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Quantity should be between 0 and 100, both inclusive");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else{
                    //If all conditions satisfied, add the product to the database

                    ProductDataModel newProductData = addNewProductData(productDataModel);
                    return new ResponseEntity<>(newProductData, HttpStatus.CREATED);
                }

            }else{
                //If sku already exists return Bad Request error

                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("SKU already exists");

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Override
    public ResponseEntity<Object> getProductData(String productIdStr){
        //Check if the product id is an integer
        Integer productId;
        try {
            productId = Integer.valueOf(productIdStr);
        } catch (NumberFormatException e) {
            //If not an integer, return Bad Request Error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        //Try searching the product by its product id
        ProductDataModel productData = new ProductDataModel();
        productData = getProductDataByProductId(productId);

        if(productData == null){
            //If product with this product id does not exist, return Not Found error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Not Found");
            errorResponse.setStatus(404);
            errorResponse.setMessage("Product Id does not exist");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }else{
            //If product id exists, return the product information

            return new ResponseEntity<>(productData, HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Object> updateProductDataPut(String productIdStr, ProductDataModel productDataModel, String username){
        //Search the user by the username
        UserDataModel userData = userServiceImpl.getUserDataByUsername(username);


        if(userData == null){
            //If user does not exist return Unauthorized error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        //Else set the user in the product object
        productDataModel.setUser(userData);

        //Check if the product id is an integer
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

        if(productDataModel.getName() == null || productDataModel.getDescription() == null || productDataModel.getSku() == null || productDataModel.getManufacturer() == null || productDataModel.getQuantity() == null ){
            //If any mandatory fields are null, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One or more fields are null");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            //Try searching product by its id
            ProductDataModel searchProduct = new ProductDataModel();
            searchProduct = searchProductDataById(productId);

            if(searchProduct == null){
                //If product does not exist, return Forbidden error

                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }else if(searchProduct.getUser().getUserId() != userData.getUserId()){
                //If the user id of authenticated user and the product owner_user_id do not match, return Forbidden error

                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }else{
                if(productDataModel.getName().length()<1 || productDataModel.getDescription().length()<1 || productDataModel.getSku().length()<1 || productDataModel.getManufacturer().length()<1) {
                    //If any of the mandatory fields are very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else if(productDataModel.getQuantity()<0 || productDataModel.getQuantity() > 100){
                    //If the quantity is less than 0 or greater than 100, return a Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Quantity should be between 0 and 100, both inclusive");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else{
                    if(searchProduct.getSku() != productDataModel.getSku()){
                        //Check if the product sku is being modified

                        ProductDataModel searchProductSku = new ProductDataModel();
                        searchProductSku = searchProductDataBySku(productDataModel.getSku());

                        if(searchProductSku == null){
                            //If sku is being modified and is unique, allow update

                            ProductDataModel updatedProductData = updateOldProductData(productDataModel, productId);
                            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                        }else{
                            if(searchProductSku.getProductId() != productId){
                                //If product skew is being modified and is non unique, return Bad Request Error

                                ErrorResponseModel errorResponse = new ErrorResponseModel();
                                errorResponse.setErr("Bad Request");
                                errorResponse.setStatus(400);
                                errorResponse.setMessage("SKU already exists");

                                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                            }else{
                                //If product sku is being modified and non unique, but of the same product - allow update
                                ProductDataModel updatedProductData = updateOldProductData(productDataModel, productId);
                                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                            }
                        }
                    }else{
                        //If product sku is remaining same, allow update

                        ProductDataModel updatedProductData = updateOldProductData(productDataModel, productId);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    }
                }
            }
        }
    }

    @Override
    public ResponseEntity<Object> updateProductDataPatch(String productIdStr, ProductDataModel productDataModel, String username){

        //Check if the user exists
        UserDataModel userData = userServiceImpl.getUserDataByUsername(username);

        if(userData == null){
            //If user does not exist, return Unauthorized error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        //Set the user in the project object
        productDataModel.setUser(userData);

        //Check if product id is integer
        Integer productId;
        try {
            productId = Integer.valueOf(productIdStr);
        } catch (NumberFormatException e) {
            //If product id is not an integer, return Bad Request Error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Product Id should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        //Search the product by the product id
        ProductDataModel searchProduct = new ProductDataModel();
        searchProduct = searchProductDataById(productId);

        if(searchProduct == null){
            //If product does not exist, return Forbidden Error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }else if(searchProduct.getUser().getUserId() != userData.getUserId()){
            //If product exists but was created by another user, return Forbidden error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Forbidden");
            errorResponse.setStatus(403);
            errorResponse.setMessage("User cannot access this resource");

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }else{
            if(productDataModel.getName() == null && productDataModel.getDescription() == null && productDataModel.getSku() == null && productDataModel.getManufacturer() == null && productDataModel.getQuantity() == null ){
                //If all the optional fields are null, return Bad Request error

                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("Please enter at least one mandatory field");

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            if(productDataModel.getName()!= null) {
                if (productDataModel.getName().length() < 1) {
                    //If name is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the name in the product object

                productDataModel.setName(searchProduct.getName());
            }

            if(productDataModel.getDescription()!= null) {
                if (productDataModel.getDescription().length() < 1) {
                    //If description is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the description in the product object

                productDataModel.setDescription(searchProduct.getDescription());
            }

            if(productDataModel.getSku() != null) {
                if (productDataModel.getSku().length() < 1) {
                    //If sku is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the sku in the product object

                productDataModel.setSku(searchProduct.getSku());
            }

            if(productDataModel.getSku() != null) {
                if (productDataModel.getSku().length() < 1) {
                    //If sku is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the sku in the product object

                productDataModel.setSku(searchProduct.getSku());
            }

            if(productDataModel.getManufacturer() != null) {
                if (productDataModel.getManufacturer().length() < 1) {
                    //If manufacturer is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the manufacturer in the product object

                productDataModel.setManufacturer(searchProduct.getManufacturer());
            }

            if(productDataModel.getQuantity() != null) {
                if (productDataModel.getQuantity() < 0 || productDataModel.getQuantity() > 100) {
                    //If quantity is present, but not between 0 and 100 both inclusive, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Quantity should be between 0 and 100, both inclusive");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                productDataModel.setQuantity(searchProduct.getQuantity());
            }


            if(searchProduct.getSku() != productDataModel.getSku()){
                //If sku is being updated, try searching if sku exists

                ProductDataModel searchProductSku = new ProductDataModel();
                searchProductSku = searchProductDataBySku(productDataModel.getSku());

                if(searchProductSku == null){
                    //If sku is unique, allow update

                    ProductDataModel updatedProductData = updateOldProductData(productDataModel, productId);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }else{
                    if(searchProductSku.getProductId() != productId){
                        //if sku exists and is of different product, return Bad Request error

                        ErrorResponseModel errorResponse = new ErrorResponseModel();
                        errorResponse.setErr("Bad Request");
                        errorResponse.setStatus(400);
                        errorResponse.setMessage("SKU already exists");

                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }else{
                        //Else allow update

                        ProductDataModel updatedProductData = updateOldProductData(productDataModel, productId);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    }
                }
            }else{
                //If same sku, allow update
                ProductDataModel updatedProductData = updateOldProductData(productDataModel, productId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
    }

    @Override
    public ResponseEntity<Object> deleteProductData(String productIdStr, String username){
        //Check if the user exists
        UserDataModel userData = userServiceImpl.getUserDataByUsername(username);

        if(userData == null){
            //If user does not exist, return Unauthorized error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        //Check if the product id is an integer
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

        //Try searching the product by product id in the database
        ProductDataModel searchProduct = new ProductDataModel();
        searchProduct = searchProductDataById(productId);

        if(searchProduct == null){
            //If product id does not exist, return Not Found error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Not Found");
            errorResponse.setStatus(404);
            errorResponse.setMessage("Resource not found");

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }else{
            if(searchProduct.getUser().getUserId() == userData.getUserId()){
                //If product is found and was created by authenticated user, allow delete

                deleteProductData(productId);
                List<ImageModel> imageModels = imageDataRepo.findByProductProductId(productId);
                for(ImageModel image : imageModels){
                    imageDataRepo.deleteById(image.getImageId());
                    s3_client.deleteObject(bucketName, image.getFileName());
                }
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                //If product was found but created by another user, return Forbidden error

                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Forbidden");
                errorResponse.setStatus(403);
                errorResponse.setMessage("User cannot access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }
        }
    }

}

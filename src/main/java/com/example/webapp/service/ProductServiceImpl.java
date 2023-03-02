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
import com.example.webapp.model.ProductModel;
import com.example.webapp.model.UserModel;
import com.example.webapp.repository.ImageDataRepo;
import com.example.webapp.repository.ProductRepository;
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
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepo;

    @Autowired
    UserServiceImpl userServiceImpl;

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
    public ProductModel getProductByProductId(Integer productId) {


        ProductModel product = productRepo.findByProductId(productId);
        return product;
    }

    @Override
    public List<ProductModel> getProductByUserId(Integer userId) {

        List<ProductModel> productsByUserId = productRepo.findByUserUserId(userId);
        return productsByUserId;
    }

    @Override
    public ProductModel addNewProduct(ProductModel productModel) {

        productRepo.saveProduct(productModel.getName(),
                productModel.getDescription(),
                productModel.getSku(),
                productModel.getManufacturer(),
                productModel.getQuantity(),
                LocalDateTime.now(ZoneOffset.UTC),
                LocalDateTime.now(ZoneOffset.UTC),
                productModel.getUser().getUserId());

        ProductModel newProduct = productRepo.getLastAddedProductForUserId(productModel.getUser().getUserId());
        return newProduct;
    }

    @Override
    public ProductModel updateOldProduct(ProductModel productModel, Integer productId) {

        productRepo.updateProduct(productModel.getName(),
                productModel.getDescription(),
                productModel.getSku(),
                productModel.getManufacturer(),
                productModel.getQuantity(),
                LocalDateTime.now(ZoneOffset.UTC),
                productId);

        ProductModel updatedProduct = productRepo.findByProductId(productId);
        return updatedProduct;
    }


    @Override
    public void deleteProduct(Integer productId) {

        productRepo.deleteById(productId);
    }

    @Override
    public ProductModel searchProductBySku(String sku) {
        ProductModel searchProduct = productRepo.findBySku(sku);
        return searchProduct;
    }

    @Override
    public ProductModel searchProductById (Integer productId){
            ProductModel searchProduct = productRepo.findByProductId(productId);
            return searchProduct;

    }

    @Override
    public ResponseEntity<Object> addProduct(ProductModel productModel, String username){
        /*This function adds a new product for authenticated users*/

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

        //Else set the user in the product object
        productModel.setUser(userData);

        //Check if quantity is an integer
        Integer quantity;
        try {
            quantity = Integer.valueOf(productModel.getQuantity());
        } catch (NumberFormatException e) {
            //If quantity is not an integer, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("Quantity should be an integer");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        //Check if all mandatory fields are non null
        if(productModel.getName() == null || productModel.getDescription() == null || productModel.getSku() == null || productModel.getManufacturer() == null || productModel.getQuantity() == null){
            //If any field is null, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One or more fields are null");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            //Else initialize a new object and search for the sku in the request body

            ProductModel searchProduct = new ProductModel();
            searchProduct = searchProductBySku(productModel.getSku());

            if(searchProduct == null){
                //If sku does not exist, proceed further
                if(productModel.getName().length()<1 || productModel.getDescription().length()<1 || productModel.getSku().length()<1 || productModel.getManufacturer().length()<1) {
                    //If any mandatory field is very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else if(productModel.getQuantity()<0 || productModel.getQuantity() > 100){
                    //If Quantity is not between 0 and 100, both inclusive, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Quantity should be between 0 and 100, both inclusive");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else{
                    //If all conditions satisfied, add the product to the database

                    ProductModel newProductData = addNewProduct(productModel);
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
    public ResponseEntity<Object> getProduct(String productIdStr){
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
        ProductModel productData = new ProductModel();
        productData = getProductByProductId(productId);

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
    public ResponseEntity<Object> updateProductPut(String productIdStr, ProductModel productModel, String username){
        //Search the user by the username
        UserModel userData = userServiceImpl.getUserByUsername(username);


        if(userData == null){
            //If user does not exist return Unauthorized error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        //Else set the user in the product object
        productModel.setUser(userData);

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

        if(productModel.getName() == null || productModel.getDescription() == null || productModel.getSku() == null || productModel.getManufacturer() == null || productModel.getQuantity() == null ){
            //If any mandatory fields are null, return Bad Request error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Bad Request");
            errorResponse.setStatus(400);
            errorResponse.setMessage("One or more fields are null");

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            //Try searching product by its id
            ProductModel searchProduct = new ProductModel();
            searchProduct = searchProductById(productId);

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
                if(productModel.getName().length()<1 || productModel.getDescription().length()<1 || productModel.getSku().length()<1 || productModel.getManufacturer().length()<1) {
                    //If any of the mandatory fields are very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else if(productModel.getQuantity()<0 || productModel.getQuantity() > 100){
                    //If the quantity is less than 0 or greater than 100, return a Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("Quantity should be between 0 and 100, both inclusive");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }else{
                    if(searchProduct.getSku() != productModel.getSku()){
                        //Check if the product sku is being modified

                        ProductModel searchProductSku = new ProductModel();
                        searchProductSku = searchProductBySku(productModel.getSku());

                        if(searchProductSku == null){
                            //If sku is being modified and is unique, allow update

                            ProductModel updatedProductData = updateOldProduct(productModel, productId);
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
                                ProductModel updatedProductData = updateOldProduct(productModel, productId);
                                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                            }
                        }
                    }else{
                        //If product sku is remaining same, allow update

                        ProductModel updatedProductData = updateOldProduct(productModel, productId);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    }
                }
            }
        }
    }

    @Override
    public ResponseEntity<Object> updateProductPatch(String productIdStr, ProductModel productModel, String username){

        //Check if the user exists
        UserModel userData = userServiceImpl.getUserByUsername(username);

        if(userData == null){
            //If user does not exist, return Unauthorized error

            ErrorResponseModel errorResponse = new ErrorResponseModel();
            errorResponse.setErr("Unauthorized");
            errorResponse.setStatus(401);
            errorResponse.setMessage("Invalid credentials. User access denied.");

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        //Set the user in the project object
        productModel.setUser(userData);

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
        ProductModel searchProduct = new ProductModel();
        searchProduct = searchProductById(productId);

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
            if(productModel.getName() == null && productModel.getDescription() == null && productModel.getSku() == null && productModel.getManufacturer() == null && productModel.getQuantity() == null ){
                //If all the optional fields are null, return Bad Request error

                ErrorResponseModel errorResponse = new ErrorResponseModel();
                errorResponse.setErr("Bad Request");
                errorResponse.setStatus(400);
                errorResponse.setMessage("Please enter at least one mandatory field");

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            if(productModel.getName()!= null) {
                if (productModel.getName().length() < 1) {
                    //If name is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the name in the product object

                productModel.setName(searchProduct.getName());
            }

            if(productModel.getDescription()!= null) {
                if (productModel.getDescription().length() < 1) {
                    //If description is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the description in the product object

                productModel.setDescription(searchProduct.getDescription());
            }

            if(productModel.getSku() != null) {
                if (productModel.getSku().length() < 1) {
                    //If sku is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the sku in the product object

                productModel.setSku(searchProduct.getSku());
            }

            if(productModel.getSku() != null) {
                if (productModel.getSku().length() < 1) {
                    //If sku is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the sku in the product object

                productModel.setSku(searchProduct.getSku());
            }

            if(productModel.getManufacturer() != null) {
                if (productModel.getManufacturer().length() < 1) {
                    //If manufacturer is present, but very short in length, return Bad Request error

                    ErrorResponseModel errorResponse = new ErrorResponseModel();
                    errorResponse.setErr("Bad Request");
                    errorResponse.setStatus(400);
                    errorResponse.setMessage("One or more fields are too short");

                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }else {
                //Else set the manufacturer in the product object

                productModel.setManufacturer(searchProduct.getManufacturer());
            }

            if(productModel.getQuantity() != null) {
                if (productModel.getQuantity() < 0 || productModel.getQuantity() > 100) {
                    //If quantity is present, but not between 0 and 100 both inclusive, return Bad Request error

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
                //If sku is being updated, try searching if sku exists

                ProductModel searchProductSku = new ProductModel();
                searchProductSku = searchProductBySku(productModel.getSku());

                if(searchProductSku == null){
                    //If sku is unique, allow update

                    ProductModel updatedProduct = updateOldProduct(productModel, productId);
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

                        ProductModel updatedProduct = updateOldProduct(productModel, productId);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    }
                }
            }else{
                //If same sku, allow update
                ProductModel updatedProduct = updateOldProduct(productModel, productId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
    }

    @Override
    public ResponseEntity<Object> deleteProduct(String productIdStr, String username){
        //Check if the user exists
        UserModel userData = userServiceImpl.getUserByUsername(username);

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
        ProductModel searchProduct = new ProductModel();
        searchProduct = searchProductById(productId);

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

                deleteProduct(productId);
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

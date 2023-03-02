package com.example.webapp.service;

import com.example.webapp.model.ProductModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ProductService {

    public ProductModel getProductByProductId(Integer productId);

    public List<ProductModel> getProductByUserId(Integer userId);

    public ProductModel addNewProduct(ProductModel productModel);

    public ProductModel updateOldProduct(ProductModel productModel, Integer productId);

    public void deleteProduct(Integer productId);

    public ProductModel searchProductById (Integer productId);

    public ProductModel searchProductBySku(String sku);

    public ResponseEntity<Object> addProduct(ProductModel productModel, String username);

    public ResponseEntity<Object> getProduct(String productIdStr);

    public ResponseEntity<Object> updateProductPut(String productIdStr, ProductModel productModel, String username);

    public ResponseEntity<Object> updateProductPatch(String productIdStr, ProductModel productModel, String username);

    public ResponseEntity<Object> deleteProduct(String productIdStr, String username);
}



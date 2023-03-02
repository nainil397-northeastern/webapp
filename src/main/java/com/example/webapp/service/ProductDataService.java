package com.example.webapp.service;

import com.example.webapp.model.ProductDataModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ProductDataService {
    public ProductDataModel getProductDataByProductId(Integer productId);

    public List<ProductDataModel> getProductDataByUserId(Integer userId);

    public ProductDataModel addNewProductData(ProductDataModel productModel);

    public ProductDataModel updateOldProductData(ProductDataModel productModel, Integer productId);

    public void deleteProductData(Integer productId);

    public ProductDataModel searchProductDataBySku(String sku);

    public ProductDataModel searchProductDataById(Integer productId);

    public ResponseEntity<Object> addProductData(ProductDataModel productDataModel, String username);

    public ResponseEntity<Object> getProductData(String productIdStr);

    public ResponseEntity<Object> updateProductDataPut(String productIdStr, ProductDataModel productDataModel, String username);

    public ResponseEntity<Object> updateProductDataPatch(String productIdStr, ProductDataModel productDataModel, String username);

    public ResponseEntity<Object> deleteProductData(String productIdStr, String username);
}

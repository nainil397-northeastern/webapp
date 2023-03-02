package com.example.webapp.service;

import com.example.webapp.model.ProductModel;
import com.example.webapp.model.UserAccountModel;

import java.util.List;

public interface ProductService {

    public ProductModel getProductByProductId(Integer productId);
    public List<ProductModel> getProductByUserId(Integer userId);

    public ProductModel addProductData(ProductModel productModel);

    public ProductModel updateProductData(Integer productId, ProductModel productModel);

    public void deleteProductData(Integer productId);

}

package com.example.webapp.service;

import com.example.webapp.model.ProductModel;
import java.util.List;

public interface ProductService {

    public ProductModel getProductByProductId(Integer productId);

    public List<ProductModel> getProductByUserId(Integer userId);

    public ProductModel addNewProduct(ProductModel productModel);

    public ProductModel updateOldProduct(ProductModel productModel, Integer productId);

    public void deleteProduct(Integer productId);

    public ProductModel searchProductById (Integer productId);

    public ProductModel searchProductBySku(String sku);
}

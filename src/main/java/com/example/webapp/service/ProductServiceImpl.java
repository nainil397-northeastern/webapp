package com.example.webapp.service;

import com.example.webapp.model.ProductModel;
import com.example.webapp.model.UserAccountModel;
import com.example.webapp.repository.ProductRepository;
import com.example.webapp.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;


    @Override
    public List<ProductModel> getProductByUserId(Integer userId) {
        List<ProductModel> products = productRepository.findByUserUserId(userId);
        return products;
    }
    @Override
    public ProductModel getProductByProductId(Integer productId) {
        ProductModel product = productRepository.findByProductId(productId);
        return product;
    }


    @Override
    public ProductModel addProductData(ProductModel productModel) {
        productRepository.saveProduct(productModel.getName(),productModel.getDescription()
        ,productModel.getSku(),productModel.getManufacturer(),productModel.getQuantity(),
                LocalDateTime.now(ZoneOffset.UTC),LocalDateTime.now(ZoneOffset.UTC),productModel.getUser().getUserId());

        ProductModel newProduct = productRepository.getLatestAddedProduct(productModel.getUser().getUserId());
        return newProduct;
    }

    @Override
    public ProductModel updateProductData(Integer productId, ProductModel productModel) {

        productRepository.updateProduct(productModel.getName(),productModel.getDescription()
                ,productModel.getSku(),productModel.getManufacturer(),productModel.getQuantity(),
                LocalDateTime.now(ZoneOffset.UTC),productId);
        ProductModel updatedProduct = productRepository.findByProductId(productId);
        return updatedProduct;
    }

    @Override
    public void deleteProductData(Integer productId){
        productRepository.deleteById(productId);
    }

    public ProductModel searchProductDataBySku(String sku){
        ProductModel searchProduct = productRepository.findBySku(sku);
        return searchProduct;

    }

    public ProductModel searchProductDataById(Integer productId){
        ProductModel searchProduct = productRepository.findByProductId(productId);
        return searchProduct;

    }
}

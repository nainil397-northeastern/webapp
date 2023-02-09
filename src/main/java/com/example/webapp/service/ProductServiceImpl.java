package com.example.webapp.service;

import com.example.webapp.model.ProductModel;
import com.example.webapp.repository.ProductRepository;
import org.hibernate.event.internal.DefaultPersistOnFlushEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepo;

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
}

package com.example.webapp.repository;

import com.example.webapp.model.ProductModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductModel,Integer> {

    List<ProductModel> findByUserUserId(Integer userId);

    ProductModel findByProductId(Integer productId);

    ProductModel findBySku(String sku);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO product (name, description, sku, manufacturer, quantity, date_added, date_last_updated, owner_user_id) VALUES ( ?1 , ?2 , ?3 , ?4 , ?5, ?6, ?7, ?8)", nativeQuery = true)
    void saveProduct(String name, String description, String sku, String manufacturer, Integer quantity, LocalDateTime dateAdded, LocalDateTime dateLastUpdated, Integer ownerUserId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE product SET name = ?1, description = ?2, sku = ?3, manufacturer = ?4, quantity = ?5, date_last_updated = ?6 WHERE id = ?7", nativeQuery = true)
    void updateProduct(String name, String description, String sku, String manufacturer, Integer quantity, LocalDateTime dateLastUpdated, Integer productId);

    @Query(value = "SELECT * FROM product WHERE owner_user_id=?1 ORDER BY date_added DESC LIMIT 1",nativeQuery = true)
    ProductModel getLastAddedProductForUserId(Integer userId);

}

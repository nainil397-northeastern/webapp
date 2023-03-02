package com.example.webapp.repository;

import com.example.webapp.model.ImageModel;
import com.example.webapp.model.ProductDataModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ImageDataRepo extends JpaRepository<ImageModel,Integer> {

    ImageModel findByImageId(Integer imageId);

    List<ImageModel> findByProductProductId(Integer productId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO image (product_id, file_name, date_created, s3_bucket_path) VALUES ( ?1 , ?2 , ?3 , ?4)", nativeQuery = true)
    void saveProductData(Integer productId, String fileName, LocalDateTime dateCreated, String s3BucketPath);

    @Query(value = "SELECT * FROM image WHERE product_id=?1 ORDER BY date_created DESC LIMIT 1",nativeQuery = true)
    ImageModel getLastAddedImageForProductId(Integer productId);
}

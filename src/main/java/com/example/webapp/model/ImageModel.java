package com.example.webapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@Entity
@Table(name = "image")
public class ImageModel {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="image_id")
    @JsonProperty(value = "image_id")
    private Integer imageId;

    @ManyToOne
    @JoinColumn(name="product_id",referencedColumnName = "id")
    @JsonProperty(value="product_id", access = READ_ONLY)
    @JsonIdentityReference(alwaysAsId = true)
    private ProductDataModel product;

    @JsonProperty(value = "file_name", access = READ_ONLY)
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "date_created")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty(value = "date_created", access = READ_ONLY)
    private ZonedDateTime date_added;

    @JsonProperty(value = "s3_bucket_path", access = READ_ONLY)
    @Column(name = "s3_bucket_path")
    private String s3BucketPath;

}

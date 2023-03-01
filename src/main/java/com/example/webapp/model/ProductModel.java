package com.example.webapp.model;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@Entity

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")

@Table(name = "product")
public class ProductModel {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    @JsonProperty(value = "id")
    private Integer productId;

    @Column(name = "name")
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "description")
    @Column(name = "description")
    private String description;

    @Column(name = "sku")
    @JsonProperty(value = "sku")
    private String sku;

    @JsonProperty(value = "manufacturer")
    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name="quantity")
    @JsonProperty(value = "quantity")
    private Integer quantity;

    @Column(name = "date_added")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty(value = "date_added", access = READ_ONLY)
    private ZonedDateTime date_added;

    @Column(name = "date_last_updated")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty(value = "date_last_updated", access = READ_ONLY)
    private ZonedDateTime date_last_updated;

    @ManyToOne
    @JoinColumn(name="owner_user_id",referencedColumnName = "id")
    @JsonProperty(value="owner_user_id", access = READ_ONLY)
    @JsonIdentityReference(alwaysAsId = true)
    private UserModel user;
}

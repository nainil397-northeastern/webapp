package com.example.webapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponseModel {
    @JsonProperty(value = "message")
    public String message;

    @JsonProperty(value = "status")
    public Integer status;

    @JsonProperty(value = "error")
    public String err;

}

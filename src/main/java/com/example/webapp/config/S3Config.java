package com.example.webapp.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class S3Config {

//    @Value("${aws.access.key.id}")
//    private String accessKey;
//
//    @Value("${aws.secret.access.key}")
//    private String secretKey;
//
//    @Value("${aws.s3.region}")
//    private String region;
//
//    public AWSCredentials credentials() {
//        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
//        return credentials;
//    }
//
//    @Bean
//    @Primary
//    public AmazonS3 getS3Client()
//    {
//        AmazonS3 amazonS3= AmazonS3ClientBuilder
//                .standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials()))
//                .withRegion(region)
//                .build();
//        return amazonS3;
//    }
}
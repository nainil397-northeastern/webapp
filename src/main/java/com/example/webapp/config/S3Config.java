package com.example.webapp.config;

import com.amazonaws.auth.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

//@Configuration
public class S3Config {
//
//     @Value("${aws.access.key.id}")
//     private String accessKey;
//
//     @Value("${aws.secret.access.key}")
//     private String secretKey;
//
//     @Value("${aws.s3.region}")
//     private String region;
//
//     public AWSCredentials credentials() {
//         AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
//         return credentials;
//     }

//    @Value("${cloud.aws.assumeRoleARN:}")
//    private String assumeRoleARN;
//
//    @Autowired
//
//    private AWSCredentialsProvider awsCredentialsProvider;
//
//    @Bean
//    @Primary
//    public AWSCredentialsProvider awsCredentialsProvider() {
//        log.info("Assuming role {}",assumeRoleARN);
//        if (StringUtils.isNotEmpty(assumeRoleARN)) {
//            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
//                    .withClientConfiguration(clientConfiguration())
//                    .withCredentials(awsCredentialsProvider)
//                    .build();
//
//            return new STSAssumeRoleSessionCredentialsProvider
//                    .Builder(assumeRoleARN, "test")
//                    .withStsClient(stsClient)
//                    .build();
//        }
//    return awsCredentialsProvider;
//    }

    @Bean
    @Primary
    public AmazonS3 getS3Client()
    {
        AmazonS3 amazonS3= AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials()))
                .withRegion(region)
                .build();
        return amazonS3;
    }
}

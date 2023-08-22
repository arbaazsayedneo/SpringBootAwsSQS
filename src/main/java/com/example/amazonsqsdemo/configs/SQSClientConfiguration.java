package com.example.amazonsqsdemo.configs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SQSClientConfiguration {

    @Value("${cloud.aws.credentials.accessKey}")
    private String awsAccessKey;
    @Value("${cloud.aws.credentials.secretKey}")
    private String awsSecretKey;

    //Create SQS Client Bean
    @Bean
    public AmazonSQS amazonSQSClient(){
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey,awsSecretKey);
        return AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.AP_SOUTH_1)
                .build();

    }

    //Create S3 Client bean
    @Bean
    public AmazonS3 amazonS3Client(){
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey,awsSecretKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.AP_SOUTH_1)
                .build();
    }
}

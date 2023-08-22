package com.example.amazonsqsdemo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.util.IOUtils;
import com.example.amazonsqsdemo.domain.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class SQSService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SQSService.class);

    private final AmazonSQS sqs;

    private final AmazonS3 s3;
    @Value("${cloud.aws.sqs.standard.queue.url}")
    private String queueUrl;

    @Value("${cloud.aws.s3.bucketName}")
    private String awsBucketName;


    public SQSService(AmazonSQS sqs, AmazonS3 s3) {
        this.sqs = sqs;
        this.s3 = s3;
    }

    public String sendMessage(final String authToken, final String message){
        try {
            final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();

            messageAttributes.put("AuthToken",new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue(authToken));

            final SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.withMessageBody(message);
            sendMessageRequest.withQueueUrl(queueUrl);
            sendMessageRequest.withMessageAttributes(messageAttributes);

            // message is sent at this line
            SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
            LOGGER.info("Message Sent with ID:{}",sendMessageResult.getMessageId());
            // LOGGER.info("Before setting SQSMessage");
            SQSMessage sqsMessage = SQSMessage.builder()
                    .messageId(sendMessageResult.getMessageId())
                    .messageAttributes(messageAttributes)
                    .messageContent(message)
                    .build();
            // LOGGER.info("After setting SQSMessage");
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = null;

            try {
                // LOGGER.info("Inside try block");
                jsonContent = objectMapper.writeValueAsString(sqsMessage);
            } catch (IOException e){
                LOGGER.error("Error Mapping content of message {}",e.getLocalizedMessage());
            }
            byte[] jsonBytes = jsonContent.getBytes();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(jsonBytes);

            //Saving object with Message ID as file name
            // LOGGER.info("Before sending message to S3 bucket");
            s3.putObject(awsBucketName,sendMessageResult.getMessageId()+".json",byteArrayInputStream,null);

            return sendMessageResult.getMessageId();

        } catch (AmazonSQSException e){
            LOGGER.error("SQS Error Occurred with Code: {} message: {}",e.getErrorCode(),e.getErrorMessage());
            return e.getErrorMessage();
        }
    }

    public ResponseEntity<SQSMessage> getMessage(String messageId) {
        try {
            S3Object messageContent = s3.getObject(new GetObjectRequest(awsBucketName,messageId+".json"));
            InputStream inputStream = messageContent.getObjectContent();
            String jsonStringFromS3 = IOUtils.toString(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            SQSMessage sqsMessage = objectMapper.readValue(jsonStringFromS3, SQSMessage.class);
            return ResponseEntity.ok(sqsMessage);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (AmazonS3Exception e){
            if (e.getStatusCode() == 404 && "NoSuchKey".equals(e.getErrorCode())){
                LOGGER.error("No Message was found for the given ID");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            LOGGER.error("S3 Exception Occurred: {}", e.getErrorMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}

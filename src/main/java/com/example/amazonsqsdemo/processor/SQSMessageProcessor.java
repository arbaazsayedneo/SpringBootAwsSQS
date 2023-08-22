package com.example.amazonsqsdemo.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class SQSMessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQSMessageProcessor.class);

    private final AmazonSQS sqs;
    @Value("${cloud.aws.sqs.standard.queue.url}")
    private String queueUrl;

    public SQSMessageProcessor(AmazonSQS sqs) {
        this.sqs = sqs;
    }
    @Retryable(value = {AmazonSQSException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000))
    public void processMessage(Message message) {

        try {
            LOGGER.info("Message Processing Started");
            LOGGER.info("Message Received : {}",message);

            //Handle Message Processing Logic Here
            LOGGER.info("Received Message with ID: {} Content: {}",message.getMessageId(),message.getBody());
            LOGGER.info("Attributes : {}",message.getMessageAttributes().get("AuthToken"));

            //After Processing the message successfully we can delete it.
            deleteMessage(message.getReceiptHandle());

        } catch (AmazonSQSException e) {
            LOGGER.error(e.getErrorMessage());
        }

    }

    public void deleteMessage(String receiptHandle){
        sqs.deleteMessage(queueUrl,receiptHandle);
        LOGGER.info("Message Deletion Completed");
    }

}

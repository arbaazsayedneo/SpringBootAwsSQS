package com.example.amazonsqsdemo.listener;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.example.amazonsqsdemo.processor.SQSMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Service
public class SQSListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQSListener.class);

    private final AmazonSQS sqs;
    @Value("${cloud.aws.sqs.standard.queue.url}")
    private String queueUrl;

    private volatile boolean running = true;

    private SQSMessageProcessor messageProcessor;

    @Autowired
    public SQSListener(AmazonSQS sqs, SQSMessageProcessor messageProcessor) {
        this.sqs = sqs;
        this.messageProcessor = messageProcessor;
    }

    @PostConstruct
    public void init(){
        LOGGER.info("======================================================");
        LOGGER.info("Starting SQS Listening for Queue {}", queueUrl);
        LOGGER.info("======================================================");
        new Thread(()->{
           while(running){
               ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                       .withQueueUrl(queueUrl)
                       .withWaitTimeSeconds(1)
                       .withMessageAttributeNames("AuthToken");

               List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
               for(Message message : messages){
                    messageProcessor.processMessage(message);
               }
           }
        }).start();
    }

    @PreDestroy
    public void stop(){
    LOGGER.info("Stopping SQS Listener -----------");
    running=false;
    }

}
package com.example.amazonsqsdemo.domain;

import com.amazonaws.services.sqs.model.MessageAttributeValue;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SQSMessage {

    private String messageId;
    private Map<String, MessageAttributeValue> messageAttributes;
    private String messageContent;

    public SQSMessage() {
    }

    public SQSMessage(String messageId, Map<String, MessageAttributeValue> messageAttributes, String messageContent) {
        this.messageId = messageId;
        this.messageAttributes = messageAttributes;
        this.messageContent = messageContent;
    }

    @Override
    public String toString(){
        return "Message{"+
                "MessageId='" + messageId +'\'' +
                "MessageAttributes'" + messageAttributes + '\'' +
                ", MessageContent=" +messageContent +
                '}';
    }

}

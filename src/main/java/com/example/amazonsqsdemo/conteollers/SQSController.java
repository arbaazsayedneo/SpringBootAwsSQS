package com.example.amazonsqsdemo.conteollers;

import com.example.amazonsqsdemo.service.SQSService;
import com.example.amazonsqsdemo.domain.SQSMessage;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SQSController {

    private final SQSService sqsService;


    public SQSController(SQSService sqsService) {
        this.sqsService = sqsService;
    }

    @ApiOperation(value = "Send Message to Queue")
    @PostMapping("/sendMessage")
    public String sendMessage(@RequestHeader(name = "AuthToken") final String authToken,
                              @RequestBody final String message){
        return sqsService.sendMessage(authToken,message);
    }

    @ApiOperation(value = "Get saved messages in S3")
    @GetMapping("/getMessage")
    public ResponseEntity<SQSMessage> getMessagebyId(@RequestParam("messageId") String messageId){
        return sqsService.getMessage(messageId);
    }
}

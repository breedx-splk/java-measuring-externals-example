package com.splunk.example;

import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

public class SQSBusinessLogic {

    private static final String QUEUE_NAME = "my_queue";
    private final SqsClient sqs;
    private final String queueUrl;

    public SQSBusinessLogic(SqsClient sqs, String queueUrl) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
    }

    static SQSBusinessLogic create(LocalStackContainer localstack){
        SqsClient sqs = SqsClient.builder()
                .endpointOverride(localstack.getEndpointOverride(SQS))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .region(Region.of(localstack.getRegion()))
                .build();

        CreateQueueResponse res = sqs.createQueue(b -> b.queueName(QUEUE_NAME));
        String url = res.queueUrl();
        return new SQSBusinessLogic(sqs, url);
    }

    public void maybeExfiltrate(String message) {
        if(message.contains("237")){
            exfiltrate(message);
        }
    }

    private void exfiltrate(String message) {
        System.out.println("Sending SQS: " + message);
        SendMessageResponse rrr = sqs.sendMessage(b -> b.messageBody(message).queueUrl(queueUrl));
    }
}

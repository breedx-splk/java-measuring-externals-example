package com.splunk.example;

import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

public class SQSBusinessLogic {

    private static final String QUEUE_NAME = "my_queue";

    static SQSBusinessLogic create(LocalStackContainer localstack){
        SqsClient client = SqsClient.builder()
                .endpointOverride(localstack.getEndpointOverride(SQS))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .region(Region.of(localstack.getRegion()))
                .build();

        CreateQueueRequest createReq = CreateQueueRequest.builder().queueName(QUEUE_NAME).build();
        client.createQueue(createReq);
        ListQueuesResponse queues = client.listQueues();

        SendMessageRequest sendReq = SendMessageRequest.builder().build();
        client.sendMessage(sendReq);
//        client.receiveMessage(receive);

        return new SQSBusinessLogic();
    }
}

package com.splunk.example;

import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

/**
 * Represents a piece of important business logic that uses S3.
 */
public class S3BusinessLogic {
    private static final String BUCKET_NAME = "mybucket001";
    private final S3Client s3;
    private final BlockingQueue<String> buffer = new ArrayBlockingQueue<>(10 * 1024);
    private final Random rand = new Random();

    public S3BusinessLogic(S3Client s3) {
        this.s3 = s3;
    }

    public static S3BusinessLogic create(LocalStackContainer localstack) {
        S3Client s3 = S3Client
                .builder()
                .endpointOverride(localstack.getEndpointOverride(S3))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .region(Region.of(localstack.getRegion()))
                .build();

        System.out.println("Creating S3 bucket: " + BUCKET_NAME);
        s3.createBucket(b -> b.bucket(BUCKET_NAME));
        System.out.println(BUCKET_NAME + " bucket created");
        return new S3BusinessLogic(s3);
    }

    public void buffer(String string) {
        buffer.add(string);
    }

    public void flushBuffer() {
        System.out.println("Flushing buffer to s3...");
        String key = "/path/to/logs/" + System.currentTimeMillis() + ".log";

        List<String> list = new LinkedList<>();
        buffer.drainTo(list);
        String bufferContent = String.join("\n", list);

        RequestBody body = RequestBody.fromString(bufferContent);
        try {
            s3.putObject(b -> b.bucket(getBucketName()).key(key), body);
            System.out.println("S3 flush complete.");
        } catch (AwsServiceException | SdkClientException e) {
            System.err.println("Error flushing to s3: " + e.getMessage());
        }
    }

    private String getBucketName() {
        // 20% chance of failing the write to s3. It happens.
        if (rand.nextInt(5) == 0) {
            return "nonexisting_bucket";
        }
        return BUCKET_NAME;
    }
}

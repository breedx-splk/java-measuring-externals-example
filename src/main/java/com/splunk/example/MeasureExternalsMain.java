package com.splunk.example;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.util.function.Consumer;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KINESIS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

public class MeasureExternalsMain {

    public static void main(String[] args) {
        DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:0.11.3");
        LocalStackContainer localstack = new LocalStackContainer(localstackImage)
                .withServices(S3, KINESIS, SQS);
        localstack.start();

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

        String bucketName = "mybucket001";
        CreateBucketRequest createRequest = CreateBucketRequest.builder().bucket(bucketName).build();
        s3.createBucket(createRequest);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("/path/to/key01")
                .build();
        RequestBody body = RequestBody.fromString("my content");
        s3.putObject(request, body);

    }

}

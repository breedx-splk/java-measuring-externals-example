package com.splunk.example;

import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;
import software.amazon.awssdk.services.kinesis.model.RegisterStreamConsumerRequest;
import software.amazon.awssdk.services.kinesis.model.SequenceNumberRange;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KINESIS;

public class KinesisBusinessLogic {

    public static final String STREAM_NAME = "data-is-flowing";
    private final KinesisClient kinesis;
    private final String shardId;
    private String sequence;

    public KinesisBusinessLogic(KinesisClient kinesis, String shardId, String startingSequenceNum) {
        this.kinesis = kinesis;
        this.shardId = shardId;
        this.sequence = startingSequenceNum;
    }

    static KinesisBusinessLogic create(LocalStackContainer localstack){
//        KinesisAsyncClient kinesis = KinesisAsyncClient.builder()
        KinesisClient kinesis = KinesisClient.builder()
                .endpointOverride(localstack.getEndpointOverride(KINESIS))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .region(Region.of(localstack.getRegion()))
                .build();

        System.out.println("Creating kinesis data stream...");
        kinesis.createStream(b -> b.streamName(STREAM_NAME).shardCount(1));
        System.out.println("Kinesis data stream created.");

        DescribeStreamResponse description = kinesis.describeStream(b -> b.streamName(STREAM_NAME));
        String arn = description.streamDescription().streamARN();
        String shardId = description.streamDescription().shards().get(0).shardId();
        SequenceNumberRange sequenceNumberRange = description.streamDescription().shards().get(0).sequenceNumberRange();
        String startingSeq = sequenceNumberRange.startingSequenceNumber();

        PutRecordResponse res = kinesis.putRecord(b -> b.streamName(STREAM_NAME)
                .partitionKey("0")
                .data(SdkBytes.fromString("important message in here", UTF_8)));
        String seq = res.sequenceNumber();

        GetShardIteratorResponse sii = kinesis.getShardIterator(b -> b.streamName(STREAM_NAME)
                .shardId(shardId)
                .startingSequenceNumber(startingSeq)
                .shardIteratorType(ShardIteratorType.AFTER_SEQUENCE_NUMBER));
        String shardIterator = sii.shardIterator();
        GetRecordsResponse response = kinesis.getRecords(b -> b.shardIterator(shardIterator));
        return new KinesisBusinessLogic(kinesis, shardId, startingSeq);
    }

    void write(){

    }

    void read(){

    }
}

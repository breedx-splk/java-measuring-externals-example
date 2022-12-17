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
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.SequenceNumberRange;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KINESIS;

public class KinesisBusinessLogic {

    public static final String STREAM_NAME = "data-is-flowing";
    private final KinesisClient kinesis;
    private final String shardId;
    private final Consumer<String> messageConsumer;
    private String sequence;

    public KinesisBusinessLogic(KinesisClient kinesis, String shardId, String startingSequenceNum, Consumer<String> messageConsumer) {
        this.kinesis = kinesis;
        this.shardId = shardId;
        this.sequence = startingSequenceNum;
        this.messageConsumer = messageConsumer;
    }

    static KinesisBusinessLogic create(LocalStackContainer localstack, Consumer<String> messageConsumer){
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
        return new KinesisBusinessLogic(kinesis, shardId, startingSeq, messageConsumer);
    }

    // Generic fake business logic around kinesis
    public void generateImportantMessage() {
        write("very important message: " + new Date());
    }

    private void write(String message){
        try {
            System.out.println("Kinesis putRecord()");
            PutRecordResponse res = kinesis.putRecord(b -> b.streamName(STREAM_NAME)
                    .partitionKey("0")
                    .data(SdkBytes.fromString(message, UTF_8)));
//            String seq = res.sequenceNumber();
        } catch (Exception e) {
            System.out.println("Error writing to kinesis: " + e.getMessage());
        }
    }

    // Generic kinesis business logic
    public void receiveData() {
        List<Record> records = read();
        System.out.println("Kinesis: read records (" + records.size() + ")");
        records.forEach(rec -> {
            sequence = rec.sequenceNumber(); // Update our sequence for next read
            messageConsumer.accept(sequence + " " + new String(rec.data().asByteArray()));
        });

    }
    private List<Record> read(){
        GetShardIteratorResponse sii = kinesis.getShardIterator(b -> b.streamName(STREAM_NAME)
                .shardId(shardId)
                .startingSequenceNumber(sequence)
                .shardIteratorType(ShardIteratorType.AFTER_SEQUENCE_NUMBER));
        String shardIterator = sii.shardIterator();
        GetRecordsResponse response = kinesis.getRecords(b -> b.shardIterator(shardIterator));
        return response.records();
    }
}

package com.splunk.example;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KINESIS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

public class MeasureExternalsMain {


    private final LocalStackContainer localstack;
    private final ScheduledExecutorService pool = Executors.newScheduledThreadPool(3);

    public static void main(String[] args) {
        DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:latest");
        LocalStackContainer localstack = new LocalStackContainer(localstackImage)
                .withServices(S3, KINESIS, SQS);
        System.out.println("Starting fake aws localstack...");
        localstack.start();
        System.out.println("Localstack startup complete.");

        new MeasureExternalsMain(localstack).runForever();
    }

    public MeasureExternalsMain(LocalStackContainer localstack) {
        this.localstack = localstack;
    }

    private void runForever() {
        S3BusinessLogic s3 = S3BusinessLogic.create(localstack);
        KinesisBusinessLogic kinesis = KinesisBusinessLogic.create(localstack);
//        SQSBusinessLogic sqs = SQSBusinessLogic.create(localstack);

        pool.scheduleAtFixedRate(s3::flushBuffer, 5, 5, SECONDS);
        pool.scheduleWithFixedDelay(kinesis::generateImportantMessage, 250, 250, MILLISECONDS);
        pool.scheduleWithFixedDelay(kinesis::receiveData, 1500, 1500, MILLISECONDS);
    }

}

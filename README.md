# java-measuring-externals-example

Examples of how to create measurements about external services

* localstack to simulate aws 
* you need docker

# Diagram

* generate data to kinesis every 250ms
* read data from kinesis by sequence every 1500ms
* send all data with sequence number to s3 buffer
* flush s3 buffer to s3 every 5 seconds
* if data contains "237" it is also sent to sqs
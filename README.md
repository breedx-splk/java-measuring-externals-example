# java-measuring-externals-example

Examples of how to create measurements about external services.
We pretend that we're a cloud-native application running in AWS. 
To avoid having to provision real infrastructure, though, we leverage
[LocalStack](https://localstack.cloud/) to create local AWS client
compatible services in Docker.

The java AWS clients here are the real client SDK, but pointed
to the LocalStack instance via `testcontainers`.

# Diagram

The simple application here talks with 3 different AWS services.

<img width="442" alt="image" src="https://user-images.githubusercontent.com/75337021/208212278-fc0a5a11-8649-483c-aa0b-803ccf323781.png">

There are 3 scheduled jobs:

* Every 250ms, generate a message into kinesis
* Every 1500ms, read all messages from kinesis
  * prepend the sequence number
  * buffer in memory
* Every 5 seconds, flush transformed and buffered messages to S3
  * the code intentionally fails S3 writes 20% of the time

If the messages from kinesis contain the string "237", they are also 
sent to SQS.

# Out Of The Box

What do we get out of the box? First, the java agent
detects the AWS sdk and automatically instruments the client calls. 
From that instrumentation, we get spans generated for the AWS service
calls and the APM service explorer shows them as _inferred services_.

# What About Metrics

What do we already get?

What do we think we need?
plugins {
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.splunk.example.MeasureExternalsMain")
    applicationDefaultJvmArgs = listOf(
        "-javaagent:splunk-otel-javaagent-1.19.0.jar",
//        "-Dotel.javaagent.debug=true",
        "-Dotel.resource.attributes=deployment.environment=measure-ext",
        "-Dotel.service.name=MeasureExternalsExample"
    )
}

dependencies {
    implementation("software.amazon.awssdk:core:2.18.39")
    implementation("software.amazon.awssdk:s3:2.18.39")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.365")

    implementation("software.amazon.awssdk:kinesis:2.18.39")
    implementation("software.amazon.awssdk:sqs:2.18.39")
    implementation("org.testcontainers:testcontainers:1.17.6")
    implementation("org.testcontainers:localstack:1.17.6")
}

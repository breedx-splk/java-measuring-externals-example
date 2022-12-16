plugins {
    application
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    //NOTE: This is required for @P
    compilerArgs.addAll(listOf("-parameters"))
}
application {
    mainClass.set("com.splunk.example.MeasureExternalsMain")
    applicationDefaultJvmArgs = listOf(
        "-javaagent:splunk-otel-javaagent-1.19.0.jar",
        "-Dotel.javaagent.debug=true",
        "-Dotel.service.name=SpanAttrExample",
        "-Dotel.instrumentation.methods.include=com.splunk.example.SpanAttributesMain[superDuperBonusMethod,highScore]"
    )
}

dependencies {
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:1.19.2-alpha")
}

plugins {
    id("io.micronaut.application") version "4.5.4"
    id("com.gradleup.shadow") version "8.3.7"
}

version = "0.1"
group = "com.example"

repositories {
    mavenCentral()
}

dependencies {
    // Cache
    implementation("io.micronaut.cache:micronaut-cache-caffeine")

    // Micronaut Validation
    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")
    implementation("io.micronaut.validation:micronaut-validation")

    // Micronaut Serialization
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.serde:micronaut-serde-jackson")

    // Routes Validation
    annotationProcessor("io.micronaut:micronaut-http-validation")

    // Langchain4j
    annotationProcessor("io.micronaut.langchain4j:micronaut-langchain4j-processor:1.1.0")
    implementation("io.micronaut.langchain4j:micronaut-langchain4j-openai:1.1.0")
    //implementation("io.micronaut.langchain4j:micronaut-langchain4j-oci-genai:1.1.0")

    // HTTP Client
    implementation("io.micronaut:micronaut-http-client")

    // Endpoints
    implementation("io.micronaut:micronaut-management")

    // Views
    implementation("io.micronaut.views:micronaut-views-thymeleaf")

    // Logging
    runtimeOnly("ch.qos.logback:logback-classic")

    testImplementation("org.mockito:mockito-core")
}


application {
    mainClass = "com.example.Application"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}


graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.example.*")
    }
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}
var graalvmVersion  = "21.0.2"
// https://github.com/graalvm/container/pkgs/container/graalvm-community
tasks.named<io.micronaut.gradle.docker.MicronautDockerfile>("dockerfile") {
    baseImage.set("ghcr.io/graalvm/graalvm-community:$graalvmVersion")
}
// https://github.com/graalvm/container/pkgs/container/native-image-community
tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    graalImage.set("ghcr.io/graalvm/graalvm-community:$graalvmVersion")
}


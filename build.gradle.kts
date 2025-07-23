plugins {
    id("io.micronaut.application") version "4.5.4"
    id("com.gradleup.shadow") version "8.3.7"
    id("io.micronaut.aot") version "4.5.4"
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
    annotationProcessor(platform("io.micronaut.langchain4j:micronaut-langchain4j-bom:1.1.0"))
    implementation(platform("io.micronaut.langchain4j:micronaut-langchain4j-bom:1.1.0"))
    annotationProcessor("io.micronaut.langchain4j:micronaut-langchain4j-processor")
    implementation("io.micronaut.langchain4j:micronaut-langchain4j-openai")
    implementation("dev.langchain4j:langchain4j-community-oci-genai")
    implementation("io.micronaut.langchain4j:micronaut-langchain4j-oci-genai")

    // HTTP Client
    implementation("io.micronaut:micronaut-http-client")

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
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}


tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}



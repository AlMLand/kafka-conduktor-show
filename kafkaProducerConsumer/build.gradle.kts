plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.7.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11
ext["kafka.clients.version"] = "3.3.2"
ext["slf4j.version"] = "2.0.6"
ext["okhttp3.version"] = "4.10.0"
ext["okhttp.eventsource.version"] = "3.0.0"
ext["opensearch.rest.high.level.client.version"] = "2.5.0"
ext["google.gson.version"] = "2.10.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.kafka:kafka-clients:${property("kafka.clients.version")}")
    implementation("org.slf4j:slf4j-api:${property("slf4j.version")}")
    implementation("org.slf4j:slf4j-simple:${property("slf4j.version")}")
    implementation("com.squareup.okhttp3:okhttp:${property("okhttp3.version")}")
    implementation("com.launchdarkly:okhttp-eventsource:${property("okhttp.eventsource.version")}")
    implementation("org.opensearch.client:opensearch-rest-high-level-client:${property("opensearch.rest.high.level.client.version")}")
    implementation("com.google.code.gson:gson:${property("google.gson.version")}")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
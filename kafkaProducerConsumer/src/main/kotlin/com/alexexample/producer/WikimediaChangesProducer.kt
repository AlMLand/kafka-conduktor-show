package com.alexexample.producer

import com.alexexample.producer.WikimediaChangeHandler
import com.launchdarkly.eventsource.EventSource
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.BATCH_SIZE_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.BUFFER_MEMORY_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.COMPRESSION_TYPE_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.MAX_BLOCK_MS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION
import org.apache.kafka.clients.producer.ProducerConfig.PARTITIONER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.RETRY_BACKOFF_MS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.internals.DefaultPartitioner
import org.apache.kafka.common.serialization.StringSerializer
import java.net.URI
import java.util.Properties
import java.util.concurrent.TimeUnit.MINUTES

class WikimediaChangesProducer

private const val BOOTSTRAP_SERVERS = "127.0.0.1:9092"
private const val TOPIC = "wikimedia.recentchange"
private const val URL = "https://stream.wikimedia.org/v2/stream/recentchange"

fun main() {
    KafkaProducer<String, String>(properties()).let {
        eventSource(it, TOPIC).start()
        MINUTES.sleep(10)
    }
}

private fun eventSource(kafkaProducer: KafkaProducer<String, String>, topic: String) =
    EventSource.Builder(WikimediaChangeHandler(topic, kafkaProducer), URI.create(URL)).build()

private fun properties() = Properties().apply {
    setProperty(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS)
    setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
    setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
    setProperty(PARTITIONER_CLASS_CONFIG, DefaultPartitioner::class.java.name)
    setProperty(ACKS_CONFIG, "-1")
// ######### RETRIES #########
    setProperty(RETRIES_CONFIG, "2147483647")
    setProperty(DELIVERY_TIMEOUT_MS_CONFIG, "120000")
    setProperty(RETRY_BACKOFF_MS_CONFIG, "100")
    setProperty(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5")
// ######### IDEMPOTENCE #########
    setProperty(ENABLE_IDEMPOTENCE_CONFIG, "true")
// ######### BATCHING MECHANISM #########
    setProperty(LINGER_MS_CONFIG, "0")
    setProperty(BATCH_SIZE_CONFIG, "16384")
// ######### COMPRESSION #########
    setProperty(COMPRESSION_TYPE_CONFIG, "snappy")
// ######## BUFFER MEMORY ########
    setProperty(BUFFER_MEMORY_CONFIG, "33554432")
    setProperty(MAX_BLOCK_MS_CONFIG, "60000")
}

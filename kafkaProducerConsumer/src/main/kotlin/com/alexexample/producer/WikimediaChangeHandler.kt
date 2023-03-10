package com.alexexample.producer

import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.MessageEvent
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.RecordTooLargeException
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import java.util.UUID

class WikimediaChangeHandler(
    private val topic: String,
    private val kafkaProducer: KafkaProducer<String, String>
) : EventHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun onClosed() {
        kafkaProducer.close()
    }

    override fun onMessage(event: String?, messageEvent: MessageEvent?) {
        messageEvent?.data.let { data ->
            data?.let { sendRecord(data, topic) }
                ?: logger.info("The event data is null")
        }
    }

    private fun sendRecord(
        record: String,
        topic: String,
        key: String = UUID.randomUUID().toString()
    ): Unit =
        kafkaProducer.send(ProducerRecord(topic, key, record)) { metadata, exception ->
            if (exception == null) logger.info(
                """
                    Callback -> received new metadata: topic: {} | partition: {} | offset: {} | timestamp: {} | time: {}
                """.trimIndent(),
                metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp(),
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(metadata.timestamp()),
                    TimeZone.getDefault().toZoneId()
                ).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
            )
            else when (exception) {
                is RecordTooLargeException -> {
                    listOf(record.substring(0, middle(record)), record.substring(middle(record))).forEach {
                        sendRecord(it, topic, key)
                    }
                }

                else -> logger.error("Error while producing", exception)
            }
        }.run {
            get().run {
                logger.info(
                    """
                        RecordMetadata -> send to topic: ${topic()} | partition: ${partition()} | offset: ${offset()}
                    """.trimIndent()
                )
            }
        }

    private fun middle(record: String) = record.length / 2

    override fun onError(t: Throwable?) {
        logger.error("Error in stream reading", t)
    }

    override fun onOpen() {}

    override fun onComment(comment: String?) {}
}

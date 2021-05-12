package no.nav.helse.sparkerferiepenger

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.common.KafkaEnvironment
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.collections.set

@TestInstance(Lifecycle.PER_CLASS)
internal class ComponentTest : TestAbstract() {

    private lateinit var producer: KafkaProducer<String, String>
    private lateinit var consumer: KafkaConsumer<String, String>
    private val topic = "test-topic"
    private val topicInfos = listOf(
        KafkaEnvironment.TopicInfo(topic, partitions = 1)
    )
    private val embeddedKafkaEnvironment = KafkaEnvironment(
        autoStart = false,
        noOfBrokers = 1,
        topicInfos = topicInfos,
        withSchemaRegistry = false,
        withSecurity = false
    )


    @BeforeAll
    override fun `setup`() {
        super.setup()
        embeddedKafkaEnvironment.start()
        producer = KafkaProducer(baseConfig().toProducerConfig())
        consumer = KafkaConsumer<String, String>(baseConfig().toConsumerConfig())
    }

    @AfterAll
    fun `cleanup`() {
        embeddedKafkaEnvironment.tearDown()
        producer.flush()
        producer.close()
        consumer.close()
    }

    @Test
    fun `it worke`() {
        consumer.assign(listOf(TopicPartition(topic, 0)))

        val kafkaConfig = KafkaConfig(
            topicName = topic,
            bootstrapServers = embeddedKafkaEnvironment.brokersURL,
            username = "username",
            password = "password"
        )

        lagreMeldinger()

        val sykepengehistorikkForFeriepengerHåndterer =
            SykepengehistorikkForFeriepengerHåndterer(kafkaConfig.topicName, meldingDao)

        val fom = LocalDate.of(2020, 1, 1)
        val tom = LocalDate.of(2020, 12, 31)

        sendSykepengehistorikkForFeriepengerJob(
            fom,
            tom,
            meldingDao,
            sykepengehistorikkForFeriepengerHåndterer,
            producer
        )

        consumer.seekToBeginning(consumer.assignment())
        consumer.poll(Duration.ofMillis(100)).let {

            assertEquals(FNR.size, it.count())
            it.last().run {
                val fnr = FNR.first().toString().padStart(11, '0')
                assertEquals(fnr, key())
                mapOf(
                    "@event_name" to "behov",
                    "fødselsnummer" to fnr,
                    "@behov" to listOf("SykepengehistorikkForFeriepenger"),
                    "SykepengehistorikkForFeriepenger" to mapOf(
                        "historikkFom" to fom,
                        "historikkTom" to tom
                    )
                ).forEach { (key, value) ->
                    assertTrue(value().contains(""""$key":"$value"""))
                }
            }
        }
    }

    private fun baseConfig(): Properties = Properties().also {
        it.load(this::class.java.getResourceAsStream("/kafka_base.properties"))
        it.remove("security.protocol")
        it.remove("sasl.mechanism")
        it["bootstrap.servers"] = embeddedKafkaEnvironment.brokersURL
    }
}

fun Properties.toProducerConfig(): Properties = Properties().also {
    it.putAll(this)
    it[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    it[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
}

fun Properties.toConsumerConfig(): Properties = Properties().also {
    it.putAll(this)
    it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    it[ConsumerConfig.GROUP_ID_CONFIG] = "sparker-feriepenger-test"
    it[ConsumerConfig.CLIENT_ID_CONFIG] = "test"
}

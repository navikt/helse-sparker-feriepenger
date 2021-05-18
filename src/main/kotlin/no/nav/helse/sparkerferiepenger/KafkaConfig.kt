package no.nav.helse.sparkerferiepenger

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import java.io.File
import java.util.*

internal class KafkaConfig(
    val topicName: String,
    private val bootstrapServers: String,
    private val username: String,
    private val password: String,
    private val truststore: String,
    private val truststorePassword: String
) {
    internal fun producerConfig() = Properties().apply {
        put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(SaslConfigs.SASL_MECHANISM, "PLAIN")
        put(
            SaslConfigs.SASL_JAAS_CONFIG,
            "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
        )
        put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
        put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, File(truststore).absolutePath)
        put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword)
        put(ProducerConfig.CLIENT_ID_CONFIG, "sparker-feriepenger")
    }
}

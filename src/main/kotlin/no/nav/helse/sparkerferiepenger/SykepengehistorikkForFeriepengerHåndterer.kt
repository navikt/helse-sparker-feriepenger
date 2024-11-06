package no.nav.helse.sparkerferiepenger

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.LocalDate

class SykepengehistorikkForFeriepengerHåndterer(private val topic: String) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    internal fun håndter(
        fnr: String,
        fom: LocalDate,
        tom: LocalDate,
        producer: KafkaProducer<String, String>
    ) {
        try {
            val behov = mapTilSykepengehistorikkForFeriepengerBehov(fnr, fom, tom)
            producer.send(ProducerRecord(topic, fnr, objectMapper.writeValueAsString(behov)))
        } catch (e: Exception) {
            logger.error("Kunne ikke sende ut SykepengerhistorikkForFeriepenger-behov for person", e)
        }
    }
}



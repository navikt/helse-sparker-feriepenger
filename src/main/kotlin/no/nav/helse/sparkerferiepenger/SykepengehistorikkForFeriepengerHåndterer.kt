package no.nav.helse.sparkerferiepenger

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.LocalDate

class SykepengehistorikkForFeriepengerHåndterer(
    private val topic: String,
    private val meldingDao: MeldingDao,
    private val dryRun: Boolean
) {

    val logger = LoggerFactory.getLogger(this.javaClass)

    internal fun håndter(fnr: String, fom: LocalDate, tom: LocalDate, producer: KafkaProducer<String, String>) {
        try {
            if (!dryRun) {
                val metadata = producer.send(
                    ProducerRecord(
                        topic,
                        fnr,
                        objectMapper.writeValueAsString(mapTilSykepengehistorikkForFeriepengerBehov(fnr, fom, tom))
                    )
                ).get()
                logger.info("Sendt ut record med offset - ${metadata.offset()}, partisjon ${metadata.partition()}")
            }

            meldingDao.lagreFnrForSendtFeriepengerbehov(fnr.toLong())
        } catch (e: Exception) {
            logger.error("Kunne ikke sende ut SykepengerhistorikkForFeriepenger-behov for person")
        }
    }
}



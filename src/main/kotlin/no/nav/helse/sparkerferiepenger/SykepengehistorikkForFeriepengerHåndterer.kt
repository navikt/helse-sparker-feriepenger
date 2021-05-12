package no.nav.helse.sparkerferiepenger

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.LocalDate

class SykepengehistorikkForFeriepengerHåndterer(
    private val topic: String,
    private val meldingDao: MedlingDao
) {

    val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    val logger = LoggerFactory.getLogger(this.javaClass)

    internal fun håndter(fnr: String, fom: LocalDate, tom: LocalDate, producer: KafkaProducer<String, String>) {
        try {
            producer.send(
                ProducerRecord( // TODO: anybody help...?
                    topic,
                    fnr,
                    objectMapper.writeValueAsString(mapTilSykepengehistorikkForFeriepengerBehov(fnr, fom, tom))
                )
            )
            meldingDao.lagreFnrForSendtFeriepengerbehov(fnr.toLong())
        } catch (e: Exception) {
            sikkerlogg.error("Kunne ikke sende ut SykepengerhistorikkForFeriepenger-behov for fødselsnummer $fnr")
            logger.error("Kunne ikke sende ut SykepengerhistorikkForFeriepenger-behov for person")
        }
    }
}



package no.nav.helse.sparkerferiepenger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.system.exitProcess

val objectMapper: ObjectMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

fun main() {

    val env = System.getenv()

    val config = KafkaConfig(
        bootstrapServers = env.getValue("KAFKA_BROKERS"),
        truststore = env.getValue("KAFKA_TRUSTSTORE_PATH"),
        truststorePassword = env.getValue("KAFKA_CREDSTORE_PASSWORD"),
        keystoreLocation = env.getValue("KAFKA_KEYSTORE_PATH"),
        keystorePassword = env.getValue("KAFKA_CREDSTORE_PASSWORD")
    )
    val topic = env.getValue("KAFKA_TARGET_TOPIC")

    val dataSourceBuilder = DataSourceBuilder(env)
    val dataSource = dataSourceBuilder.getDataSource()

    val antall = env.get("ANTALL")?.toInt() ?: Int.MAX_VALUE
    val antallSkipped = env.getValue("ANTALL_SKIPPED").toInt()
    val enkeltpersoner = env.getValue("ENKELTPERSONER")
    val opptjeningsår = env.getValue("OPPTJENINGSAAR").toInt()

    val fom = LocalDate.of(opptjeningsår, 1, 1)
    val tom = LocalDate.of(opptjeningsår, 12, 31)
    val meldingDao = MeldingDao(dataSource)

    val producer = KafkaProducer(config.producerConfig(), StringSerializer(), StringSerializer())
    val sykepengehistorikkForFeriepengerHåndterer = SykepengehistorikkForFeriepengerHåndterer(topic)

    sendSykepengehistorikkForFeriepengerJob(
        fom,
        tom,
        meldingDao,
        sykepengehistorikkForFeriepengerHåndterer,
        antall,
        antallSkipped,
        enkeltpersoner,
        producer
    )
    exitProcess(0)
}

internal fun sendSykepengehistorikkForFeriepengerJob(
    fom: LocalDate,
    tom: LocalDate,
    meldingDao: MeldingDao,
    sykepengehistorikkForFeriepengerHåndterer: SykepengehistorikkForFeriepengerHåndterer,
    antall: Int,
    antallSkipped: Int,
    enkeltpersoner: String,
    producer: KafkaProducer<String, String>
) {
    val logger = LoggerFactory.getLogger("no.nav.helse.sparker.feriepenger")
    val startMillis = System.currentTimeMillis()

    if (enkeltpersoner.isNotBlank()) {
        enkeltpersoner
            .split(",")
            .forEach { fnr ->
                sykepengehistorikkForFeriepengerHåndterer.håndter(fnr, fom, tom, producer)
            }
        return
    }

    val fødselsnumre = meldingDao.hentFødselsnummere(antall, antallSkipped)
    logger.info("Fant ${fødselsnumre.size} fødselsnumre, starter publisering av behov...")

    fødselsnumre.forEach { personIder ->
        sykepengehistorikkForFeriepengerHåndterer.håndter(personIder.fødselsnummer, fom, tom, producer)
    }

    producer.flush()
    producer.close()

    logger.info("Prosessert SykepengehistorikkForFeriepenger-behov på ${(System.currentTimeMillis() - startMillis) / 1000}s")
}

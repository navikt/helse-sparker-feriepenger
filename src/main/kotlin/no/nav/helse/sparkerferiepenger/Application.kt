package no.nav.helse.sparkerferiepenger

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDate
import kotlin.system.exitProcess

val objectMapper = jacksonObjectMapper()
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

    val antall = env.getValue("ANTALL").toInt()
    val antallSkipped = env.getValue("ANTALL_SKIPPED").toInt()
    val enkeltperson = env["ENKELTPERSON"]

    val forrigeÅr = LocalDate.now().minusYears(1).year
    val fom = LocalDate.of(forrigeÅr, 1, 1)
    val tom = LocalDate.of(forrigeÅr, 12, 31)
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
        enkeltperson,
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
    enkeltperson: String?,
    producer: KafkaProducer<String, String>
) {
    val logger = LoggerFactory.getLogger("no.nav.helse.sparker.feriepenger")
    val startMillis = System.currentTimeMillis()

    if (enkeltperson != null) {
        val parts = enkeltperson.split(":")
        sykepengehistorikkForFeriepengerHåndterer.håndter(parts[0], parts[1], fom, tom, producer)
        return
    }

    meldingDao.hentFødselsnummere().drop(antallSkipped).take(antall).forEach { personIder ->
        sykepengehistorikkForFeriepengerHåndterer.håndter(personIder.fødselsnummer, personIder.aktørId, fom, tom, producer)
    }

    producer.flush()
    producer.close()

    logger.info("Prosessert SykepengehistorikkForFeriepenger-behov på ${(System.currentTimeMillis() - startMillis) / 1000}s")
}

private fun String.readFile() = File(this).readText(Charsets.UTF_8)

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
    val config = System.getenv().let { env ->
        KafkaConfig(
            topicName = env.getValue("KAFKA_RAPID_TOPIC"),
            bootstrapServers = env.getValue("KAFKA_BOOTSTRAP_SERVERS"),
            username = "/var/run/secrets/nais.io/service_user/username".readFile(),
            password = "/var/run/secrets/nais.io/service_user/password".readFile(),
            truststore = env["NAV_TRUSTSTORE_PATH"],
            truststorePassword = env["NAV_TRUSTSTORE_PASSWORD"]
        )
    }

    val env = System.getenv()
    val dataSourceBuilder = DataSourceBuilder(env)
    //TODO: dataSourceBuilder.migrate()
    val dataSource = dataSourceBuilder.getDataSource()

    val forrigeÅr = LocalDate.now().minusYears(1).year
    val fom = LocalDate.of(forrigeÅr, 1, 1)
    val tom = LocalDate.of(forrigeÅr, 12, 31)
    val meldingDao = PostgresMeldingDao(dataSource)

    val producer = KafkaProducer(config.producerConfig(), StringSerializer(), StringSerializer())
    val sykepengehistorikkForFeriepengerHåndterer = SykepengehistorikkForFeriepengerHåndterer(config.topicName, meldingDao)
    sendSykepengehistorikkForFeriepengerJob(fom, tom, meldingDao, sykepengehistorikkForFeriepengerHåndterer, producer)
    exitProcess(0)
}

internal fun sendSykepengehistorikkForFeriepengerJob(
    fom: LocalDate,
    tom: LocalDate,
    meldingDao: MedlingDao,
    sykepengehistorikkForFeriepengerHåndterer: SykepengehistorikkForFeriepengerHåndterer,
    producer: KafkaProducer<String, String>
) {
    val logger = LoggerFactory.getLogger("no.nav.helse.sparker.feriepenger")
    val startMillis = System.currentTimeMillis()

    meldingDao.hentFødselsnummere().forEach { fnr ->
        sykepengehistorikkForFeriepengerHåndterer.håndter(fnr, fom, tom, producer)
    }

    producer.flush()
    producer.close()

    logger.info("Prosessert SykepengehistorikkForFeriepenger-behov på ${(System.currentTimeMillis() - startMillis) / 1000}s")
}

private fun String.readFile() = File(this).readText(Charsets.UTF_8)

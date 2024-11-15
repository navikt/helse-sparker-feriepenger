package no.nav.helse.sparkerferiepenger

import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.LocalDate

internal class ComponentTest : TestAbstract() {
    private val topic = "test-topic"

    private val producer = mockk<KafkaProducer<String, String>>(relaxed = true)

    @Test
    fun `it worke agian`() {
        lagreMeldinger()

        val sykepengehistorikkForFeriepengerHåndterer = SykepengehistorikkForFeriepengerHåndterer(topic = topic)

        val fom = LocalDate.of(2020, 1, 1)
        val tom = LocalDate.of(2020, 12, 31)

        val captured = mutableListOf<ProducerRecord<String, String>>()

        sendSykepengehistorikkForFeriepengerJob(
            fom,
            tom,
            meldingDao,
            sykepengehistorikkForFeriepengerHåndterer,
            Integer.MAX_VALUE,
            0,
            "",
            producer
        )

        verify(exactly = 8) {
            producer.send(capture(captured))
        }

        val sisteBehov = captured.last()

        val fnr = PERSONIDER.last().fødselsnummer
        assertEquals(fnr, sisteBehov.key())

        val recordValue = sisteBehov.value()
        mapOf("@event_name" to "behov", "fødselsnummer" to fnr).forEach { (key, value) ->
            assertTrue(recordValue.contains(""""$key":"$value"""))
        }

        assertTrue(recordValue.contains(""""@behov":["SykepengehistorikkForFeriepenger"]"""))
        assertTrue(recordValue.contains(""""SykepengehistorikkForFeriepenger":{"historikkFom":"$fom","historikkTom":"$tom"}"""))
    }
}

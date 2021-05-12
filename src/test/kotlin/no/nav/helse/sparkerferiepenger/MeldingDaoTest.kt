package no.nav.helse.sparkerferiepenger

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.Connection
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.sql.DataSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MeldingDaoTest : TestAbstract(){

    @Test
    fun `kan hente ut fødselsnummere`() {
        lagreMeldinger()
        val fødselsnummere = meldingDao.hentFødselsnummere()
        assertEquals(8, fødselsnummere.size)
    }

    @Test
    fun `kan lagre fødselsnummer i sendt_feriepengerbehov`() {
        meldingDao.lagreFnrForSendtFeriepengerbehov(FNR.first())
        val fødselsnummer = hentFødselsnummer()
        assertEquals(1, fødselsnummer.size)
        assertEquals(FNR.first(), fødselsnummer.first())
    }

    @Test
    fun `ignorerer fødselsnummere som har sendt ut SykepengehistorikkForFeriepenger-behov`() {
        lagreMeldinger()
        meldingDao.lagreFnrForSendtFeriepengerbehov(FNR.first())
        val fødselsnummere = meldingDao.hentFødselsnummere()
        assertEquals(7, fødselsnummere.size)
    }

}

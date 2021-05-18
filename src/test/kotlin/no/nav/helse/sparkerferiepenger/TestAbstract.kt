package no.nav.helse.sparkerferiepenger

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.Connection
import java.util.*
import javax.sql.DataSource

abstract class TestAbstract {
    internal lateinit var embeddedPostgres: EmbeddedPostgres
    internal lateinit var postgresConnection: Connection
    internal lateinit var dataSource: DataSource
    internal lateinit var flyway: Flyway
    internal lateinit var meldingDao: PostgresMeldingDao

    @BeforeAll
    internal fun setupAll(@TempDir postgresPath: Path) {
        embeddedPostgres = EmbeddedPostgres.builder()
            .setOverrideWorkingDirectory(postgresPath.toFile())
            .setDataDirectory(postgresPath.resolve("datadir"))
            .start()
        postgresConnection = embeddedPostgres.postgresDatabase.connection

        dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = embeddedPostgres.getJdbcUrl("postgres", "postgres")
            maximumPoolSize = 3
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        })

        flyway = Flyway
            .configure()
            .dataSource(dataSource)
            .load()

        meldingDao = PostgresMeldingDao(dataSource)
    }

    @BeforeEach
    internal open fun setup() {
        flyway.clean()
        flyway.migrate()
    }

    @AfterAll
    internal fun tearDown() {
        postgresConnection.close()
        embeddedPostgres.close()
    }

    companion object {
        val FØDSELSNUMRE = listOf(
            9038400182, 24068919084, 17086922452, 19026500128, 24038920673, 3079016259, 11117615091, 5068821403, 9038400182
        )

        val MELDING_TYPE_ID = 1
    }

    private fun lagreMeldingType() {
        using(sessionOf(dataSource)) { session ->
            val query = """INSERT INTO melding_type (id, navn) VALUES ($MELDING_TYPE_ID, 'meldingtype')"""
            session.run(queryOf(query).asUpdate)
        }
    }

    internal fun lagreMeldinger() {
        lagreMeldingType()

        using(sessionOf(dataSource)) { session ->
            FØDSELSNUMRE.forEach {
                val query =
                    """INSERT INTO melding (id, melding_type_id, fnr, json) VALUES ('${UUID.randomUUID()}', $MELDING_TYPE_ID, $it, '{}')"""
                session.run(queryOf(query).asUpdate)
            }
        }
    }

    internal fun hentFødselsnummer() =
        using(sessionOf(dataSource)) { session ->
            val query = """SELECT fnr FROM sendt_feriepengerbehov"""
            session.run(queryOf(query).map { it.long(1) }.asList)
        }
}

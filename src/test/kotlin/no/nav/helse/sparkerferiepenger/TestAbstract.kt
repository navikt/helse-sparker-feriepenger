package no.nav.helse.sparkerferiepenger

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*
import javax.sql.DataSource

abstract class TestAbstract {
    private val dataSource: DataSource
    private val flyway: Flyway
    protected val meldingDao: MeldingDao

    private val postgres = PostgreSQLContainer<Nothing>("postgres:14").apply {
        withReuse(true)
        withLabel("app-navn", "sparke-sin-sparsom")
        start()

        println("Database: jdbc:postgresql://localhost:$firstMappedPort/test startet opp, credentials: test og test")
    }

    init {
        dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = postgres.jdbcUrl
            username = postgres.username.also (::println)
            password = postgres.password
            maximumPoolSize = 3
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 10000
            maxLifetime = 30001
        })

        flyway = Flyway
            .configure()
            .dataSource(dataSource)
            .load()

        meldingDao = MeldingDao(dataSource)
    }

    @BeforeEach
    internal open fun setup() {
        flyway.clean()
        flyway.migrate()
    }

    companion object {
        val PERSONIDER = listOf(
            PersonIder("03079016259", UUID.randomUUID().toString()),
            PersonIder("05068821403", UUID.randomUUID().toString()),
            PersonIder("09038400182", UUID.randomUUID().toString()),
            PersonIder("11117615091", UUID.randomUUID().toString()),
            PersonIder("17086922452", UUID.randomUUID().toString()),
            PersonIder("19026500128", UUID.randomUUID().toString()),
            PersonIder("24038920673", UUID.randomUUID().toString()),
            PersonIder("09038400182", UUID.randomUUID().toString()),
            PersonIder("24068919084", UUID.randomUUID().toString()),
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
            PERSONIDER.forEach {
                val query =
                    """INSERT INTO melding (id, melding_type_id, fnr, json)
                       VALUES ('${UUID.randomUUID()}', $MELDING_TYPE_ID, ${it.fødselsnummer}, '{"aktørId": "${it.aktørId}"}')"""
                session.run(queryOf(query).asUpdate)
            }
        }
    }
}

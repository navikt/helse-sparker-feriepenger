package no.nav.helse.sparkerferiepenger

import com.github.navikt.tbd_libs.test_support.DatabaseContainers
import com.github.navikt.tbd_libs.test_support.TestDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.*
import javax.sql.DataSource

val databaseContainer = DatabaseContainers.container("sparker-feriepenger")

abstract class TestAbstract {
    private lateinit var testDataSource: TestDataSource
    private val dataSource: DataSource get() = testDataSource.ds
    protected lateinit var meldingDao: MeldingDao

    @BeforeEach
    internal open fun setup() {
        testDataSource = databaseContainer.nyTilkobling()
        meldingDao = MeldingDao(dataSource)
    }

    @AfterEach
    fun tearDown() {
        databaseContainer.droppTilkobling(testDataSource)
    }

    companion object {
        val PERSONIDER = listOf(
            PersonIder("03079016259"),
            PersonIder("05068821403"),
            PersonIder("09038400182"),
            PersonIder("11117615091"),
            PersonIder("17086922452"),
            PersonIder("19026500128"),
            PersonIder("24038920673"),
            PersonIder("09038400182"),
            PersonIder("24068919084"),
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
                       VALUES ('${UUID.randomUUID()}', $MELDING_TYPE_ID, ${it.fødselsnummer}, '{}')"""
                session.run(queryOf(query).asUpdate)
            }
        }
    }
}

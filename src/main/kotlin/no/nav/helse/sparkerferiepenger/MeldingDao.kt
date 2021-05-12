package no.nav.helse.sparkerferiepenger

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource

interface MedlingDao {
    fun hentFødselsnummere(): List<String>
    fun lagreFnrForSendtFeriepengerbehov(fnr: Long)
}

class PostgresMeldingDao(
    private val dataSource: DataSource
) : MedlingDao {
    override fun hentFødselsnummere() = using(sessionOf(dataSource)) { session ->
        val query = """SELECT DISTINCT fnr FROM melding WHERE fnr NOT IN (SELECT fnr FROM sendt_feriepengerbehov)"""
        session.run(queryOf(query).map { it.string(1).padStart(11, '0') }.asList)
    }

    override fun lagreFnrForSendtFeriepengerbehov(fnr: Long) {
        using(sessionOf(dataSource)) { session ->
            val query = """INSERT INTO sendt_feriepengerbehov (fnr) VALUES ($fnr)"""
            session.run(queryOf(query).asUpdate)
        }
    }
}

// TODO: fjerne?
class MeldingDaoMock : MedlingDao {
    val fødselnummere = listOf(
        9038400182, 24068919084, 17086922452, 19026500128, 24038920673, 3079016259, 11117615091, 5068821403
    )

    override fun hentFødselsnummere(): List<String> = fødselnummere.map { it.toString().padStart(11, '0') }

    override fun lagreFnrForSendtFeriepengerbehov(fnr: Long) {
        TODO("Not yet implemented")
    }
}

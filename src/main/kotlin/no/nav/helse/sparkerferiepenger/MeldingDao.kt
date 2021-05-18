package no.nav.helse.sparkerferiepenger

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource

interface MeldingDao {
    fun hentFødselsnummere(): List<String>
    fun lagreFnrForSendtFeriepengerbehov(fnr: Long)
}

class PostgresMeldingDao(
    private val dataSource: DataSource
) : MeldingDao {
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

package no.nav.helse.sparkerferiepenger

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource


class MeldingDao(private val dataSource: DataSource) {
    fun hentFødselsnummere() = using(sessionOf(dataSource)) { session ->
        val query = """
            SELECT DISTINCT ON
                (fnr) fnr,
                (melding.json #>> '{}')::json ->> 'aktørId' AS aktørId
            FROM
                melding
            WHERE
                fnr NOT IN (SELECT fnr FROM sendt_feriepengerbehov)
                AND (melding.json #>> '{}')::json ->> 'aktørId' IS NOT NULL;
        """
        session.run(queryOf(query).map {
            PersonIder(
                fødselsnummer = it.long("fnr").padToFnr(),
                aktørId = it.string("aktørId")
            )
        }.asList)
    }

    fun lagreFnrForSendtFeriepengerbehov(fnr: Long) {
        using(sessionOf(dataSource)) { session ->
            val query = """INSERT INTO sendt_feriepengerbehov (fnr) VALUES ($fnr)"""
            session.run(queryOf(query).asUpdate)
        }
    }
}

data class PersonIder(val fødselsnummer: String, val aktørId: String)

internal fun Long.padToFnr() = toString().padStart(11, '0')

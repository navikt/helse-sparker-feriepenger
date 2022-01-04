package no.nav.helse.sparkerferiepenger

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource


class MeldingDao(private val dataSource: DataSource) {
    fun hentFødselsnummere(antall: Int, antallSkipped: Int) = using(sessionOf(dataSource)) { session ->
        val query = """
            SELECT DISTINCT ON
                (fnr) fnr,
                (melding.json #>> '{}')::json ->> 'aktørId' AS aktørId
            FROM
                melding
            WHERE
                (melding.json #>> '{}')::json ->> 'aktørId' IS NOT NULL
            ORDER BY fnr
            LIMIT $antall OFFSET $antallSkipped;
        """
        session.run(queryOf(query).map {
            PersonIder(
                fødselsnummer = it.long("fnr").padToFnr(),
                aktørId = it.string("aktørId")
            )
        }.asList)
    }
}

data class PersonIder(val fødselsnummer: String, val aktørId: String)

internal fun Long.padToFnr() = toString().padStart(11, '0')

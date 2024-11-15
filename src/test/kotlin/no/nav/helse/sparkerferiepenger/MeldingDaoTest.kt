package no.nav.helse.sparkerferiepenger

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

internal class MeldingDaoTest : TestAbstract() {

    @Test
    fun `kan hente ut fødselsnummere`() {
        lagreMeldinger()
        val fødselsnummere = meldingDao.hentFødselsnummere(100000, 0)
        assertEquals(8, fødselsnummere.size)
    }
}

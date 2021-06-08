package no.nav.helse.sparkerferiepenger

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MeldingDaoTest : TestAbstract() {

    @Test
    fun `kan hente ut fødselsnummere`() {
        lagreMeldinger()
        val fødselsnummere = meldingDao.hentFødselsnummere()
        assertEquals(8, fødselsnummere.size)
    }
}

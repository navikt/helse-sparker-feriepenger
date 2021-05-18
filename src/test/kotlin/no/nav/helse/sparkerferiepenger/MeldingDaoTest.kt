package no.nav.helse.sparkerferiepenger

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

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
        meldingDao.lagreFnrForSendtFeriepengerbehov(FØDSELSNUMRE.first())
        val fødselsnummer = hentFødselsnummer()
        assertEquals(1, fødselsnummer.size)
        assertEquals(FØDSELSNUMRE.first(), fødselsnummer.first())
    }

    @Test
    fun `ignorerer fødselsnummere som har sendt ut SykepengehistorikkForFeriepenger-behov`() {
        lagreMeldinger()
        meldingDao.lagreFnrForSendtFeriepengerbehov(FØDSELSNUMRE.first())
        val fødselsnummere = meldingDao.hentFødselsnummere()
        assertEquals(7, fødselsnummere.size)
    }

}

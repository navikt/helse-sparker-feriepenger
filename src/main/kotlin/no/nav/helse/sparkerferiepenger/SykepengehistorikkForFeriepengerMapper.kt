package no.nav.helse.sparkerferiepenger

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


fun mapTilSykepengehistorikkForFeriepengerBehov(
    fnr: String,
    fom: LocalDate,
    tom: LocalDate
): Map<String, Any> =
    mapOf(
        "@id" to UUID.randomUUID(),
        "@event_name" to "behov",
        "@opprettet" to LocalDateTime.now(),
        "aktørId" to "bjeff_dette_feltet_er_bevisst_satt_til_dette_mjau_mjau",
        "fødselsnummer" to fnr,
        "@behov" to listOf("SykepengehistorikkForFeriepenger"),
        "SykepengehistorikkForFeriepenger" to mapOf(
            "historikkFom" to fom,
            "historikkTom" to tom
        )
    )

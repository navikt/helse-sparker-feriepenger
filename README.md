# Sparker

## Beskrivelse

Finner utbetalings-events og legger melding om utbetaling av feriepenger på rapid.
Sparker-feriepenger kjører som en job i Kubernetes

## Kjøre jobben
1. Finn ønsket Docker-image fra feks output fra GitHub Actions

1. Legg inn ønsket SHA for docker image i relevant yml-fil, `/deploy/prod.yml` eller `/deploy/dev.yml`
1. Legg inn ønsket antall personer å sende ut behov i `ANTALL`-variabel i relevant yml-fil, `/deploy/prod.yml` eller `/deploy/dev.yml`
1. Sett hvilket `OPPTJENINGSAAR` det skal beregnes feriepenger for i relevant yml-fil
1. Slett eventuelle tidligere instanser av jobben med `k -n tbd delete job helse-sparker-feriepenger` i riktig cluster
1. Deploy jobben med `k -n tbd apply -f deploy/dev.yml` eller `k -n tbd apply -f deploy/prod.yml`
1. For å følge med på output: finn podden med `k -n tbd get pods | grep helse-sparker-feriepenger`. Tail log med: `k -n tbd logs -f <pod>`

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #område-helse.

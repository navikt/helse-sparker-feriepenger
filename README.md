Henter fødselsnumre fra databasen til spare og legger behovmeldinger for `SykepengehistorikkForFeriepenger` på rapiden, som blir besvart og får Spleis til å beregne og eventuelt utbetale feriepenger.

Sparker-feriepenger kjører som en `naisjob` i prod-gcp (eller dev-gcp).

## Forberedelser
1. I spleis, oppdater hardkodet verdi på `DATO_FOR_SISTE_FERIEPENGEKJØRING_I_INFOTRYGD` i forhold til året det skal kjøres beregning for.

## Kjøre jobben
1. Finn ønsket Docker-image fra feks. output fra GitHub Actions
1. Legg inn ønsket SHA for docker image i relevant yml-fil, `/deploy/prod-gcp.yml` eller `/deploy/dev-gcp.yml`
1. Legg inn ønsket antall personer å sende ut behov i `ANTALL`-variabel i relevant yml-fil, `/deploy/prod-gcp.yml` eller `/deploy/dev-gcp.yml`
1. Sett hvilket `OPPTJENINGSAAR` det skal beregnes feriepenger for i relevant yml-fil
1. Logg på GCP med `gcloud auth login --update-adc` 
1. Trykk på naisdevice og marker `aiven-prod`
1. Slett eventuelle tidligere instanser av jobben med `k -n tbd delete naisjob helse-sparker-feriepenger` i riktig cluster
1. Slett gammel nettverkspolicy (`k -n tbd delete networkpolicy spare-db-policy`)
1. Deploy `k -n tbd apply -f deploy/db-spare-prod.yml` for at helse-sparker-feriepenger skal få nettverkstilgang til databasen til spare
1. Deploy jobben med `k -n tbd apply -f deploy/dev-gcp.yml` eller `k -n tbd apply -f deploy/prod-gcp.yml`
1. For å følge med på output: finn podden med `k -n tbd get pods | grep helse-sparker-feriepenger`. Tail log med: `k -n tbd logs -f <pod>`
1. Dette funker sjelden. Når det blir feil i loggene kan det reddes inn med en `k patch naisjob helse-sparker-feriepenger -n tbd --type json -p='[{"op": "remove", "path": "/status/synchronizationHash"}]'`
1. Funker det fortsatt ikke? Prøv steget over igjen. Det kan ta noen forsøk
1. Slett jobben `k -n tbd delete naisjob helse-sparker-feriepenger`
1. Skru av SendFeriepengeOppdrag-toggle i spleis

## Nyttig triks når alt håp er ute
Google og oppdater readme

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #område-helse.

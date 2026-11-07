# Ndryshimet e kryera nga AI

> Eta3 · dokumentacioni në shqip · dega `localization/albanian-sq`

## 2026-11-07 — Lokalizimi shqip dhe rikthimi i konfigurimit të build-it
**Ndryshime:** u rikthye konfigurimi i plotë Android në `app/build.gradle.kts` dhe u shtua vetëm filtri `sq` krahas gjuhëve ekzistuese. U shtuan resurse shqipe për UI-në e bisedave, terminalit, statusit të ekzekutimit, aftësive Root, mjedisit Linux dhe detajeve të listave.

**Kufijtë:** ky është progres i pjesshëm; `values-sq/strings.xml` ende nuk përmban të gjitha çelësat e burimit anglisht dhe tekstet hardcoded nuk janë audituar plotësisht. Nuk u ndryshua sjellja e Runtime-it.

**Verifikimi:** ndryshimet u shtynë në këtë degë. Build-i dhe testet Gradle nuk janë ekzekutuar nga mjedisi i këtij agjenti; nuk pretendohet se build-i kalon.

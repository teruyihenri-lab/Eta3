# Gjendja aktuale — Eta3

> Përditësuar 2026-11-07; dega `localization/albanian-ui-phase1`

## E verifikuar nga burimet
- Projekti: `teruyihenri-lab/Eta3`, Android Kotlin + Compose, `applicationId=io.github.mangi.eta`.
- `values-sq/strings.xml`: **1,331 tekste** të përkthyera nga 1,331 dhe **20 grupe shumësi** nga 20; përputhja e emrave është kontrolluar automatikisht.
- Shqipja `sq` regjistrohet te `app/src/main/res/xml/locales_config.xml` dhe `app/build.gradle.kts`.
- `LocaleResourcesTest.kt` ka prova të reja për `sq-AL` (për ekzekutim nga CI).
- Dokumentacioni i 79 skedarëve është në `main` nga PR #1.

## E pambaruara dhe kufizimet
- Nuk është kontrolluar ende nëse çdo varg i ndërtuar drejtpërdrejt në Kotlin shfaqet në shqip.
- Nuk është kryer ende testim real i ekranit apo instalim APK në pajisje.
- Kalimi i testeve dhe ndërtimi duhet konfirmuar me logjet e GitHub Actions.
- Pjesa e përkthyer ruan emrat teknikë dhe emrat e modeleve të zërit.

# Ndryshimet e kryera nga AI

> Eta3 · dokumentacioni në shqip · krijuar më 2026-11-07 · dega `localization/albanian-sq`

## 2026-11-07 — Dokumentacioni fillestar
**Ndryshim:** krijimi i `AGENTS.md` dhe dokumenteve të vazhdimësisë së Eta3 në `docs/project/` në degën `localization/albanian-sq`.

**Kufiri:** vetëm dokumentacion; nuk janë ndryshuar ende rrugët runtime, Kodi Kotlin, resource-t e gjuhës apo APK-ja.

**Verifikimi:** kontrolli i skedarëve në GitHub pas commit-it. Testet e ndërtimit Android nuk janë kryer.

## 2026-11-07 — Faza e parë e lokalizimit shqip
- Shtuar `values-sq/strings.xml` me **297 tekste** dhe 20 grupe shumësash.
- Regjistruar `sq` te `locales_config.xml` dhe `localeFilters`.
- Shtuar testet e burimeve shqipe për cilësimet, menytë, terminalin, kohën dhe shumësat.
- Identiteti i aplikacionit dhe nënshkrimi nuk janë ndryshuar.
- Mbeten **1034** etiketa `string` të përkthyeshme jashtë paketës shqipe dhe auditimi i vargjeve hardcoded.
- Build dhe teste në Android: nuk janë ekzekutuar ende; nuk ka APK të verifikuar.

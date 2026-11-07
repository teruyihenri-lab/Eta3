# Ndryshimet e kryera nga AI

> Eta3 · dokumentacioni në shqip · krijuar më 2026-11-07 · dega `localization/albanian-sq`

## 2026-11-07 — Dokumentacioni fillestar
**Ndryshim:** krijimi i `AGENTS.md` dhe dokumenteve të vazhdimësisë së Eta3 në `docs/project/` në degën `localization/albanian-sq`.

**Kufiri:** vetëm dokumentacion; nuk janë ndryshuar ende rrugët runtime, Kodi Kotlin, resource-t e gjuhës apo APK-ja.

**Verifikimi:** kontrolli i skedarëve në GitHub pas commit-it. Testet e ndërtimit Android nuk janë kryer.

## 2026-11-07 — Burimet Android në shqip
- Shtuar `app/src/main/res/values-sq/strings.xml`: **1,331/1,331** tekste dhe **20/20** grupe shumësi nga skedari bazë.
- Shtuar `sq` te `locales_config.xml` dhe `localeFilters`; zgjidhet nga cilësimet e gjuhës.
- Zgjeruar `LocaleResourcesTest.kt` me pohime për gjuhën shqipe.
- Shtuar kontrollin e çelësave dhe formatuesve te `scripts/check_albanian_resources.py`.
- U ruajt shenja `formatted="false"` për `appearance_interface_scale_summary`.
- Nuk janë modifikuar `applicationId`, nënshkrimi ose logjika e runtime.
- **Ende për t'u verifikuar:** CI, ndërfaqja në pajisje, dhe tekste hardcoded në Kotlin.

## 2026-11-07 — Lokalizim i ndërfaqes së personazheve
- 24 vargje hardcoded kineze në `CharacterContextScreens`, `CharacterLibraryScreen` dhe `CharacterPortraitCard` u zëvendësuan me burime Android për anglisht, shqip, kinezisht të thjeshtuar dhe tradicionale.
- `Skills` dhe `API Key` janë përkthyer në UI shqip pa ndryshuar identifikuesit teknikë.
- Zgjeruar testet `LocaleResourcesTest`; mbetet të verifikohen nga CI.

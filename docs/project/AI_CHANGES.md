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

## 2026-11-07 — Ekrani i ndryshimit të personazhit
- U hoqën 27 etiketa kineze hardcoded nga `CharacterEditorScreen.kt`, duke përfshirë formularin, dialogët dhe përshëndetjet.
- U shtuan burime `stringResource` për EN, SQ, zh-Hans dhe zh-Hant; përshëndetjet me numër përdorin `%1$d`.
- U plotësuan etiketat teknike `Skills` dhe `API Key` në dy variantet kineze për të ruajtur testet dhe përputhjen e resource-ve.

## 2026-11-07 — Hollësitë e personazhit
- U lokalizuan etiketat, paralajmërimet dhe dialogu i fshirjes në `CharacterDetailScreen`.
- U përdor `Context.getString` në callback për titullin e përshëndetjes, sepse callback nuk është `@Composable`.
- Emri rezervë i skedarit të eksportuar merret nga burimi lokal; nuk ndryshohet formati i karakterit.

## 2026-11-07 — Paraqitja dhe përshëndetjet e personazhit
- U lokalizuan pamja e personazhit, butoni për nisjen e bisedës, titujt e përshëndetjeve, përshkrimi i avatarit dhe etiketa e autorit në `CharacterHero.kt`.
- Emrat e personazheve dhe përmbajtja e shkruar nga përdoruesi mbeten të paprekura.

# Gjendja aktuale

> Eta3 · dokumentacioni në shqip · krijuar më 2026-11-07 · dega `localization/albanian-sq`

## Verifikimi i 2026-11-07
- Repo: `teruyihenri-lab/Eta3`
- Dega aktuale: `localization/albanian-sq`
- Aplikacion Android në Kotlin / Jetpack Compose
- Konfigurimi i `sq` locale dhe skedarët e resursit të Shqipërisë janë shtuar; përfshirë çelësat primarë të UI-së dhe përkthimet e funksioneve të njohura në mjedisin Linux, terminalin, bisedën dhe lejet.
- Verifikimi i build-it nuk është kryer në një mjedis me SDK/Gradle të konfiguruar; kjo mbetet një hap i përfundimit pas kontrollit statik të resursit.

## Statusi i lokalizimit
- `app/src/main/res/xml/locales_config.xml`: e përditësuar për `sq`
- `app/build.gradle.kts`: e përditësuar për `localeFilters` të `sq`
- `app/src/main/res/values-sq/strings.xml`: përkthim fillestar i pjesës më të dukshme të UI-së
- `app/src/main/res/values-sq/*.xml`: krijuar për terminal, root, mjedis Linux dhe detaje UI
- Mbetet një pastrim i planifikuar i pjesës tjetër të string-eve të projektit për një përkthim të plotë dhe të verifikuar.

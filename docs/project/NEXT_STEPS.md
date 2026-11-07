# Hapat e ardhshëm

> Eta3 · dokumentacioni në shqip · krijuar më 2026-11-07 · dega `localization/albanian-sq`

## Hapat me përparësi
1. Përkthe të gjitha burimet e `values/strings.xml` dhe `plurals` në `values-sq/strings.xml`, me parametra dhe escape XML të saktë.
2. Shto `sq` në `locales_config.xml` dhe `localeFilters` në `app/build.gradle.kts`.
3. Audito vargjet hardcoded në Kotlin, përfshirë ndërfaqen Compose, zërin, gabimet, permission, notifications dhe accessibility.
4. Ruaj markat, emrat e API, fjalëkyçet teknike dhe serializimin.
5. Ekzekuto teste të resources, testet Gradle dhe një build; verifiko në pajisje me gjuhën shqip.
6. Përditëso `AI_CHANGES.md`, `CURRENT_STATUS.md`, `NEXT_STEPS.md`, `TEST_RESULTS.md` bashkë me çdo ndryshim.

## Vazhdimi pas fazës së parë (2026-11-07)
1. Përkthe 1034 etiketat `string` që mungojnë në `values-sq`.
2. Audito strings hardcoded nga Kotlin dhe tekstet e mjeteve, përfshirë njoftimet/aksesueshmërinë.
3. Kontrollo formulimet, apostrofat, XML escapes dhe parametrat e formatimit.
4. Ekzekuto `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` në mjedis me JDK 25 dhe Android SDK 37.
5. Testo në telefon: gjuha e aplikacionit shqip, menu, chat, zë, terminal, provider, Linux dhe njoftime.
6. Përditëso dokumentacionin me prova të rezultateve, pastaj përgatit PR të verifikuar.

## Përditësim i radhës
- Përkthe **846 vargjet e mbetura** dhe kontrollo me kujdes parametrat e formatimit.
- Audito tekstet e futur drejtpërdrejt në Kotlin; verifiko mesazhet e shfaqura nga tool/runtime.
- Kryej build, testet Robolectric, lint, testim në pajisje dhe korrigjo regresionet para bashkimit në `main`.

## Pas plotësimit të values-sq
1. Kontrollo GitHub Actions PR #3 për build/test; zgjidh çdo gabim konkret.
2. Audito 214 skedarët e ndërfaqes Kotlin, më pas mesazhet runtime/përshkrimet dinamike; lokalizo çdo tekst të dukshëm hardcoded.
3. Kontrollo shqipen në telefon, përfshirë tekstet e gjata, dialogët, terminalin, MCP, zërin dhe njoftimet.
4. Ruaj versionCode, applicationId dhe nënshkrimin e përditësimit; mos u shpreh se ka APK derisa të testohet.
5. Përditëso dokumentacionin në çdo ndryshim; bashko PR vetëm pasi të verifikohen testet.

## Auditimi i vargjeve hardcoded
- Vazhdo kontrollin e skedarëve `ui/` dhe runtime për tekste kineze/anglisht të dukshme.
- Kontrollo `OpenCodeEmbeddedGateway` për statuset dinamike të kthyer nga shërbimi.
- Verifiko dhe korrigjo çdo problem build-i në PR #3.

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

## Auditimi i mëtejshëm
- Prioritet: `SpeechInputController.kt`, `StreamingSpeechSynthesis.kt`, `CharacterLibraryStore.kt`, `AgentAppState.kt` dhe mesazhet e shërbimeve zanore.
- Ndaj komentet dhe literalet e protokolleve nga tekstet që sheh përdoruesi.
- Konfirmo build/testet në PR #3 dhe korrigjo gabimet e gjetura.

## Vazhdimi i zërit
- Audito `SpeechProtocol.kt`, `SpeechHttp.kt`, `SpeechRecorder.kt`, `StreamingSpeechSynthesis.kt` dhe gabimet e tjera që shfaqen te përdoruesi.
- Verifiko testet e build-it dhe përkthimin e njoftimeve të zërit në telefon.

## Verifikimi pas korrigjimit të testit
1. Kontrollo PR Check për commit-in e korrigjimit; nxirr gabimet e mbetura nga raporti `:app:testDebugUnitTest`.
2. Verifiko `:app:lintDebug`, `:app:testDebugUnitTest`, `:app:assembleDebug` dhe artifact-in e APK-së, nëse CI e publikon.
3. Mos bashko në `main` përpara build-it të suksesshëm dhe auditimit të literaleve Kotlin.

## Pas korrigjimit të burimit me përqindje
1. Kontrollo `PR Check` në commit-in e ri që `mergeDebugResources` të mos raportojë më gabimin e `appearance_interface_scale_summary`.
2. Nëse dështojnë teste të tjera, analizo raportet Gradle dhe korrigjo shkaqet reale pa fshehur testet.
3. Vazhdo auditimin e plotë të teksteve Kotlin dhe testimin në pajisje para bashkimit në `main`.

## Kontrolli pas riparimeve të fundit
1. Lexo `PR Check` për commit-in e fundit dhe raporto me numra Lint errors / teste të dështuara.
2. Nëse dështon një test i madhësisë së skedarit, analizo përdorimin real të memories; mos e anashkalo testin.
3. Nëse ka ikona të tjera pa mapim, shtoji në `ToolIcons.kt`, jo në listën e përjashtimeve të testit.
4. Pas build/testeve, testim real i APK-së dhe auditim i shqipes në çdo ekran.

## 2026-11-07 — Verifikimi i familjeve të modeleve
1. Kontrollo `ModelContextInferenceTest` në GitHub Actions për `familyStripsDateAndVersionSuffixes`, `overrideOfSiblingWins` dhe provat e tjera.
2. Shiko rezultatet e tjera të `testDebugUnitTest`, `lintDebug`, `assembleDebug` pa fshehur dështimet.
3. Mos bashko PR #3 në `main` pa verifikim të plotë.

## 2026-11-07 — Kontrolli i plotë i burimit
1. Analizo rezultatet e auditit të të gjithë Kotlin dhe ndaj tekstet e dukshme nga identifikuesit, komentet dhe protokollet që duhen ruajtur.
2. Lokalizo mesazhet e tjera të dukshme që nuk janë te `values`; dokumento përjashtimet e sigurisë/protokolleve.
3. Verifiko Lint, testet dhe APK Debug nga GitHub Actions, më pas testim në pajisje, sidomos Android 15/16 dhe dialogët e gjatë.

# Rezultatet e testimit

> Eta3 · dokumentacioni në shqip · krijuar më 2026-11-07 · dega `localization/albanian-sq`

## Provat dhe testet
- Leximi i repository dhe krijimi i degës GitHub: **u krye**.
- Shkrimi në GitHub i `AGENTS.md`: **u krye**, commit `217c066d3847b34b8e93c831b7ec15e614e31875`.
- Teste për burimet `values-sq`: **nuk janë kryer**.
- Gradle build / lint: **nuk janë kryer**.
- APK i instaluar/testuar: **nuk është verifikuar**.

Për çdo test të ardhshëm regjistro komandën, mjedisin, rezultatin dhe logun.

## 2026-11-07 — Kontroll statik i burimeve shqipe
- Përkthimet: **297** emra unikë u gjetën në burimin bazë.
- Vendmbajtësit e formateve `%...` për 297 vargjet: përputhje e kontrolluar kundrejt anglishtes.
- Grupet e shumësit: 20 me format `one/other`.
- Shtuar testet Robolectric për `sq-AL`, por **nuk janë ekzekutuar ende**.
- Build real, lint dhe testim në pajisje: **nuk janë kryer**.

## Kontrolli statik — zgjerimi i përkthimeve
- U verifikuan **188 çelësa të rinj** kundrejt burimit bazë; asnjë emër i panjohur ose i dyfishtë.
- U krahasuan parametrat e formatimit të teksteve të reja; nuk u gjet mospërputhje.
- **Nuk** është kryer ekzekutimi i testeve ose build-i i APK-së.

## Kontrolli i burimeve — 2026-11-07
- Nga GitHub: 1331 burime `string` të përkthyeshme u gjetën në bazë dhe 1331 të përkthyera; mungojnë **0**.
- Kontrollet e formateve `%`: **0 mospërputhje**.
- Grupe `plurals` të mbuluara: 20/20; grupet kanë `one` dhe `other`.
- Kontrolle sintaksore: 0 simbol `&` të paescape dhe 0 apostrofa të paescape në përkthimet string.
- Është shtuar `scripts/check_albanian_translations.py` dhe workflow për PR.
- Teste Gradle/Android në pajisje: ende në pritje të rezultateve CI dhe testimit real.

## Raporti i auditimit Kotlin — 2026-11-07
- Workflow `Auditimi i teksteve në Kotlin` kaloi si raport informues; zbuloi 691 rreshta të mundshëm kinezë, jo 691 gabime të konfirmuara UI.
- Kontrolli i mbulimit të burimeve shqip dhe ai i dokumentacionit kaluan në commit-et e mëparshme.
- Verifikimi i build-it Android dhe i telefonit mbetet i papërfunduar.

## Dështimi i parë i konfirmuar në Gradle — 2026-11-07
- `AgentToolCatalogTest > browserToolAllowsArbitraryUrlsAndFormSubmission FAILED`, `AgentToolCatalogTest.kt:65`.
- Shkaku: `assertFalse(description.contains("拦截"))` bie ndesh me veçoritë e tanishme të përgjimit të kërkesave të shfletuesit.
- Korrigjim: test pozitiv mbi enum-in e veprimeve dhe format e parametrave JSON; nuk u çaktivizua asnjë funksion.
- Statusi i suite-it pas korrigjimit: **ende pa rezultat CI**.

## 2026-11-07 — Gabim gjatë bashkimit të burimeve Android
- **Log i përdoruesit:** `app/src/main/res/values-sq/strings.xml:139:4`, `Multiple substitutions specified in non-positional format` për `appearance_interface_scale_summary`.
- **Diagnoza statike:** atributi `formatted="false"` ekziston në `values/strings.xml`, por mungonte në `values-sq/strings.xml`; asnjë çelës tjetër me mospërputhje të këtij atributi nuk u identifikua.
- **Korrigjimi:** atributi u rivendos dhe kontrolli statik u zgjerua.
- **Build i plotë pas ndryshimit:** ende i pakonfirmuar.

## Raporti real GitHub Actions #42401565088 (2026-11-07)
- 1783 teste gjithsej, 7 dështime dhe 1 i anashkaluar.
- Dështime: `PrefsDefaultsTest`, dy prova `ModelContextInferenceTest`, `EtaBackupRepositoryTest`, `EtaBackupStreamingTest` (Java heap), `ModelMetadataCatalogTest`, `ToolCatalogUiTest`.
- Lint: 150 MissingTranslation errors; raporti ZIP u lexua dhe klasifikua në 102 burime shqipe që mungonin nga skedarët XML shtesë dhe 48 burime kineze që mungonin pas shtimit të UI-së së re.
- Riparimet janë publikuar, por kjo pjesë dokumenton **shkaqet dhe ndryshimet**, jo një kalim testesh ende të paverifikuar.

## 2026-11-07 — ModelContextInferenceTest.kt:32
- Pritshmëria: `deepseek-v3-0324` → `deepseek-v3`.
- Para riparimit: `deepseek-v3-0324` → `deepseek-v3` → `deepseek`; `-v3` trajtohej gabimisht si prapashtesë versioni.
- Pas ndryshimit të kodit, testet e reja ende duhen ekzekutuar nga CI; ky është rezultat i analizës së kodit, jo test i kaluar.

## 2026-11-07 — Verifikimi i kërkuar për gjendjen e re
- Ndryshimet e lokalizimit UI dhe zë janë në kod dhe pritet build-i i ri për verifikim.
- Nga ky commit e tutje, një build i suksesshëm publikon `eta3-shqip-debug-apk` (APK Debug), për testim manual. Nuk garanton përditësim pa çinstalim kur certifikata ndryshon.
- Vetëm kontrolli statik i burimeve XML nuk provon që e gjithë ndërfaqja është shqip.

## 2026-11-07 — Gabim kompilimi gjatë lokalizimit
- Logu i CI: `RootShellDeviceController.kt:329`, `Unresolved reference MAX_INPUT_TEXT_CHARSkaraktere`.
- Shkaku: pas përkthimit, interpolimi Kotlin `$MAX_INPUT_TEXT_CHARS` u bashkua me `karaktere` dhe u interpretua si identifikues i vetëm.
- Korrigjimi: përdor `${MAX_INPUT_TEXT_CHARS} karaktere`, pa ndryshuar limitin e hyrjes.
- Build-i i ri duhet të konfirmojë korrigjimin; nuk është ende përfundim testimi.

## Rregulli i ndërtimit Debug — 2026-11-07
- Workflow është zgjeruar për të ruajtur APK-në vetëm kur kontrollet e build-it kanë sukses.
- Kjo hyrje përshkruan ndryshimin e konfigurimit; testi real i APK-së në pajisje mbetet i pakryer.

## 2026-11-07 — Interpolimet Kotlin në komandat e tekstit
- CI #42409706310 dështoi me tre referenca të pazgjidhura: `MAX_REPLACE_TEXT_CHARSkaraktere` në rreshtin 351 dhe `MAX_CLIPBOARD_TEXT_CHARSkaraktere` në rreshtat 620/672.
- U korrigjuan të tri vargjet në `${MAX_...} karaktere`, pa ndryshuar limitet ose sjelljen.
- Shtuar `scripts/check_kotlin_interpolation.py` te workflow i përkthimeve për të kapur modele të dyshimta përpara kompilimit.
- Ndërtimi pas korrigjimeve kërkon ende sukses të konfirmuar nga CI.

## Verifikimi i përshkrimeve të browser_use/web_search/fetch_url
- Korrigjimet janë vetëm tekste përshkruese; rezultatet e build-it pas ndryshimit janë ende pa konfirmim.

## 2026-11-07 — Përkthimi i katalogëve të kontrollit të telefonit
- Përkthyer 14 përshkrime të kontekstit dhe ekranit, 15 tekste unike të gjesteve dhe 19 tekste të hyrjes së tekstit/sistemit (48 fraza unike gjithsej). Përsëritjet e njëjta u zëvendësuan në mënyrë konsistente.
- Ruajtur emrat e mjeteve, strukturat JSON, action, index, observation_id, koordinatat dhe vlerat numerike.
- Përditësuar kontrollet tekstuale në AgentToolCatalogTest që të verifikojnë udhëzimet shqip për të njëjtin vëzhgim, ruajtjen e pemës UI dhe drejtimet scroll.
- Ndërtimi dhe rezultatet e testeve pas këtij ndryshimi ende duhen konfirmuar.

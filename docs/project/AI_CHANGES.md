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

## 2026-11-07 — Vazhduan përkthimet e ndërfaqes
- U shtuan edhe **188 tekste** në shqip për ndërfaqen, mjetet, kontrollin e pajisjes, ofruesit AI dhe mesazhet e sistemit.
- Totali i regjistruar në `values-sq`: **485/1331** burime `string` dhe **20/20** grupe `plurals`.
- U kontrolluan emrat e çelësave dhe formatimet `%` të këtyre 188 teksteve.
- Gradle build dhe testim në pajisje mbeten të pakryer.

## 2026-11-07 — Përfundimi i inventarit të burimeve shqipe
- `app/src/main/res/values-sq/strings.xml` përmban **1331/1331** burime `string` të përkthyeshme dhe **20/20** `plurals`.
- Shqipja `sq` është deklaruar në `locales_config.xml` dhe `localeFilters`; janë shtuar teste Robolectric.
- U ruajtën emrat zyrtarë të modeleve dhe zërave; përshkrimet e ndërfaqes janë në shqip.
- Shtuar një kontroll i automatizuar që verifikon numrin e çelësave dhe parametrat `%`.
- Mbeten auditimi i teksteve hardcoded Kotlin, cilësia e përkthimit dhe verifikimi i build-it/telefonit.

## 2026-11-07 — Tekstet OpenCode në Kotlin
- U zëvendësuan tekstet kineze të konfigurimit të ndërmjetësit OpenCode me 27 burime `R.string` në anglisht dhe shqip.
- Ndryshimi prek vetëm përmbajtjen e mesazheve dhe etiketave; nuk ndryshon logjikën e gateway.
- Kërkohet kontroll i build-it/CI dhe auditim i pjesës tjetër të Kotlin.

## 2026-11-07 — Cilësimet dhe sugjerimet zanore
- Lokalizuar gjashtë etiketa kineze të cilësimeve të analizës së kundërt dhe personazheve.
- Lokalizuar tri sugjerime të asistentit zanor, titujt e njoftimeve zanore, kanali, veprimi Ndalo dhe mesazhi i lejeve.
- Shtuar 13 burime të reja për anglishten dhe shqipen, pa ndryshuar IDs ose protokollet.
- Puna me tekstet hardcoded vazhdon; kontrollet Android në CI duhet të përfundojnë.

## 2026-11-07 — Statuset e njohjes zanore
- U zhvendosën shtatë mesazhe kineze të progresit dhe gabimit nga `SpeechInputController.kt` në burimet anglisht/shqip; ruhet logjika e kapjes dhe njohjes së zërit.
- Nuk ndryshuan protokollet ASR, token-ët ose rruga e audios.

## 2026-11-07 — Korrigjim i testit të shfletuesit
- Testi `AgentToolCatalogTest.browserToolAllowsArbitraryUrlsAndFormSubmission` dështoi te `AgentToolCatalogTest.kt:65` sepse priste gabimisht mungesën e funksionit `拦截` (përgjimi i kërkesave).
- Kodi aktual i `AgentBrowserToolCatalog` përfshin `set_intercept`, `list_requests`, `clear_requests`, `add_intercept_replace` dhe `clear_intercept_replace`.
- Testi tani kontrollon veprimet dhe parametrat në JSON Schema, pa varësi nga teksti kinez. Funksionaliteti i shfletuesit nuk u hoq apo ndryshua.
- Gradle / GitHub Actions duhet të ekzekutohen përsëri për konfirmim.

## 2026-11-07 — Riparim i formatimit të përqindjeve në burimet shqip
- Sipas logut të përdoruesit, `:app:mergeDebugResources` raportoi gabimin `Multiple substitutions specified in non-positional format` për `appearance_interface_scale_summary` në `values-sq/strings.xml:139`.
- Shkaku i verifikuar: burimi anglisht kishte `formatted="false"`, ndërsa përkthimi shqip e kishte humbur këtë atribut (teksti përmban `80%–110%`).
- Rivendosur `formatted="false"` në burimin shqip, pa ndryshuar tekstin që sheh përdoruesi.
- Përmirësuar `scripts/check_albanian_translations.py` për të krahasuar atributet `formatted="false"` dhe për të parandaluar regresionin.
- Nuk është verifikuar ende një build i plotë i suksesshëm pas këtij commit-i.

## 2026-11-07 — Riparimi i Lint dhe testeve Android
- Shtuar përkthimet shqipe të 102 burimeve të njoftimeve, mjedisit Linux, Root dhe detajeve të ndërfaqes; plotësuar 48 përkthime kineze për të mos prishur gjuhët ekzistuese.
- Kontrolli statik tani skanon **të gjitha** dosjet XML `values/` dhe `values-sq/`, përfshirë `formatted`, vendmbajtësit dhe shumësat.
- Korrigjuar normalizimi i prefiksit të ofruesve dhe data `-0324` në familjet e modeleve.
- Testi i inferencës tani krahason modelin pa dritare konteksti, jo modelin që e ka tashmë vlerën.
- Testi i cilësimeve përfshin çelësat ekzistues të `AGENT_REFUSAL_FAILOVER_ENABLED` dhe `AGENT_ROLEPLAY_DEFAULT`.
- `AgentMemoryStore` kthen `AgentMemoryException` kur dosja e memories nuk mund të krijohet.
- Shtuar ikonat për mjetet reverse engineering, njohurinë lokale dhe telefonatat.
- Rritur limiti i memories së JVM-së së testeve në 2 GiB për provën e ruajtjes së skedarëve të mëdhenj. Kjo kërkon verifikim CI.
- Nuk janë hequr teste ose fikur rregulla Lint.

## 2026-11-07 — Regresioni i familjeve të modeleve
- Gabimi real: `ModelContextInferenceTest.familyStripsDateAndVersionSuffixes` dështoi në rreshtin 32 për `deepseek-v3-0324`.
- Shkaku: cikli i heqjes së prapashtesave së pari hiqte `-0324`, pastaj hiqte gabimisht edhe `-v3`, duke e kthyer `deepseek-v3` në `deepseek`.
- Riparimi: mos hiq gjeneratat e modelit `-vN`; hiq vetëm datat dhe `-latest`.
- Shtuar raste regresioni për `deepseek-v3`, `deepseek-v3-latest`, `claude-sonnet-v4` dhe `qwen3-vl-v2`.
- Ndërtimi/testet CI në commit-in e ri ende duhen konfirmuar.

## 2026-11-07 — Zgjerimi i përkthimeve dhe publikimi i APK-së
- Lokalizuar kodet e dukshme të moduleve të zërit, kujtesës, katalogëve, protokollit të punës dhe personazheve në degën e punës.
- U zgjerua auditi i literaleve kineze/angleze nga ekrani dhe zëri te **i gjithë kodi Kotlin**. Ky audit është orientues dhe nuk përkthen apo ndryshon logjikën vetvetiu.
- PR Check tani publikon një APK Debug artifact në rast suksesi të të gjitha testeve/Lint/assembleDebug; debug APK mund të ketë certifikatë tjetër nga ajo e instalimit ekzistues.
- Testimi real në telefon dhe auditimi i plotë i teksteve jashtë burimeve mbeten të pakonfirmuara.

## 2026-11-07 — Përputhshmëria e statusit të ndërprerjes
- Rifreskua konstanta e brendshme `LEGACY_STOPPED_ERROR` në `已停止`, sepse shërbimi runtime e përdor si vlerë protokolli. Përkthimi i këtij kodi në `U ndal` prishte klasifikimin si ndalim dhe e shfaqte si dështim.
- Rezultati i ndalimit të krijuar nga UI përdor sërish këtë konstante; etiketa e dukshme shqip ruhet në burimet UI.
- Duhet verifikuar me CI.

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

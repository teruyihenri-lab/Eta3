# Rezultatet e testimit

> Eta3 · dokumentacioni në shqip · krijuar më 2026-11-07 · dega `localization/albanian-sq`

## Provat dhe testet
- Leximi i repository dhe krijimi i degës GitHub: **u krye**.
- Shkrimi në GitHub i `AGENTS.md`: **u krye**, commit `217c066d3847b34b8e93c831b7ec15e614e31875`.
- Teste për burimet `values-sq`: **nuk janë kryer**.
- Gradle build / lint: **nuk janë kryer**.
- APK i instaluar/testuar: **nuk është verifikuar**.

Për çdo test të ardhshëm regjistro komandën, mjedisin, rezultatin dhe logun.

## 2026-11-07 — Përkthimi shqip
- Përputhja e çelësave `strings.xml`: **1,331/1,331** pa dublikime ose mungesa (kontroll përmes leximit të repository-t).
- Përputhja e grupeve `plurals`: **20/20**.
- Parametrat e formatimit u krahasuan gjatë futjes së përkthimeve.
- Teste `LocaleResourcesTest.kt` për shqipen: **u shtuan, nuk janë ekzekutuar ende**.
- Build APK / lint / test në pajisje: **nuk janë verifikuar ende**.

- Vargjet e para statike të ekranit të personazheve u zhvendosën në resources; testet instrumentale/CI mbeten për konfirmim.

- U shtua një pohim për `character_save` në `LocaleResourcesTest`; rezultatet e CI duhen verifikuar në SHA-n e ri.

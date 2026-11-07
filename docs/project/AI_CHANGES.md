# Ndryshimet e kryera nga AI

> Eta3 · dokumentacioni në shqip · krijuar më 2026-11-07 · dega `localization/albanian-sq`

## 2026-11-07 — Regjistrimi i gjuhës shqipe dhe restaurimi i konfigurimit të build-it
**Ndryshim:** u vendos `sq` si locale e mbështetur në `app/build.gradle.kts` dhe në `app/src/main/res/xml/locales_config.xml`, duke ruajtur konfigurimin ekzistues të Android dhe gjuhët e tjera të mbështetura.

**Kufiri:** kjo është një hap i nevojshëm për aktivizimin e locale `sq`, por lokalizimi i plotë i aplikacionit nuk është ende i verifikuar në çdo ekran, string dhe Kotlin hardcoded. Nuk u ndryshuan runtime ose logjikë e aplikacionit.

**Verifikimi:** ndryshimet janë ruajtur në degën `localization/albanian-sq`; build-iAndroid dhe testet standarde nuk janë ekzekutuar nga ky mjedis dhe nuk pretendohet sukses.

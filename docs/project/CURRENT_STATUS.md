# Gjendja aktuale — Eta3

> Verifikuar më 2026-11-07 · dega `localization/albanian-ui-phase1`

## Përkthimi i burimeve të Android
- **1,407 / 1,407** burime `string` me emra të përputhur në `values-sq`, përfshirë 76 çelësa të rinj të UI-së.
- **20 / 20** grupe `plurals` për gjuhën shqipe.
- Gjuha `sq` shtuar në `locales_config.xml` dhe `localeFilters`.
- Testet `LocaleResourcesTest.kt` kanë prova për `sq-AL`.

## Kodi UI i përkthyer në këtë fazë
- `CharacterContextScreens.kt`, `CharacterLibraryScreen.kt`, `CharacterPortraitCard.kt`.
- `CharacterEditorScreen.kt`, `CharacterDetailScreen.kt`, `CharacterHero.kt`.
- Burime për anglisht, shqip, kinezisht të thjeshtuar dhe tradicionale, pa ndryshuar formatin e të dhënave të personazheve.

## Çfarë nuk është përfunduar
- `CharacterWorldbookEditor.kt` dhe disa faqe të kujtesës/udhëzimeve përmbajnë ende tekste kineze hardcoded.
- Nuk është verifikuar mbulimi i çdo teksti të dukshëm nga 596 skedarë Kotlin.
- PR #2 është i hapur. Kontrolli i dokumentacionit ka kaluar në SHA `5e35a5e1`; kontrolli Android `lint/test/assembleDebug` ishte ende në ekzekutim në verifikimin e fundit.
- Nuk është bërë instalim apo test vizual në telefon.
- **Mos e quaj përkthimin e plotë të aplikacionit ose APK-në të përfunduar.**

# Hapat e ardhshëm

> Eta3 · dokumentacioni në shqip · dega `localization/albanian-sq`

## Hapat me përparësi
1. Kontrollo të gjitha `string` dhe `plurals` në `app/src/main/res/values/strings.xml` dhe krahaso me `values-sq` për çelësa të munguar ose të dyfishta.
2. Përshtat kurdo tekst hardcoded në Kotlin/XML me `values-sq`, duke ruajtur emrat e API, markat, komandat dhe çelësat JSON si origjinal.
3. Verifiko XML dhe formatime placeholder (`%1$s`, `%d`, `formatted`, `translatable`, `plurals`) për të shmangur gabime runtime.
4. Ekzekuto Gradle Build/Test në mjedis me JDK 25 dhe Android SDK; regjistro rezultatet në `TEST_RESULTS.md`.
5. Verifiko në pajisje/emulator dhe përditëso dokumentacionin pas çdo korekcioni real.

# Hapat e ardhshëm

> Eta3 · lokalizimi shqip · dega `localization/albanian-sq`

## Hapat me përparësi
1. Rikupero/numëro të gjithë çelësat `string` dhe `plurals` nga burimet bazë dhe përkthe çelësat që mungojnë në `values-sq`, pa dublikuar emra resursesh.
2. Kontrollo çdo skedar `values-sq/*.xml` për XML të vlefshëm, placeholder-a identikë dhe çelësa të dyfishuar.
3. Audito tekstet hardcoded të dukshme në Kotlin dhe XML; ruaj identifikuesit teknikë, API-të, komandat dhe emrat e markave.
4. Ekzekuto testet dhe build-in Android në mjedis me JDK 25 dhe Android SDK; rregullo gabimet reale dhe shëno provat në `TEST_RESULTS.md`.
5. Verifiko gjuhën shqipe në pajisje ose emulator, pastaj përditëso këtë status dhe regjistrat e lokalizimit.

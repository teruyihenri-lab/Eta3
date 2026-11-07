# Rezultatet e testimit

> Eta3 · dega `localization/albanian-sq`

## Regjistri i verifikimit
- `sq` është shtuar si locale e mbështetur në `androidResources` dhe `locale-config`.
- Pjesa e parë e lokalizimit është aktivizuar, por nuk ka prova të verifikuara për build/tests në Android.
- Build/testet Gradle: **nuk janë ekzekutuar** në këtë mjedis.
- Duhet të ekzekutohet `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` me JDK 25 dhe Android SDK për verifikim të plotë.

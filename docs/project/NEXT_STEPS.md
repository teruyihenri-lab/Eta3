# Hapat e ardhshëm

> Përditësuar 2026-11-07; dega `localization/albanian-ui-phase1`

1. Verifiko që `check_albanian_resources.py` dhe testet Gradle kalojnë në CI.
2. Audito tekstet hardcoded në 596 skedarë Kotlin të `app/src/main/kotlin`; transfero tekstet që shfaqen te përdoruesi në burime të lokalizuara.
3. Kontrollo të gjitha ekranet: modelet, MCP, chat, zërin, njoftimet, lejet, terminalin, shfletuesin, kujtesën, aftësitë, aksesueshmërinë dhe dialogët.
4. Rishiko cilësinë e përkthimit nga folës i shqipes dhe ndërprerjen e teksteve të gjata.
5. Kryej build të nënshkruar me çelësin origjinal dhe instalim si përditësim në telefon, pa çinstalim.
6. Me çdo ndryshim përditëso `AI_CHANGES.md`, `CURRENT_STATUS.md`, `NEXT_STEPS.md` dhe `TEST_RESULTS.md`.

7. Vazhdo heqjen e vargjeve hardcoded kineze në ekranet `CharacterEditorScreen`, `CharacterDetailScreen`, `CharacterHero`, `CharacterWorldbookEditor`, `AgentMemoryScreen` dhe `AgentUserPromptScreen`.

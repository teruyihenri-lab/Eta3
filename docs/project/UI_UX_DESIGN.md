# Dizajni i ndërfaqes

> Eta3 · dokumentacioni në shqip · krijuar më 2026-11-07 · dega `localization/albanian-sq`

## Kontrata e ndërfaqes shqip
- Gjuhë: shqip standard, shkronjat ë/ç UTF-8.
- Përkthe çdo etiketë të dukshme, placeholder, përshkrim, dialog, njoftim dhe accessibility contentDescription.
- Ruaj numrat, argumentet e formatimit (`%1$s`, `%1$d`), escape XML dhe shumësat.
- Testo tekstet e gjata për prerje në ekran dhe për ekranet e vogla.
- Emrat e markave dhe termat e papërkthyeshëm të API-ve nuk ndryshojnë.

## Implementim 2026-11-07
- `sq` shtohet si gjuhë e aplikacionit; `values-sq/strings.xml` plotëson tekstet e përkthyeshme në burimet Android.
- Vargjet e gjata, ndërfaqet Compose dhe tekstet hardcoded mbeten për rishikim me test real.

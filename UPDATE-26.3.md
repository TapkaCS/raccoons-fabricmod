# Aktualizace na Minecraft 26.3 (Fabric)

Nahrany projekt puvodne cilil na Minecraft 26.1.2.

## Zmeny
- Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3.
- GeckoLib 5.5.7, stabilni Fabric Loom 1.17.21, Gradle 9.6.0.
- PlayerTrigger: novy balicek net.minecraft.advancements.triggers.
- Rotace predmetu v tlame: PoseStack.rotateDegrees, zachovan uhel 90 stupnu.
- Herni logika, identifikatory, modely, animace, textury, zvuky a recepty zachovany.
- Opraven format podminek achievementu ochočení a rozmnožování pro 26.3: objekt misto pole, type misto condition a minecraft:entity_type misto stareho type v predikatu.
- Podminky zustavaji omezene na entitu raccoons:raccoon.

## Metadata
- Obnovena puvodni ikona, autor TapkaCS, popisek a odkazy z verejne vydane verze 1.2.0 pro 26.2.
- Na zadost autora nastavena PolyForm Noncommercial 1.0.0 v metadatech a souboru LICENSE.
- Required Notice: Copyright (c) 2026 TapkaCS.
- Tato zmena nerusi opravneni ke kopiim drive vydanym pod MIT.
- Kod ani herni assety (krome ikony) se touto opravou nemeni.

## Instalace
Pouzij Minecraft 26.3, Fabric Loader 0.19.5 nebo novejsi a Javu 25.
Do mods vloz raccoons-1.3.0-mc26.3.jar, Fabric API 0.161.0+26.3 a GeckoLib pro Fabric 26.3 ve verzi 5.5.7.
Predchozi JAR tohoto modu nahrad novym; neponechavej obe verze soucasne.

## Overeni
Ciste sestaveni ./gradlew clean build uspesne (JDK 25).
Zkontrolovana metadata vysledneho JARu a nezmenene herni assety.
Spusteni Fabric serveru 26.3: registry a 1873 achievementu uspesne nacteny, server dosahl Done.
Projekt neobsahuje automaticke testy. Spusteni klienta ani ziskavani achievementu hranim nebylo overeno.

## Sestaveni ze zdroju
Linux: ./gradlew build
Windows: gradlew.bat build
Vystup: build/libs/raccoons-1.3.0.jar

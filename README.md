# ⚔️ Realm Builder

> *Zbuduj miasto. Przeżyj 80 tur. Przejdź do historii.*

**Realm Builder** to turowa gra strategiczno-ekonomiczna napisana w Javie z interfejsem graficznym JavaFX. 

Wciel się we Władcę - rozbudowuj osadę od zera, zarządzaj pięcioma zasobami, reaguj na losowe zdarzenia i przetrwaj 20 lat panowania.

---

## 🎮 Jak grać

Jedna tura = jeden dzień w mieście. W każdej turze:

1. Przeglądasz stan zasobów i mapę miasta
2. Podejmujesz decyzję - budujesz, ulepszasz lub przechodzisz dalej
3. Miasto automatycznie produkuje zasoby, pobiera podatki i konsumuje żywność
4. Losowo może pojawić się zdarzenie (najazd, susza, festiwal...)
5. Rada Miasta może zaoferować pomoc za złoto
6. Co 4 tury zmienia się pora roku

**Cel:** Przeżyć 80 tur (20 lat). Przegrywasz gdy złoto lub populacja spada do zera.

---

## 🌟 Mechaniki gry

### 📊 Zarządzanie zasobami
Balansuj pięcioma zasobami jednocześnie:

| Zasób | Źródło | Zużycie |
|---|---|---|
| 💰 Złoto | Rynek, podatki od populacji | Budowa, doradcy |
| 🌾 Żywność | Farma, Studnia | Populacja je każdą turę |
| 🌲 Drewno | Tartak | Ulepszanie budynków |
| ⚔️ Morale | Kościół, Koszary | Spada przy głodzie i zdarzeniach |
| 👥 Populacja | Domy | Spada przy głodzie i zarazie |

### 🏗️ Budynki
Osiem budynków do wzniesienia, każdy klikalny na mapie:

| Budynek   | Koszt | Produkuje |
|-----------|---|---|
| 🌽 Farma  | 100💰 | +15 żywności/turę |
| 🌲 Tartak | 120💰 | +20 drewna/turę |
| 🏠 Domy   | 80💰 | +10 populacji/turę |
| 💰 Rynek  | 150💰 | +25 złota/turę |
| ⚔️ Koszary | 130💰 | +8 morale/turę |
| ⛪ Kościół | 110💰 | +12 morale/turę |
| 💧 Studnia | 90💰 | +8 żywności/turę |
| 🏰 Zamek  | 300💰 | +20 morale + prestiż |

Każdy budynek można ulepszyć (koszt: 80💰 + 30🌲) - produkcja rośnie z każdym poziomem.

### 🌦️ Pory roku
Co 4 tury zmienia się pora roku = wpływa na produkcję zasobów:

- 🌸 **Wiosna**: +20% żywności, +10% morale, bonus +5 morale
- ☀️ **Lato**: +30% żywności, +10% drewna, bonus +10 żywności
- 🍂 **Jesień**: +10% żywności, +20% złota, bonus +15 złota
- ❄️ **Zima**: -30% żywności, -20% drewna, malus -20 żywności i -5 morale

### ⚡ Zdarzenia losowe
Z 40% szansą na turę może wystąpić jedno z sześciu zdarzeń:

| Zdarzenie | Pora roku | Efekt |
|---|---|---|
| 🌩️ Burza | Jesień | -50💰 -15 morale |
| ⚔️ Najazd | Lato | -20 populacji -40💰 |
| ☀️ Susza | Lato | -60🌾 -10 morale |
| 🎉 Festiwal | Wiosna | +20 morale +10 populacji |
| ☠️ Zaraza | Zima | -30 populacji -20 morale |
| 🌾 Żniwa | Jesień | +80🌾 +30💰 +10 morale |

### 👑 Rada Miasta
Trzej doradcy mogą zredukować skutki negatywnych zdarzeń:

| Doradca | Specjalność | Koszt | Pomaga przy |
|---|---|---|---|
| 🗡️ Rycerz Aldric | Walka | 30💰 | Najazd, Burza |
| 🔮 Wiedźma Morgana | Magia | 40💰 | Zaraza, Susza |
| 💼 Kupiec Benedikt | Handel | 35💰 | Burza, Susza, Najazd |

### 🏅 System rang
Po zakończeniu gry otrzymujesz rangę:

| Ranga | Warunek |
|---|---|
| ⭐ Nowicjusz | 20 tur |
| ⭐⭐ Zarządca | 40 tur |
| ⭐⭐⭐ Władca | 60 tur |
| ⭐⭐⭐⭐ Król | 80 tur |
| ⭐⭐⭐⭐⭐ Legenda | 80 tur + populacja 500+ |

### 🎖️ Osiągnięcia
Siedem osiągnięć do odblokowania:

- **Niezniszczalny** - przeżyj 3 najazdy z rzędu
- **Bogacz** - zgromadź 5000 złota jednocześnie
- **Budowniczy** - zbuduj wszystkie dostępne budynki
- **Dobry władca** - utrzymaj morale powyżej 80 przez 10 kolejnych tur
- **Metropolia** - osiągnij 500 mieszkańców
- **Weteran** - ukończ 40 tur
- **Legenda** - 80 tur z populacją 500+

---

## 💻 Technologie

### Język i narzędzia
- **Java 17+**
- **JavaFX 21** - interfejs graficzny (FXML + CSS)
- **Maven** - zarządzanie zależnościami i budowanie
- **Gson 2.10** - zapis stanu gry w formacie JSON
- **JUnit 5** - testy jednostkowe


| Temat | Zastosowanie w projekcie                                                                                                                                                     |
|---|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Refleksja i adnotacje** | `@Building`, `@Event`, `@AdvisorPlugin` - silnik skanuje pakiety i ładuje klasy automatycznie przez `ClassLoader` i `Class.forName()`. Zero ręcznego rejestrowania.          |
| **Stream API** | `ResourceCalculator` oblicza produkcję, `AchievementTracker` filtruje osiągnięcia, `TurnProcessor` filtruje zdarzenia - wszystko przez `.stream().filter().mapToInt().sum()` |
| **Kolekcje i generyki** | `EnumMap<ResourceType, Integer>` dla zasobów, `List<Building>`, `Map<Season, List<GameEvent>>`, `BuildingLoader` zwraca `List<pl.realmbuilder.interfaces.Building>`          |
| **OOP** | Hierarchia: `GameEvent → NegativeEvent`, interfejsy `Building`/`GameEvent`/`Advisor`, wzorzec Strategy w doradcach, wzorzec Observer/Callback w `TurnProcessor`              |
| **JUnit** | `CityTest`, `FarmTest`, `EconomyProcessorTest`, `AchievementTrackerTest` = każdy komponent testowalny w izolacji                                                             |

### Wzorce projektowe

- **Open/Closed Principle (SOLID)** - nowy budynek = jedna klasa z `@Building`, zero zmian w silniku
- **Strategy** - `Advisor.canHelp()` i `Advisor.help()` - każdy doradca ma własną strategię
- **MVC** - Model (`City`), View (FXML), Controller (`GameController`)
- **Observer/Callback** - `Consumer<GameEvent>` w `TurnProcessor` oddziela silnik od UI
- **DTO** - `GameSave` przenosi dane do/z JSON bez naruszania hermetyczności `City`

---

## 🏛️ Architektura projektu

```
src/main/java/pl/realmbuilder/
│
├── annotations/          # Adnotacje systemu pluginowego
│   ├── Building.java     # @Building(name, cost, produces, amount)
│   ├── Event.java        # @Event(name, probability, season)
│   └── AdvisorPlugin.java# @AdvisorPlugin(name, specialty, cost)
│
├── interfaces/           # Kontrakty między warstwami
│   ├── Building.java     # getName, getCost, produce(City), upgrade
│   ├── GameEvent.java    # getName, apply(City), getProbability
│   └── Advisor.java      # canHelp(GameEvent), help(City, GameEvent)
│
├── model/                # Czyste dane, zero logiki UI
│   ├── City.java         # Stan miasta - zasoby, budynki, tura
│   ├── Season.java       # Enum z metodą next() i mnożnikami
│   ├── ResourceType.java # Enum - GOLD, FOOD, WOOD, MORALE, POPULATION
│   ├── Difficulty.java   # Enum z mnożnikami produkcji i zdarzeń
│   ├── Achievement.java  # Model osiągnięcia z flagą unlocked
│   └── GameSave.java     # DTO do serializacji przez Gson
│
├── engine/               # Logika gry
│   ├── BuildingLoader.java    # Refleksja - skanuje buildings/
│   ├── EventLoader.java       # Refleksja - skanuje events/
│   ├── AdvisorLoader.java     # Refleksja - skanuje advisors/
│   ├── TurnProcessor.java     # Orkiestrator jednej tury
│   ├── ResourceCalculator.java# Stream API - oblicza produkcję
│   ├── SeasonManager.java     # Efekty sezonowe i mnożniki
│   ├── EconomyProcessor.java  # Konsumpcja żywności + podatki
│   ├── AchievementTracker.java# Śledzi i odblokowuje osiągnięcia
│   ├── RankCalculator.java    # Oblicza rangę i wynik końcowy
│   ├── SaveManager.java       # Gson - zapis/wczytanie JSON
│   └── TurnNarrator.java      # Narracja fabularna i ostrzeżenia
│
├── buildings/            # Implementacje - Farm, Sawmill, Houses...
├── events/               # Implementacje - StormEvent, RaidEvent...
├── advisors/             # Implementacje - Knight, Witch, Merchant
│
└── ui/                   # Warstwa JavaFX
    ├── App.java               # Główna klasa, zarządzanie scenami
    ├── MenuController.java    # Ekran startowy
    ├── GameController.java    # Główny ekran gry + mapa SVG
    ├── EventDialogController.java # Dialog zdarzeń
    └── EndScreenController.java   # Ekran końcowy z rangą
```

---

## 🚀 Uruchomienie

### Wymagania
- Java 17 lub nowsza
- Maven 3.8+
- Połączenie z internetem przy pierwszym uruchomieniu (Maven pobiera zależności)

### Uruchomienie przez Maven
```bash
# Uruchom grę
mvn javafx:run
```

### Budowanie pliku JAR
```bash
mvn package
java -jar target/realm-builder-1.0-SNAPSHOT.jar
```

### Uruchomienie w IntelliJ IDEA
1. Otwórz projekt (`File → Open`)
2. Poczekaj aż Maven pobierze zależności
3. W panelu Maven kliknij `Plugins → javafx → javafx:run`

---

## 🧪 Testy

```bash
mvn test
```

Testy obejmują:
- `CityTest` - inicjalizacja zasobów, warunki końca gry, zmiana pór roku
- `FarmTest` - produkcja, ulepszanie, mnożniki trudności
- `EconomyProcessorTest` - konsumpcja żywności, podatki, głód
- `AchievementTrackerTest`  odblokowywanie osiągnięć

---

## 📁 Zapis gry

Stan gry zapisywany jest do pliku `realm_builder_save.json` w katalogu roboczym. Plik jest czytelny. Można go otworzyć w dowolnym edytorze tekstowym.

---

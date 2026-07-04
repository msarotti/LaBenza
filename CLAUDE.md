# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

LaBenza is an Android app that shows current Italian fuel prices for stations near
a location (current GPS position or a typed/searched place). It has two front-ends
that share the same data layer: a Jetpack Compose phone UI and an Android Auto
(projected, POI-category) car UI.

A core design constraint is that the app must work on **de-Googled devices**: it
uses no Google Play Services. Location comes from the AOSP `LocationManager`,
geocoding from OpenStreetMap Nominatim, and navigation is launched via a
vendor-neutral `geo:` URI rather than a Maps-specific intent.

## Build & Test

Android studio path is: ~/GitApps/android-studio/
The JDK is contained in the Android Studio folder so search it here.

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew installDebug           # build + install on a connected device/emulator
./gradlew test                   # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest   # instrumented tests (needs a device/emulator)
./gradlew lint                   # Android lint

# Run a single unit test
./gradlew test --tests "com.example.labenza.ExampleUnitTest.addition_isCorrect"
```

Dependencies are managed through the Gradle version catalog at
`gradle/libs.versions.toml` — add/upgrade libraries there, not inline in
`app/build.gradle.kts`.

## Architecture

Data flows in one direction: **API → Repository → ViewModel (StateFlow) → UI**.
There is no DI framework; dependencies are constructed by hand in `MainActivity`
(phone) and directly inside `FuelListScreen` (car).

### Data layer (`data/`)
- `api/FuelPriceApi` — Retrofit interface for the official MIMIT "Osservaprezzi
  Carburanti" backend (`carburanti.mise.gov.it`), `POST ospzApi/search/zone`.
- `api/GeocodingApi` — Retrofit interface for OSM Nominatim (search + reverse).
  Nominatim's usage policy requires the descriptive `User-Agent` header (already
  set) and ~1 req/sec, which is why autocomplete input is debounced.
- `repository/FuelRepository`, `repository/GeocodingRepository` — each owns its
  own Retrofit instance and `kotlinx.serialization` `Json` config
  (`ignoreUnknownKeys = true`). Repository methods return `Result<…>` (fuel) or
  null/empty (geocoding) and never throw.
- `model/` — `@Serializable` DTOs. Note the API quirks encoded here: `Station.distance`
  arrives as a *string* (exposed as `distanceKm`), and fuel prices are nested in a
  `fuels` list keyed by `fuelId` (1 = benzina, 2 = gasolio/diesel). `Station.benzinaPrice`
  / `dieselPrice` compute the cheapest self/served price for that fuel.

### Presentation
- `ui/viewmodel/FuelViewModel` — single ViewModel for the phone UI. Exposes
  `uiState` (sealed `FuelUiState`: Idle/Loading/Success/Error), `query`, and
  `suggestions` as `StateFlow`s. Autocomplete is a debounced
  (`AUTOCOMPLETE_DEBOUNCE_MS`) flow on `_query`; `suppressNextQuery` prevents
  re-querying the text that a just-picked suggestion wrote back into the field.
- `ui/screens/MainScreen` — the Compose UI; collects the ViewModel's flows.
- `MainActivity` — wires up repositories + `LocationHelper`, requests location
  permission, sets the Compose content. Uses an inline `ViewModelProvider.Factory`.

### Android Auto (`car/`)
- `FuelCarAppService` — the `CarAppService` entry point (registered in the
  manifest under the `androidx.car.app.category.POI` intent filter). Currently
  uses `ALLOW_ALL_HOSTS_VALIDATOR` — tighten before production.
- `FuelListScreen` — `PlaceListMapTemplate` screen with its own `State` sealed
  class and the standard `state = …; invalidate()` redraw pattern. It constructs
  its own `FuelRepository` / `LocationHelper` (does not share the ViewModel) and
  handles its own runtime permission request and the `ConstraintManager` row-count
  limit.
- `StationDetailScreen` — pushed from a list row; "Naviga" action launches
  navigation via `MapNavigation.geoUri`.

### Shared helpers
- `location/LocationHelper` — `LocationManager`-only location (last-known, then a
  single timed fix over GPS/network). Used by both front-ends.
- `util/MapNavigation` — builds the vendor-neutral `geo:` URI used by both
  front-ends to start turn-by-turn navigation.

## Conventions

- All user-facing strings are in **Italian** (the app targets Italian fuel data).
- Both UI front-ends must keep working without Google Play Services — do not
  introduce `play-services-location`, the Maps SDK, or Google-specific intents;
  go through `LocationHelper` and `MapNavigation` instead.

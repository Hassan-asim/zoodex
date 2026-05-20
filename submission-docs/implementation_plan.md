# Zoodex - Detailed Implementation Plan

## Overview
This document outlines the chronological implementation plan used to build the Zoodex application from scratch. It breaks down the development process into distinct architectural and feature-based phases, highlighting the integration of UI, local state, hardware APIs, and cloud services.

---

## Phase 1: Project Scaffolding & Core UI Architecture
**Objective:** Establish the foundational Android project, declarative UI framework, and visual identity.
1. **Initialize Project:** Create a new Android Studio project with an empty Jetpack Compose Activity (Kotlin).
2. **Define Visual Theme (`ui/theme/`):** 
   - Implement a custom `Color.kt` and `Theme.kt` containing neon-cyberpunk hexadecimal tokens (e.g., Neon Cyan, Obsidian Black, Apple Red).
   - Configure typography using modern, bold sans-serif fonts.
3. **Setup Navigation:** 
   - Implement a `NavHost` in `MainActivity.kt` to handle routing between primary screens (Hub, Map, Arena, Social, Roster).
4. **Build Core Screens (Placeholder State):** Scaffold empty composables for each major feature to verify routing logic.

---

## Phase 2: Data Modeling & Local Persistence
**Objective:** Create the internal data structures representing the game's mechanics and a system to persist progress locally.
1. **Animal Database (`AnimalDatabase.kt`):** 
   - Define data classes for `Beast` and `AnimalData`.
   - Populate a static repository of 60+ unique beasts, assigning elemental types (Cyber, Void, Fire, Earth, Water, Electric), descriptions, emojis, and base combat stats.
2. **State Management (`GameState.kt`):** 
   - Implement a Singleton object to hold the player's volatile state (current team, unlocked beasts, XP, Gold, Level) using Compose's `mutableStateOf` and `mutableStateListOf` for reactive UI updates.
3. **Local Storage:** 
   - Write custom `save()` and `init()` methods inside `GameState` using `SharedPreferences`.
   - Implement JSON serialization/deserialization to ensure the player's beast roster and team configurations persist across application restarts.

---

## Phase 3: Real-World Mapping & Hardware Integration
**Objective:** Gamify physical movement by integrating map rendering and GPS hardware tracking.
1. **Integrate MapLibre SDK:**
   - Add MapLibre dependencies to `build.gradle.kts`.
   - Configure `AndroidView` in Compose to host the MapLibre `MapView`.
   - Apply a custom dark "Stamen Toner" / OpenFreeMap vector tile style URL to match the cyberpunk aesthetic.
2. **GPS Hardware Tracking:**
   - Implement Android Runtime Permissions for `ACCESS_FINE_LOCATION`.
   - Connect Google Play Services `FusedLocationProviderClient` to poll coordinates every 2 seconds.
3. **Territory Logic (`MapScreen.kt`):**
   - Track the user's continuous path into a `walkedPath` array.
   - Use the Haversine formula to calculate the physical radius of the walked area once tracking stops.
   - Generate GeoJSON polygon configurations on the fly and inject them into MapLibre's `FillLayer` to paint claimed territories visually.

---

## Phase 4: Cloud Backend & Social Systems
**Objective:** Connect the local app to a cloud backend to allow for multiplayer interactions and persistent world state.
1. **Supabase Configuration:** 
   - Set up a Supabase PostgreSQL instance.
   - Write SQL schemas to generate `operative_profiles`, `operative_messages`, and `friendships` tables.
   - Configure Row Level Security (RLS) policies to protect data manipulation.
2. **REST API Client (`SupabaseService.kt`):**
   - Write a lightweight HTTP REST client using `HttpURLConnection` and Kotlin Coroutines (avoiding heavy SDK dependencies).
   - Implement functions for user initialization, sending/receiving messages, and managing friend requests.
3. **Territory Synchronization:**
   - Push calculated territory coordinates to the Supabase database.
   - **Technical Pivot:** Dynamically generate unique UUID prefixes (`TERRITORY_xxxx`) to bypass strict SQL `UNIQUE` constraints, allowing users to claim multiple disjointed territories securely.

---

## Phase 5: Arena Combat & Gameplay Loop
**Objective:** Build an engaging turn-based RPG battle system to utilize captured beasts.
1. **State Machine Design (`ArenaScreen.kt`):**
   - Create an `ArenaPhase` enum (`PRE_MATCH`, `SEARCHING`, `PLAYER_TURN`, `ENEMY_TURN`, `VICTORY`, `DEFEAT`).
   - Force the `Activity` into Landscape mode to optimize the battle view layout.
2. **Dynamic Encounters:**
   - Write algorithms to dynamically generate AI boss stats (HP, Strength, Defense, Speed) scaling randomly within a range based on the player's current level.
3. **Combat Mathematics:**
   - Implement damage formulas that calculate outgoing damage based on the attacker's `strength` and apply mitigation based on the defender's `defense`.
4. **Battle UI & Animations:**
   - Construct custom HP bars using constrained Compose `Box` modifiers (`Alignment.CenterStart`) to accurately track health fractions.
   - Apply infinite transition animations (floating, shaking on hit) to beast emojis to simulate dynamic combat.

---

## Phase 6: Polish, Optimization & QA
**Objective:** Refine the user experience, fix edge-case bugs, and finalize assets.
1. **UI Constraints & Overlap Fixes:**
   - Refactor the Arena layout to remove redundant combat logs and anchor action buttons horizontally, preventing UI clipping on smaller landscape screens.
2. **Crash Resolution:**
   - Identify and remove unsupported XML `<shape>` drawables that caused fatal `IllegalArgumentException` crashes when rendered via Compose `painterResource`.
3. **Asset Finalization:**
   - Embed custom application icons (`zoodex_new_logo.png`) into the Android Manifest.
4. **Documentation:**
   - Generate comprehensive submission documentation (`README`, `walkthrough`, `instructions`, `TASKS_AND_CONTRIBUTIONS`).

# Zoodex

## High-Level Application Overview
Zoodex is a futuristic, neon-cyberpunk-themed Android application that combines location-based territory conquest with turn-based RPG beast battling. Users act as "Operatives" exploring the real world, claiming territories using live GPS tracking, capturing digital beasts, and challenging rival operatives in a highly stylized Combat Arena.

## Purpose of the Application
The primary purpose of Zoodex is to gamify physical exploration and walking. By rewarding real-world movement with territory control and beast progression, Zoodex bridges the gap between fitness tracking and engaging RPG mechanics, encouraging users to go outside and interact with their environment dynamically.

## Core Features and Functionality
- **Territory Conquest (GPS Tracking):** Users walk in the real world to track and claim geographical zones. The map integration records their path using MapLibre and checks for overlaps with rival territories.
- **Combat Arena (Turn-Based Battles):** A 3v3 turn-based RPG battle system where players deploy their captured beasts against AI bosses or Rival operatives. Features dynamic damage mitigation and randomized AI stat generation.
- **Real-Time Comms Center:** An integrated chat and friends system powered by Supabase, allowing users to add friends via unique Operative Codes, track online status, and send direct messages.
- **Dynamic Beast Roster:** Over 60 unique beasts across various elements (Cyber, Void, Fire, Water, Earth, Electric), each with distinct lore, elemental affinities, and base stats.
- **Persistent State Management:** Local persistence ensures progress is always saved locally, while Supabase syncs territory and social states securely.

## Tech Stack Used
- **Frontend / UI:** Android Jetpack Compose (Kotlin)
- **Map Engine:** MapLibre Android SDK (OpenFreeMap & Stamen Toner styles)
- **Backend / Database:** Supabase (PostgreSQL, REST API)
- **Location Services:** Google Play Services FusedLocationProviderClient
- **Asynchronous Operations:** Kotlin Coroutines & Dispatchers

## Project Structure Overview
- `app/src/main/java/com/Sufi/zoodex/ui/screens/` - Contains all Compose UI screens (ArenaScreen, MapScreen, SocialScreen, RosterScreen, etc.).
- `app/src/main/java/com/Sufi/zoodex/data/` - Holds core data models and singletons (GameState, AnimalDatabase, SupabaseService).
- `app/src/main/java/com/Sufi/zoodex/ui/theme/` - Contains typography, cyberpunk color tokens, and custom styling configurations.
- `app/src/main/res/` - Contains Android resources including the custom Zoodex logo icon.

## Key Modules/Components
- **GameState (`GameState.kt`):** The single source of truth for the local player's inventory, levels, stats, and unlocked beasts, utilizing SharedPreferences.
- **SupabaseService (`SupabaseService.kt`):** Handles all asynchronous HTTP REST calls to the Supabase backend for friendships, messages, and territory claims.
- **MapScreen (`MapScreen.kt`):** Integrates MapLibre and LocationServices to draw real-time walked paths and claimed polygons on the map.
- **ArenaScreen (`ArenaScreen.kt`):** Manages the complex state machine for turn-based combat phases, damage calculations, and animations.

## User Roles and Workflows
1. **Operative (Player):** The sole user role.
   - *Workflow - Setup:* On first launch, the user defines a Callsign and selects a Faction, generating an Operative Profile.
   - *Workflow - Conquest:* Navigates to Map -> Grants GPS permission -> Taps Start Tracking -> Walks -> Taps Stop -> Territory is saved to Supabase.
   - *Workflow - Combat:* Navigates to Arena -> Selects Team -> Enters Battle -> Uses tactical elemental moves -> Gains XP/Gold upon victory.

## Future Improvement Suggestions
- **Multiplayer PvP Sync:** Upgrade the turn-based Arena to support live WebSocket-based PvP battles instead of asynchronous rival snapshots.
- **Expanded Faction Systems:** Introduce global faction leaderboards and weekly territory control rewards.
- **Enhanced Map Data:** Integrate POIs (Points of Interest) that spawn rare beasts in specific real-world locations based on biome.

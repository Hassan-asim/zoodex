# Tasks and Contributions

## Complete Task Breakdown of the Application Build
The development of Zoodex was split into distinct phases targeting UI/UX, Local Systems, and Remote Backend Integrations.

1. **Initial Scaffold:** Setup Jetpack Compose, routing, and neon typography.
2. **Database & Lore:** Design the `AnimalDatabase` containing stats, elements, and descriptions for over 60 unique creatures.
3. **Local State:** Build `GameState.kt` to serialize user inventory, gold, and XP to `SharedPreferences`.
4. **Backend Setup:** Configure Supabase PostgreSQL tables (Profiles, Messages, Friendships) and robust RLS policies.
5. **Comms Integration:** Build `SupabaseService.kt` allowing users to fetch profiles and send messages securely.
6. **Location/Map Integration:** Embed MapLibre into Android, hook into Google FusedLocation provider, and draw walked territory paths dynamically using GeoJSON.
7. **Arena Mechanics:** Code the turn-based math, phase state machine, and battle UI inside `ArenaScreen`.

## Contribution Summary

### What the AI Agent (Antigravity) Handled
- **Architectural Bug Fixes:** Identified and fixed critical architectural crashes in Jetpack Compose (e.g., removing unsupported `<shape>` XML vectors that fatally crashed `painterResource`).
- **Database Logic:** Solved complex database logic errors, specifically bypassing a strict `UNIQUE` SQL constraint in Supabase via backend string manipulation to allow users to register multiple territory claims without schema modifications.
- **UI/UX Overhaul:** Refactored the `ArenaScreen` UI layout: Replaced overlapping elements, ensured health bars rendered flawlessly above sprites, removed redundant logs, and organized move sets to prevent layout clipping in constrained Landscape mode orientations.
- **Combat Mechanics:** Overhauled combat mathematics. Introduced randomized dynamic base stats for AI enemies and implemented defense-based damage mitigation math to drastically increase gameplay diversity and tactical depth.

### What the Human Developer Handled
- Defined the overarching application vision, aesthetic (neon cyberpunk), and core feature scope.
- Handled Android environment setup, provided the custom Zoodex logo assets, and managed the Supabase project instantiation and SQL schema execution.
- Directed QA testing, reproducing map tracking bugs and combat logic errors for the AI agent to investigate and resolve.

## Features Implemented in the Application
- ✅ **Dynamic GPS Territory Claiming** via MapLibre and Supabase tracking algorithms.
- ✅ **Turn-based Combat Arena** with elemental beasts, dynamic stat scaling, and interactive HP bars.
- ✅ **Live Social Hub** supporting unique Operative friend requests and direct messaging.
- ✅ **Persistent Inventory** containing leveling mechanics, beast rosters, and team building mechanics.

## Major Technical Decisions
- **Jetpack Compose Native Rendering:** Chosen over heavy game engines like Unity or Godot to allow seamless and lightweight integration between standard UI (chats/roster) and the Map/Arena without massive bundle overhead.
- **MapLibre over Google Maps:** MapLibre was chosen for its unparalleled ability to deeply customize map rendering styles (Stamen Toner/Cyberpunk look) which Google Maps restricts natively.

## Challenges Solved During Development
- **Compose Layout Constraints:** The Arena screen forces landscape mode, severely limiting vertical height. Complex UI elements were overlapping and getting cropped. This was meticulously solved by utilizing dynamic `weight(1f)` layouts and carefully anchoring components (`Alignment.CenterStart`).
- **Supabase Unique Constraints:** Saving multiple claims originally triggered 400 Bad Request errors due to a hardcoded `requester_callsign`. This was bypassed by generating unique substring identifiers per territory and utilizing PostgREST wildcard `like` queries to retrieve them without needing to restructure the entire remote database.

## Pending/Improvement Tasks
- Implement offline-first caching for Supabase messages using Android Room.
- Add sound effect triggers and particle animations in the Arena via Compose graphics layer manipulations.
- Introduce dynamic weather systems based on real-world locations to boost specific elemental beasts during combat.

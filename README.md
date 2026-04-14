# Extended Community API

Higher-level Kotlin APIs for the [TribotRS Automation SDK](https://github.com/TribotRS/automation-sdk).

> **Kotlin only** — this library is written in and designed for Kotlin. Java interop is not a goal.

## Installation

Add the JitPack repository and dependency to your script's `build.gradle.kts`:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.SkrrtNick:Extended-Community-Api:latest.release")
}
```

> To pin a specific version, replace `latest.release` with a release tag (e.g. `v1.0.0`).

## Features

### Query Builders
Fluent query builders for finding game entities. Reusable builders with filter chaining and a [`QueryResults`](src/main/kotlin/org/tribot/api/query/QueryResults.kt) wrapper.

- [**NPC**](src/main/kotlin/org/tribot/api/query/NpcQueryBuilder.kt), [**Object**](src/main/kotlin/org/tribot/api/query/ObjectQueryBuilder.kt), [**GroundItem**](src/main/kotlin/org/tribot/api/query/GroundItemQueryBuilder.kt), [**Player**](src/main/kotlin/org/tribot/api/query/PlayerQueryBuilder.kt) — distance, name, ID, action, animation, combat state filters + `nearest()`, `sortByDistance()`
- [**Inventory**](src/main/kotlin/org/tribot/api/query/InventoryQueryBuilder.kt), [**Bank**](src/main/kotlin/org/tribot/api/query/BankQueryBuilder.kt), [**Equipment**](src/main/kotlin/org/tribot/api/query/EquipmentQueryBuilder.kt) — name, ID, action, quantity filters
- [**Widget**](src/main/kotlin/org/tribot/api/query/WidgetQueryBuilder.kt) — text, actions, sprite, item ID, visibility, child traversal
- [**World**](src/main/kotlin/org/tribot/api/query/WorldQueryBuilder.kt) — members, PvP, population, region, activity filters
- [**Graphic Object**](src/main/kotlin/org/tribot/api/query/GraphicObjectQueryBuilder.kt) — ID filters for spot animations

### Requirements System
Composable game-state conditions with AND/OR/NOR/NAND/XOR logic via [`Requirement`](src/main/kotlin/org/tribot/api/requirements/Requirement.kt) and [`CompositeRequirement`](src/main/kotlin/org/tribot/api/requirements/CompositeRequirement.kt).

- [`ItemRequirement`](src/main/kotlin/org/tribot/api/requirements/ItemRequirement.kt), [`EquipmentRequirement`](src/main/kotlin/org/tribot/api/requirements/EquipmentRequirement.kt), [`SkillRequirement`](src/main/kotlin/org/tribot/api/requirements/SkillRequirement.kt), [`QuestRequirement`](src/main/kotlin/org/tribot/api/requirements/QuestRequirement.kt), [`CombatLevelRequirement`](src/main/kotlin/org/tribot/api/requirements/CombatLevelRequirement.kt), [`PrayerRequirement`](src/main/kotlin/org/tribot/api/requirements/PrayerRequirement.kt), [`FreeSlotRequirement`](src/main/kotlin/org/tribot/api/requirements/FreeSlotRequirement.kt)
- [`VarbitRequirement`](src/main/kotlin/org/tribot/api/requirements/VarbitRequirement.kt), [`SettingRequirement`](src/main/kotlin/org/tribot/api/requirements/SettingRequirement.kt), [`VarClientRequirement`](src/main/kotlin/org/tribot/api/requirements/VarClientRequirement.kt), [`ZoneRequirement`](src/main/kotlin/org/tribot/api/requirements/ZoneRequirement.kt), [`WidgetRequirement`](src/main/kotlin/org/tribot/api/requirements/WidgetRequirement.kt)
- `Requirements.all(...)`, `Requirements.any(...)`, `Requirements.not(...)`
- [`EquipmentRequirement`](src/main/kotlin/org/tribot/api/requirements/EquipmentRequirement.kt) integrates with [`ItemDatabase`](src/main/kotlin/org/tribot/api/data/ItemDatabase.kt) to auto-resolve skill requirements:
  ```kotlin
  val whipReq = EquipmentRequirement(itemId = 4151, slot = EquipmentSlot.WEAPON)
  whipReq.skillRequirements  // [SkillRequirement(ATTACK, 70)] — resolved from ItemDatabase
  whipReq.canEquip()         // true if player has 70 Attack
  ```

### Loadout System
Define desired inventory + equipment, auto-restock from bank in one call via [`LoadoutManager`](src/main/kotlin/org/tribot/api/loadout/LoadoutManager.kt).

- `LoadoutManager.fulfill(ctx, loadout)` — open bank, deposit unwanted, withdraw missing, equip gear
- Supports alternate item IDs (e.g. any tier of axe) via [`LoadoutItem`](src/main/kotlin/org/tribot/api/loadout/Loadout.kt)

### Grand Exchange
Offer management, price lookups, and GE interaction via [`GrandExchange`](src/main/kotlin/org/tribot/api/ge/GrandExchange.kt).

- `GrandExchange.open()`, `.close()`, `.getOffers()`, `.getCompletedOffers()`
- [`PriceLookup`](src/main/kotlin/org/tribot/api/ge/PriceLookup.kt)`.getPrice(itemId)` — bulk-fetched from OSRS Wiki API, file-cached across clients with 5min TTL

### Magic / Spells
Typed [`Spell`](src/main/kotlin/org/tribot/api/magic/Spell.kt) enum with all 4 spellbooks (~140 spells), rune requirements, and staff support.

- [`SpellHelper`](src/main/kotlin/org/tribot/api/magic/SpellHelper.kt)`.canCast(ctx, spell)` — checks spellbook, level, runes (with staff/tome/combo rune support)
- [`Staff`](src/main/kotlin/org/tribot/api/magic/Staff.kt) enum — all elemental/battle/mystic staves, tomes, Kodai wand, Twinflame staff
- [`RuneType`](src/main/kotlin/org/tribot/api/magic/RuneType.kt) — combo rune substitution (smoke, mist, dust, mud, lava, steam, sunfire, aether)

### Food / Consumables
Database of food healing values and potion boost formulas, validated against the OSRS Wiki.

- [`ConsumableDatabase`](src/main/kotlin/org/tribot/api/consumable/ConsumableDatabase.kt)`.get(itemId)` — heal amounts, stat boosts/drains, special effects
- [`ConsumableHelper`](src/main/kotlin/org/tribot/api/consumable/ConsumableHelper.kt)`.eat(ctx, itemId)`, `.drink(ctx, itemId)` — with wait-for-effect
- Covers food, potions, brews, pies, combo food, and special items (Anglerfish, Sara brew, Super restore)

### Event Dispatcher
Derived RuneLite-equivalent events by polling game state via [`EventDispatcher`](src/main/kotlin/org/tribot/api/events/EventDispatcher.kt) — no SDK modifications needed.

**Tick-rate (~600ms):**
StatChanged, InventoryChanged, EquipmentChanged, NpcSpawned/Despawned, PlayerSpawned/Despawned, ObjectSpawned/Despawned, GroundItemSpawned/Despawned, VarbitChanged, SettingChanged, VarClientChanged, GEOfferChanged, WidgetOpened/Closed

**Frame-rate (~20ms):**
AnimationChanged, InteractingChanged, HealthChanged

### Conditional Waiting
[`Conditions`](src/main/kotlin/org/tribot/api/waiting/Conditions.kt) — `waitUntil(timeout) { condition }` with humanized polling via player profiles.

### Player Profile / Player Sense
Per-account behavioral profiles for consistent, human-like variance. Seed-based generation with overridable properties via [`PlayerProfile`](src/main/kotlin/org/tribot/api/preferences/PlayerProfile.kt).

### Bank Cache
In-memory snapshot of bank contents via [`BankCache`](src/main/kotlin/org/tribot/api/banking/BankCache.kt), auto-updated on game tick when bank is open. Query bank without having it open.

## Roadmap

- Dialogue / chat interaction helpers
- Shop interaction API
- Trade interaction API
- Player-owned house support
- Slayer task tracking
- Rune pouch support in canCast
- Persistent bank cache (file-backed)
- Prayer flicking helpers
- Combat helper (eat/pray/spec management)
- Pathfinding integration helpers

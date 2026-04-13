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
Fluent query builders for finding game entities. Reusable builders with filter chaining and a `QueryResults` wrapper.

- **NPC, Object, GroundItem, Player** — distance, name, ID, action, animation, combat state filters + `nearest()`, `sortByDistance()`
- **Inventory, Bank, Equipment** — name, ID, action, quantity filters
- **Widget** — text, actions, sprite, item ID, visibility, child traversal
- **World** — members, PvP, population, region, activity filters
- **Graphic Object** — ID filters for spot animations

### Requirements System
Composable game-state conditions with AND/OR/NOR/NAND/XOR logic.

- Item, Equipment, Skill, Quest, Combat Level, Prayer, Free Slots
- Varbit, Setting (varp), VarClient, Zone, Widget
- `Requirements.all(...)`, `Requirements.any(...)`, `Requirements.not(...)`

### Loadout System
Define desired inventory + equipment, auto-restock from bank in one call.

- `LoadoutManager.fulfill(ctx, loadout)` — open bank, deposit unwanted, withdraw missing, equip gear
- Supports alternate item IDs (e.g. any tier of axe)

### Grand Exchange
Offer management, price lookups, and GE interaction.

- `GrandExchange.open()`, `.close()`, `.getOffers()`, `.getCompletedOffers()`
- `PriceLookup.getPrice(itemId)` — bulk-fetched from OSRS Wiki API, file-cached across clients with 5min TTL

### Magic / Spells
Typed spell enum with all 4 spellbooks (~140 spells), rune requirements, and staff support.

- `SpellHelper.canCast(ctx, spell)` — checks spellbook, level, runes (with staff/tome/combo rune support)
- `Staff` enum — all elemental/battle/mystic staves, tomes, Kodai wand, Twinflame staff
- `RuneType` — combo rune substitution (smoke, mist, dust, mud, lava, steam, sunfire, aether)

### Food / Consumables
Database of food healing values and potion boost formulas, validated against the OSRS Wiki.

- `ConsumableDatabase.get(itemId)` — heal amounts, stat boosts/drains, special effects
- `ConsumableHelper.eat(ctx, itemId)`, `.drink(ctx, itemId)` — with wait-for-effect
- Covers food, potions, brews, pies, combo food, and special items (Anglerfish, Sara brew, Super restore)

### Event Dispatcher
Derived RuneLite-equivalent events by polling game state — no SDK modifications needed.

**Tick-rate (~600ms):**
StatChanged, InventoryChanged, EquipmentChanged, NpcSpawned/Despawned, PlayerSpawned/Despawned, ObjectSpawned/Despawned, GroundItemSpawned/Despawned, VarbitChanged, SettingChanged, VarClientChanged, GEOfferChanged, WidgetOpened/Closed

**Frame-rate (~20ms):**
AnimationChanged, InteractingChanged, HealthChanged

### Conditional Waiting
`waitUntil(timeout) { condition }` with humanized polling via player profiles.

### Player Profile / Player Sense
Per-account behavioral profiles for consistent, human-like variance. Seed-based generation with overridable properties.

### Bank Cache
In-memory snapshot of bank contents, auto-updated on game tick when bank is open. Query bank without having it open.

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

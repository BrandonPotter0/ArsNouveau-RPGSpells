# Ars Nouveau Mana for RPG Spells

A NeoForge compatibility mod for Minecraft 1.21.1 that replaces RPG Series rune-item spell costs with Ars Nouveau mana.

## Target environment

- Minecraft: 1.21.1
- NeoForge: 21.1.264 or newer in the 21.1.x line
- Java: 21
- Ars Nouveau: 5.13.5 or newer
- Spell Engine: 1.10.9+1.21.1 (NeoForge)
- Author: Brandon Potter

## Cost model

The initial balance target is **100 mana per original rune**, scaled by the original rune amount and spell tier:

`manaCost = 100 × runeAmount × (spellTier + 1) × manaMultiplier`

The default multiplier is `1.0` and is configurable.

| Spell tier | Default mana cost |
|---:|---:|
| 0 | 100 |
| 1 | 200 |
| 2 | 300 |
| 3 | 400 |
| 4 | 500 |

Cooldowns, cast times, and non-rune costs should remain controlled by Spell Engine.

## Compatibility goals

- Ars Nouveau mana is the only supported spell resource; there is no rune fallback.
- Ars Nouveau is a required dependency.
- Support Wizards and Elemental Wizards RPG Series spell content without editing those upstream mods.
- Charge mana at the correct successful-cast cost stage and avoid duplicate charges for batched or channelled casts.
- Preserve existing cooldown, exhaustion, durability, and status-effect costs.

## Implementation

The first compatibility pass is now in the source tree:

- A pre-cast Spell Engine event checks that the player has enough Ars Nouveau mana.
- The check leaves Spell Engine's native cooldown handling intact.
- An `Ammo` mixin bypasses `runes:` item requirements and prevents rune consumption.
- Spell Engine's successful-cost event removes mana only after successful delivery completion.
- A client tooltip mixin displays the calculated mana cost and the player's available mana.
- Creative players are exempt from mana payment.
- Mana costs are server-configurable through `config/arsnouveau_rpgspells-server.toml`.

The integration is **not yet build- or in-game-verified**. The next milestone is to compile against the pinned NeoForge, Ars Nouveau, and Spell Engine artifacts, fix any mapping/API differences, then test instant, projectile, channeled, and failed-delivery casts on both client and dedicated server.

## Build

Open the project in IntelliJ IDEA with JDK 21, then run:

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

The first implementation milestone will be a minimal loadable mod with validated dependency metadata and configuration. The next milestone will implement and test mana preflight, consumption, and rune-cost suppression.

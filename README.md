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

The initial balance target is **100 mana per original rune requirement**, scaled by spell tier:

`manaCost = 100 × (spellTier + 1) × manaMultiplier`

The default multiplier is `1.0`. The multiplier will be configurable.

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

## Development status

This repository is at the initial project-scaffolding stage. The exact Spell Engine interception point and Ars Nouveau mana capability calls still need to be verified against the selected dependency artifacts before the compatibility implementation is added.

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

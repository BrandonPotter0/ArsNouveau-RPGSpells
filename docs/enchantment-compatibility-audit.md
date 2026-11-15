# Enchantment and perk compatibility audit

Target environment: Minecraft 1.21.1, NeoForge 21.1.x, Ars Nouveau 5.13.5, Spell Engine 1.10.9, Spell Power Attributes 1.6.0, Wizards RPG Series 3.1.3, Elemental Wizards 3.2.2, MRPG Lib 2.8.1.

This document records source-level findings from the exact JARs supplied for this project. It is an investigation baseline, not a claim that every interaction has been tested in-game.

## Confirmed automatic Spell Power integration

Spell Engine 1.10.9 resolves spell impact power through `SpellExecution.Power.resolve`. It chooses the impact school (or spell school when the impact has no override) and calls `SpellPower.getSpellPower(school, caster)`. That API reads the matching Spell Power school attribute, generic Spell Power attribute, critical chance, and critical damage.

As a result, the following attribute-based enchantments should already affect RPG spell impacts when the spell's school matches. Do not add duplicate modifiers for these:

| Enchantment | Attribute bonuses per level | Source |
|---|---|---|
| Spell Power | +5% generic | Spell Power Attributes |
| Sunfire | +3% Arcane and Fire | Spell Power Attributes |
| Soulfrost | +3% Soul and Frost | Spell Power Attributes |
| Energize | +3% Healing and Lightning | Spell Power Attributes |
| Stonebloom | +3% Earth and Nature | MRPG Lib |
| Typhoon | +3% Air and Water | MRPG Lib |

Stonebloom and Typhoon are armor enchantments, max level 5, and belong to Spell Power's `multi_school` exclusive set. Their definitions are conditional on Spell Power being loaded.

Spell Power's Critical Chance and Critical Damage enchantments modify Spell Power attributes consumed by `SpellPower.getSpellPower`; whether those critical values produce an RPG spell critical depends on the spell's impact configuration and Spell Engine's critical handling. This needs in-game verification before treating the enchantments as universally effective.

Spell Haste modifies the Spell Power Haste attribute. Spell Engine's Spell Power impact resolution does not itself apply casting haste; inspect the cast-duration path separately before adding a bridge.

## Ars Nouveau 5.13.5 thread/perk findings

Ars Nouveau stores armor threads/perks in the `ARMOR_PERKS` data component. `PerkUtil.getPerksFromLiving` scans the wearer's armor slots, and `PerkUtil.countForPerk` returns the highest matching perk-slot value found across equipped armor. This is the supported retrieval path; compatibility code should not parse item lore or NBT manually.

Confirmed perk mechanics from the supplied JAR:

| Thread | Existing Ars mechanic | RPG spell compatibility candidate |
|---|---|---|
| Spell Power | Adds +2 per perk level to Ars Nouveau's `SPELL_DAMAGE_BONUS` attribute | Translate into RPG damage power only if the exact additive/multiplier semantics can be preserved |
| Life Drain | Heals caster for 20% per perk level of spell damage dealt, via Ars `SpellDamageEvent.Post` | Heal caster after successful RPG spell damage |
| Warding | Adds +2 per perk level to Ars `WARDING` attribute | Reduce incoming RPG magic damage by the same flat amount |
| Kindling | Before a damaging Ars effect resolves, sets fire ticks to 100 × perk slot value | Apply burn to valid RPG spell damage impacts |
| Chilling | Before a damaging Ars effect resolves, adds freezing buildup and Freezing effect | Apply equivalent freezing to valid RPG spell damage impacts |
| The Wixie | Adds +15% per perk level to Ars `WIXIE` attribute | Extend RPG-applied potion/status-effect duration if the RPG effect is duration-based |
| Immolation | Conditional fire-related thread behavior; source implementation needs further inspection | Only bridge the confirmed fire-spell bonus and conditional effects once fully traced |

Other threads that primarily modify movement, fall damage, underwater breathing, food saturation, looting, repair, mana capacity/regeneration, or death prevention are not assumed to affect RPG spell impacts.

## Spell Infinity

Spell Engine 1.10.9 registers `spell_engine:spell_infinity` as a mainhand enchantment, max level 1, with an ammo-use effect that sets rune ammo consumption to zero. The upstream enchantment JSON has normal enchanting cost/weight values (weight 1) and is listed in the upstream `minecraft:non_treasure` tag. That tag alone does not establish that it is unavailable from every normal survival acquisition route.

The project requirement is to keep the enchantment registered and command-usable while removing normal survival acquisition. The exact vanilla/NeoForge acquisition hooks still need to be confirmed before implementation. Mana charging must remain active even when Spell Infinity is present.

## Confirmed native defensive compatibility

Two defensive mechanics do not need a custom RPG-spell bridge:

- **Ars Nouveau Warding** already works against RPG spell damage. Ars Nouveau's 1.21.x `EventHandler.entityHurt` subtracts the wearer's `PerkAttributes.WARDING` value from damage tagged `c:is_magic`. Spell Power's `c:is_magic` tag includes the `#spell_power:all` damage types, and Spell Engine creates RPG spell damage sources using the relevant Spell Power school damage type. Warding is therefore already applied by Ars Nouveau's own event handler.
- **Spell Power Magic Protection** already applies to RPG spell damage. Its enchantment effect checks `c:is_magic` and excludes damage bypassing invulnerability. RPG spell school damage types are included in `c:is_magic`, so a second custom reduction would double-dip.

These findings are based on the exact Ars Nouveau 1.21.x source, Spell Engine 1.21.1 source, and Spell Power 1.6.0 JAR resources. No extra compatibility modifier should be added for either feature.

## Spell Infinity survival-acquisition restriction

The compatibility branch now removes `spell_engine:spell_infinity` from the vanilla enchantment tags used for:
- Enchanting-table availability (`minecraft:in_enchanting_table`)
- Villager trading (`minecraft:tradeable`)
- Random loot (`minecraft:on_random_loot`)
- Mob equipment generation (`minecraft:on_mob_spawn_equipment`)

The enchantment itself remains registered and its supported-item definition is untouched. This intentionally preserves command-based application and use. The existing mana-cost checks remain independent of rune-ammo consumption, so Spell Infinity must not make rune-based RPG spells free of Ars Nouveau mana.

## Next implementation/research steps

1. Verify Spell Engine's critical strike and cast-duration paths for Spell Power critical and haste attributes.
2. Trace Ars Elemental thread perk implementations and their data-component retrieval.
3. Inspect Spell Engine damage-source tagging and damage application to bridge Warding/Magic Protection safely.
4. Implement the smallest safe compatibility slice, then build and test in the supplied 1.21.1 NeoForge environment.
5. Handle Spell Infinity acquisition separately, preserving registry and command application.

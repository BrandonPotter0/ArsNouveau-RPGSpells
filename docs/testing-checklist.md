# Initial in-game testing checklist

## Test environment

- Minecraft 1.21.1
- NeoForge 21.1.264 or a later 21.1.x build
- Java 21
- Ars Nouveau 5.13.5 or newer
- Spell Engine 1.10.9 for NeoForge
- Wizards RPG Series
- Elemental Wizards RPG Series

## Mana costs

- [ ] Tier 0 rune-cost spell displays and costs 30 mana at multiplier 1.0.
- [ ] Tier 1 spell costs 60 mana.
- [ ] Higher tiers scale linearly by 30 mana per tier.
- [ ] A spell with an original rune amount greater than one scales by that amount.
- [ ] Changing `baseManaPerRune` changes the base cost.
- [ ] Changing `manaMultiplier` scales the result; a fractional result rounds up.

## Casting behavior

- [ ] A player with sufficient mana can cast without carrying the original rune.
- [ ] The original rune is not consumed if present in inventory.
- [ ] A player without sufficient mana cannot start the spell and sees the mana warning.
- [ ] Native cooldown checks and cooldown feedback still work.
- [ ] Cast time and charge behavior are unchanged.
- [ ] Creative players can cast without mana payment.
- [ ] A projectile that fails to complete delivery does not charge mana.
- [ ] A successful projectile delivery charges mana once.
- [ ] A channeled spell charges once on release, not once per channel tick.
- [ ] Passive triggered spells do not charge repeatedly from a single completion.

## UI and environment

- [ ] Spell tooltips show mana cost and current mana instead of the rune item requirement.
- [ ] Client and dedicated server both load without mixin errors.
- [ ] The mod refuses to load when Ars Nouveau is absent.
- [ ] The mod works with Wizards installed.
- [ ] The mod works with Elemental Wizards installed.
- [ ] The mod still loads when neither optional RPG content mod is installed.

## Known validation boundary

The first implementation pass has not yet been compiled or run in Minecraft. Mark each item only after testing with the actual dependency jars and keep crash logs for any failed checks.


## Optional Ars Elemental Focus compatibility

Test with Ars Elemental 0.7.10.4 installed. Then repeat the base-mod loading checks without Ars Elemental.

- [ ] Without Ars Elemental installed, the mod loads normally and the mana integration still works.
- [ ] With Ars Elemental installed, a Lesser Fire Focus boosts Fire RPG spell damage, healing, and status-effect strength.
- [ ] A Lesser Fire Focus dampens Water, Frost, Air, and Earth RPG elemental effects.
- [ ] A Greater Fire Focus boosts matching Fire spells without dampening other elemental schools.
- [ ] Lesser and Greater Water Focuses both treat Water and Frost RPG schools as matching.
- [ ] Lesser and Greater Air Focuses match Air RPG spells.
- [ ] Lesser and Greater Earth Focuses match Earth RPG spells, including Terra summon spells.
- [ ] A Necrotic Focus boosts Arcane RPG damage spells and Arcane summon impacts.
- [ ] Focus changes take effect immediately after equipping or removing the Curio.
- [ ] Multiple Focuses do not cause crashes or corrupt Spell Engine's cached modifiers.
- [ ] Client and dedicated server both load with Ars Elemental present.
- [ ] No changes are required to Wizards or Elemental Wizards JARs.

package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.mojang.logging.LogUtils;
import net.minecraft.world.damagesource.DamageTypes;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;
import org.slf4j.Logger;

/**
 * Bridges Spell Power Attributes into Ars Nouveau spell damage.
 *
 * RPG spell damage is handled at Spell Engine's impact call site by
 * SpellPowerImpactMixin, where the caster and raw damage amount are both available.
 */
public final class SpellPowerCompatibility {
    private static final Logger LOGGER = LogUtils.getLogger();

    private SpellPowerCompatibility() {
    }

    /**
     * Maps Ars Nouveau's elemental schools to their matching Spell Power
     * schools. This allows school-specific attributes and enchantments,
     * including Typhoon's Air and Water bonuses, to affect Ars Nouveau damage.
     *
     * SpellContext advances its current index immediately before resolving each
     * spell part, so currentIndex - 1 identifies the part currently producing
     * the damage event.
     */
    public static void onArsSpellDamage(SpellDamageEvent.Pre event) {
        if (event.caster.level().isClientSide
                || event.damage <= 0.0F
                // Ars Elemental's Spike uses stalagmite damage and already supplies
                // its own configured damage value. It must not receive generic
                // Ars Nouveau Spell Power scaling from this compatibility listener.
                || event.damageSource.is(DamageTypes.STALAGMITE)) {
            return;
        }

        double baseline = SpellSchools.GENERIC.attributeBaseValue();
        if (baseline <= 0.0D) {
            return;
        }

        net.spell_power.api.SpellSchool powerSchool = SpellSchools.GENERIC;
        AbstractSpellPart activePart = null;
        int currentIndex = event.context.getCurrentIndex();
        int activePartIndex = currentIndex - 1;

        if (activePartIndex >= 0 && activePartIndex < event.context.getSpell().size()) {
            activePart = event.context.getSpell().get(activePartIndex);
            var arsSchools = activePart.spellSchools;

            boolean hasElementalSchool =
                    arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_AIR)
                    || arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_EARTH)
                    || arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE)
                    || arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER);

            boolean hasNecromancySchool =
                    arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.NECROMANCY);

            boolean hasArcaneSchool =
                    arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ABJURATION)
                    || arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.CONJURATION)
                    || arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION);

            if (arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE)) {
                powerSchool = SpellSchools.FIRE;
            } else if (arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_AIR)) {
                // Air and Water are registered by More RPG Library, not Spell Power itself.
                // Resolve them by ID so this mod does not need a compile-time MRPG dependency.
                var airSchool = SpellSchools.getSchool("spell_power:air");
                if (airSchool != null) {
                    powerSchool = airSchool;
                }
            } else if (arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER)) {
                var waterSchool = SpellSchools.getSchool("spell_power:water");
                if (waterSchool != null) {
                    powerSchool = waterSchool;
                }
            } else if (!hasElementalSchool && hasNecromancySchool) {
                powerSchool = SpellSchools.SOUL;
            } else if (!hasElementalSchool && hasArcaneSchool) {
                powerSchool = SpellSchools.ARCANE;
            }
        }

        double spellPower = SpellPower.getSpellPower(powerSchool, event.caster).baseValue();
        double multiplier = spellPower / baseline;
        float damageBefore = event.damage;

        // Temporary focused diagnostics for Wind Shear. These report the exact
        // glyph school, selected Spell Power school, attribute values, and damage
        // multiplier so we can identify why its damage changes with this mod.
        if (activePart != null && activePart.getClass().getSimpleName().toLowerCase().contains("windshear")) {
            LOGGER.info(
                    "[Ars Nouveau RPG Spells][Wind Shear Diagnostic] glyph={}, arsSchools={}, damageType={}, selectedSchool={}, schoolPower={}, genericBaseline={}, multiplier={}, damageBefore={}, damageAfter={}",
                    activePart.getClass().getSimpleName(),
                    activePart.spellSchools,
                    event.damageSource.typeHolder().unwrapKey().map(key -> key.location().toString()).orElse("unknown"),
                    powerSchool.id,
                    spellPower,
                    baseline,
                    multiplier,
                    damageBefore,
                    Double.isFinite(multiplier) && multiplier > 0.0D ? damageBefore * multiplier : damageBefore
            );
        }

        if (Double.isFinite(multiplier) && multiplier > 0.0D) {
            event.damage = (float) (event.damage * multiplier);
        }
    }
}
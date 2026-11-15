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
 */
public final class SpellPowerCompatibility {
    private static final Logger LOGGER = LogUtils.getLogger();

    private SpellPowerCompatibility() {
    }

    /**
     * Maps Ars Nouveau's elemental schools to their matching Spell Power
     * schools, allowing school-specific attributes and enchantments to affect
     * Ars Nouveau damage.
     *
     * SpellContext advances its current index immediately before resolving each
     * spell part, so currentIndex - 1 identifies the part currently producing
     * the damage event.
     */
    public static void onArsSpellDamage(SpellDamageEvent.Pre event) {
        if (event.caster.level().isClientSide
                || event.damage <= 0.0F
                || event.damageSource.is(DamageTypes.STALAGMITE)) {
            return;
        }

        net.spell_power.api.SpellSchool powerSchool = SpellSchools.GENERIC;
        AbstractSpellPart activePart = null;
        int activePartIndex = event.context.getCurrentIndex() - 1;

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

        // Each Spell Power school can use a different attribute scale.
        // Compare against the selected school's own default, not Generic's
        // default (100), otherwise Air/Water values near 1 become ~1% damage.
        double baseline = powerSchool.attributeBaseValue();
        if (baseline <= 0.0D) {
            return;
        }

        double spellPower = SpellPower.getSpellPower(powerSchool, event.caster).baseValue();
        double multiplier = spellPower / baseline;
        float damageBefore = event.damage;
        double damageAfter = Double.isFinite(multiplier) && multiplier > 0.0D
                ? damageBefore * multiplier
                : damageBefore;

        // Temporary focused diagnostics for Wind Shear; remove after validation.
        if (activePart != null && activePart.getClass().getSimpleName().toLowerCase().contains("windshear")) {
            LOGGER.info(
                    "[Ars Nouveau RPG Spells][Wind Shear Diagnostic] glyph={}, damageType={}, selectedSchool={}, schoolPower={}, schoolBaseline={}, multiplier={}, damageBefore={}, damageAfter={}",
                    activePart.getClass().getSimpleName(),
                    event.damageSource.typeHolder().unwrapKey().map(key -> key.location().toString()).orElse("unknown"),
                    powerSchool.id,
                    spellPower,
                    baseline,
                    multiplier,
                    damageBefore,
                    damageAfter
            );
        }

        if (Double.isFinite(multiplier) && multiplier > 0.0D) {
            event.damage = (float) damageAfter;
        }
    }
}
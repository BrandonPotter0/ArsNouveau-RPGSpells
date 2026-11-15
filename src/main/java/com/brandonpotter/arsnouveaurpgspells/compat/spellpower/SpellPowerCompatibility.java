package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import net.minecraft.world.damagesource.DamageTypes;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges Spell Power Attributes into Ars Nouveau spell damage.
 */
public final class SpellPowerCompatibility {
    private static final Logger LOGGER = LoggerFactory.getLogger("arsnouveau_rpgspells/SpellPowerCompatibility");

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

        float damageBeforeSpellPower = event.damage;
        double spellDamageBonus = event.caster.getAttributes().hasAttribute(PerkAttributes.SPELL_DAMAGE_BONUS)
                ? event.caster.getAttributeValue(PerkAttributes.SPELL_DAMAGE_BONUS)
                : 0.0D;

        net.spell_power.api.SpellSchool powerSchool = SpellSchools.GENERIC;
        int activePartIndex = event.context.getCurrentIndex() - 1;

        if (activePartIndex >= 0 && activePartIndex < event.context.getSpell().size()) {
            AbstractSpellPart activePart = event.context.getSpell().get(activePartIndex);
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

        if (Double.isFinite(multiplier) && multiplier > 0.0D) {
            LOGGER.info(
                    "Ars spell damage diagnostic: school={}, damageBeforeSpellPower={}, spellPower={}, baseline={}, multiplier={}, spellDamageBonus={}, damageAfterSpellPower={}",
                    powerSchool.id,
                    damageBeforeSpellPower,
                    spellPower,
                    baseline,
                    multiplier,
                    spellDamageBonus,
                    damageBeforeSpellPower * multiplier
            );
            event.damage = (float) (event.damage * multiplier);
        }
    }
}
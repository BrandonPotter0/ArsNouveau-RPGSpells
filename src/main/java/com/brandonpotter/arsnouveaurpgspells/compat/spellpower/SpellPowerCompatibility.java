package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;

/**
 * Bridges Spell Power Attributes into Ars Nouveau spell damage.
 *
 * RPG spell damage is handled at Spell Engine's impact call site by
 * SpellPowerImpactMixin, where the caster and raw damage amount are both available.
 */
public final class SpellPowerCompatibility {
    private SpellPowerCompatibility() {
    }

    /**
     * Applies generic Spell Power to Ars Nouveau elemental damage when no
     * matching school bridge exists yet. Non-elemental Ars Nouveau glyphs use
     * Arcane as their neutral Spell Power school. Elemental schools take
     * priority when a glyph belongs to both an elemental and non-elemental
     * Ars Nouveau school.
     *
     * SpellContext advances its current index immediately before resolving each
     * spell part, so currentIndex - 1 identifies the part currently producing
     * the damage event.
     */
    public static void onArsSpellDamage(SpellDamageEvent.Pre event) {
        if (event.caster.level().isClientSide || event.damage <= 0.0F) {
            return;
        }

        double baseline = SpellSchools.GENERIC.attributeBaseValue();
        if (baseline <= 0.0D) {
            return;
        }

        net.spell_power.api.SpellSchool powerSchool = SpellSchools.GENERIC;
        int currentIndex = event.context.getCurrentIndex();
        int activePartIndex = currentIndex - 1;

        if (activePartIndex >= 0 && activePartIndex < event.context.getSpell().size()) {
            AbstractSpellPart activePart = event.context.getSpell().get(activePartIndex);
            var arsSchools = activePart.spellSchools;

            boolean hasElementalSchool =
                    arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_AIR)
                    || arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_EARTH)
                    || arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE)
                    || arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER);

            if (arsSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE)) {
                powerSchool = SpellSchools.FIRE;
            } else if (!hasElementalSchool) {
                powerSchool = SpellSchools.ARCANE;
            }
        }

        double spellPower = SpellPower.getSpellPower(powerSchool, event.caster).baseValue();
        double multiplier = spellPower / baseline;
        if (Double.isFinite(multiplier) && multiplier > 0.0D) {
            event.damage = (float) (event.damage * multiplier);
        }
    }
}

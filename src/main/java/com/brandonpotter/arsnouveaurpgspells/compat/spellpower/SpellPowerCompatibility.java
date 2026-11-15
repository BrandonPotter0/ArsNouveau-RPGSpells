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
     * Applies generic Spell Power to all Ars Nouveau spell damage and the Fire
     * school value to damage caused by an Ars Nouveau Fire-school glyph.
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
            if (activePart.spellSchools.contains(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE)) {
                powerSchool = SpellSchools.FIRE;
            }
        }

        double spellPower = SpellPower.getSpellPower(powerSchool, event.caster).baseValue();
        double multiplier = spellPower / baseline;
        if (Double.isFinite(multiplier) && multiplier > 0.0D) {
            event.damage = (float) (event.damage * multiplier);
        }
    }
}

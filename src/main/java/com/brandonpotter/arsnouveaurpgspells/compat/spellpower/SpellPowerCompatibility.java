package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
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
     * Applies the generic Spell Power attribute as a multiplier to Ars Nouveau spell damage.
     */
    public static void onArsSpellDamage(SpellDamageEvent.Pre event) {
        if (event.caster.level().isClientSide || event.damage <= 0.0F) {
            return;
        }

        double baseline = SpellSchools.GENERIC.attributeBaseValue();
        if (baseline <= 0.0D) {
            return;
        }

        double genericSpellPower = SpellPower
                .getSpellPower(SpellSchools.GENERIC, event.caster)
                .baseValue();

        double multiplier = genericSpellPower / baseline;
        if (Double.isFinite(multiplier) && multiplier > 0.0D) {
            event.damage = (float) (event.damage * multiplier);
        }
    }
}

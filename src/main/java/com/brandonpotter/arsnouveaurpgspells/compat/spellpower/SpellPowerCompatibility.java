package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;

/**
 * Bridges Spell Power Attributes into Ars Nouveau spell damage.
 *
 * The generic Spell Power attribute uses a percentage baseline (normally 100).
 * Scaling the final Ars spell damage by the generic attribute ratio lets the
 * Spell Power enchantment improve Ars spells without changing Ars' own
 * Spell Damage thread or applying Spell Power critical strikes a second time.
 */
public final class SpellPowerCompatibility {
    private SpellPowerCompatibility() {
    }

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

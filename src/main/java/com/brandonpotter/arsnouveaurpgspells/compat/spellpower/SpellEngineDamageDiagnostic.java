package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temporary diagnostics for Spell Engine damage (including Wizards RPG spells).
 * This listener observes damage only; it does not change the damage amount.
 */
public final class SpellEngineDamageDiagnostic {
    private static final Logger LOGGER = LoggerFactory.getLogger("arsnouveau_rpgspells/SpellEngineDamageDiagnostic");

    private SpellEngineDamageDiagnostic() {
    }

    public static void onDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        var source = event.getSource();

        if (target.level().isClientSide) {
            return;
        }

        var damageTypeKey = source.typeHolder().unwrapKey().orElse(null);
        if (damageTypeKey == null
                || !damageTypeKey.location().getNamespace().equals("spell_power")) {
            return;
        }

        if (!(source.getEntity() instanceof LivingEntity caster)) {
            return;
        }

        double threadBonus = caster.getAttributes().hasAttribute(PerkAttributes.SPELL_DAMAGE_BONUS)
                ? caster.getAttributeValue(PerkAttributes.SPELL_DAMAGE_BONUS)
                : 0.0D;

        double genericPower = SpellPower.getSpellPower(SpellSchools.GENERIC, caster).baseValue();
        double arcanePower = SpellPower.getSpellPower(SpellSchools.ARCANE, caster).baseValue();
        double firePower = SpellPower.getSpellPower(SpellSchools.FIRE, caster).baseValue();
        double soulPower = SpellPower.getSpellPower(SpellSchools.SOUL, caster).baseValue();

        LOGGER.info(
                "Spell Engine damage diagnostic: damageType={}, caster={}, target={}, originalDamage={}, damageAtPre={}, threadSpellDamageBonus={}, genericSpellPower={}, arcaneSpellPower={}, fireSpellPower={}, soulSpellPower={}",
                damageTypeKey.location(),
                caster.getName().getString(),
                target.getName().getString(),
                event.getOriginalDamage(),
                event.getNewDamage(),
                threadBonus,
                genericPower,
                arcanePower,
                firePower,
                soulPower
        );
    }
}

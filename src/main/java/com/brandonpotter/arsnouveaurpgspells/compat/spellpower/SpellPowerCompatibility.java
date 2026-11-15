package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.perk.SpellDamagePerk;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;

/**
 * Bridges Spell Power Attributes into Ars Nouveau spell damage, and the Ars Nouveau
 * Spell Power armor thread into RPG spell damage.
 */
public final class SpellPowerCompatibility {
    private static final TagKey<DamageType> SPELL_POWER_DAMAGE_TYPES = TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("spell_power", "all")
    );

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

    /**
     * Applies Ars Nouveau's flat Spell Power thread bonus to incoming RPG spell damage.
     *
     * Spell Engine uses Spell Power damage types for RPG spell impacts. The thread's
     * existing perk value is +2 damage per level, so this adds that same flat amount
     * before normal damage mitigation is calculated.
     */
    public static void onRpgSpellDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        var source = event.getSource();
        if (!source.is(SPELL_POWER_DAMAGE_TYPES)) {
            return;
        }

        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        int perkLevel = PerkUtil.countForPerk(SpellDamagePerk.INSTANCE, livingAttacker);
        if (perkLevel <= 0) {
            return;
        }

        event.setAmount(event.getAmount() + (2.0F * perkLevel));
    }
}

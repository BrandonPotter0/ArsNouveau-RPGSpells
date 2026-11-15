package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.perk.ChillingPerk;
import com.hollingsworth.arsnouveau.common.perk.IgnitePerk;
import com.hollingsworth.arsnouveau.common.perk.SpellDamagePerk;
import com.hollingsworth.arsnouveau.common.perk.VampiricPerk;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.internals.impact.SpellImpacts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Applies Ars Nouveau armor-thread bonuses and effects to RPG spell impacts.
 */
@Mixin(value = SpellImpacts.class, remap = false)
public abstract class SpellPowerImpactMixin {

    @Redirect(
            method = "performImpact",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private static boolean arsnouveauRpgSpells$applyDamageThreads(
            Entity target,
            DamageSource source,
            float damage,
            @Local(argsOnly = true, ordinal = 0) LivingEntity caster
    ) {
        if (caster.level().isClientSide) {
            return target.hurt(source, damage);
        }

        if (target != caster) {
            int kindlingLevel = PerkUtil.countForPerk(IgnitePerk.INSTANCE, caster);
            if (kindlingLevel > 0) {
                target.setRemainingFireTicks(20 * 5 * kindlingLevel);
            }

            if (target instanceof LivingEntity livingTarget) {
                int chillingLevel = PerkUtil.countForPerk(ChillingPerk.INSTANCE, caster);
                if (chillingLevel > 0) {
                    livingTarget.setTicksFrozen(livingTarget.getTicksFrozen() + 1);
                    livingTarget.addEffect(new MobEffectInstance(
                            ModPotions.FREEZING_EFFECT,
                            chillingLevel * 10 * 20,
                            chillingLevel >= 3 ? 2 : 1
                    ));
                }
            }
        }

        if (damage <= 0.0F) {
            return target.hurt(source, damage);
        }

        int spellPowerLevel = PerkUtil.countForPerk(SpellDamagePerk.INSTANCE, caster);
        float adjustedDamage = damage + (2.0F * spellPowerLevel);
        boolean successfulHit = target.hurt(source, adjustedDamage);

        if (successfulHit) {
            int lifeDrainLevel = PerkUtil.countForPerk(VampiricPerk.INSTANCE, caster);
            if (lifeDrainLevel > 0) {
                caster.heal(adjustedDamage * 0.2F * lifeDrainLevel);
            }
        }

        return successfulHit;
    }

    @ModifyArg(
            method = "performImpact",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"
            ),
            index = 0
    )
    private static float arsnouveauRpgSpells$addArmorSpellPowerThreadToHealing(
            float healing,
            @Local(argsOnly = true, ordinal = 0) LivingEntity caster
    ) {
        if (caster.level().isClientSide || healing <= 0.0F) {
            return healing;
        }

        int perkLevel = PerkUtil.countForPerk(SpellDamagePerk.INSTANCE, caster);
        if (perkLevel <= 0) {
            return healing;
        }

        return healing + (2.0F * perkLevel);
    }
}

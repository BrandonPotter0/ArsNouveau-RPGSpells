package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.perk.SpellDamagePerk;
import com.hollingsworth.arsnouveau.common.perk.VampiricPerk;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.internals.impact.SpellImpacts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applies Ars Nouveau armor-thread bonuses to RPG spell damage and healing.
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
        if (caster.level().isClientSide || damage <= 0.0F) {
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

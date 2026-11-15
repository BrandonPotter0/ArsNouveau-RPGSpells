package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applies Spell Power's Healing school to Ars Nouveau's direct Heal glyph.
 */
@Mixin(value = EffectHeal.class, remap = false)
public abstract class ArsHealingSpellPowerMixin {

    @ModifyArg(
            method = "onResolveEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"
            ),
            index = 0,
            remap = false
    )
    private float arsnouveauRpgSpells$applyHealingSpellPower(
            float amount,
            @Local(argsOnly = true, ordinal = 0) LivingEntity shooter
    ) {
        if (shooter.level().isClientSide || amount <= 0.0F) {
            return amount;
        }

        double baseline = SpellSchools.GENERIC.attributeBaseValue();
        if (baseline <= 0.0D) {
            return amount;
        }

        double healingPower = SpellPower.getSpellPower(SpellSchools.HEALING, shooter).baseValue();
        double multiplier = healingPower / baseline;
        if (Double.isFinite(multiplier) && multiplier > 0.0D) {
            return (float) (amount * multiplier);
        }

        return amount;
    }
}

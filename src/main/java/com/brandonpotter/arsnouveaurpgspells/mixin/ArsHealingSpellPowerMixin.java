package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
        if (shooter.level().isClientSide || amount <= 0.0F || !(shooter instanceof Player)) {
            return amount;
        }

        double baseline = SpellSchools.HEALING.attributeBaseValue();
        if (baseline <= 0.0D || SpellSchools.HEALING.attributeEntry == null) {
            return amount;
        }

        // Read the fully modified attribute so Energize's multiplied-base
        // modifier is included. Divide by the default to get a multiplier:
        // no bonus = 1.0x; Energize V (+15%) = 1.15x.
        double healingPower = shooter.getAttributeValue(SpellSchools.HEALING.attributeEntry);
        double multiplier = healingPower / baseline;

        if (Double.isFinite(multiplier)) {
            return (float) (amount * Math.max(0.0D, multiplier));
        }

        return amount;
    }
}

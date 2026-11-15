package com.brandonpotter.arsnouveaurpgspells.mixin;

import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Preserves NeoForge's already-recorded damage reductions when Lodestone
 * reapplies its magic attribute calculation during LivingDamageEvent.Pre.
 *
 * Lodestone computes an adjusted damage amount and writes it with
 * setNewDamage(). That setter replaces the current post-mitigation amount,
 * so the reductions already recorded by NeoForge must be subtracted from
 * Lodestone's adjusted amount before it is written back.
 */
@Pseudo
@Mixin(targets = "team.lodestar.lodestone.handlers.LodestoneAttributeEventHandler", remap = false)
public abstract class LodestoneDamageMitigationMixin {

    @Redirect(
            method = "processAttributes(Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;setNewDamage(F)V"
            ),
            remap = false
    )
    private static void arsnouveauRpgSpells$preserveMitigation(
            LivingDamageEvent.Pre event,
            float lodestoneAdjustedDamage
    ) {
        DamageContainer container = event.getContainer();
        float recordedReductions =
                container.getReduction(DamageContainer.Reduction.INVULNERABILITY)
                + container.getReduction(DamageContainer.Reduction.ARMOR)
                + container.getReduction(DamageContainer.Reduction.ENCHANTMENTS)
                + container.getReduction(DamageContainer.Reduction.MOB_EFFECTS);

        float adjustedDamage = Math.max(0.0F, lodestoneAdjustedDamage - recordedReductions);
        event.setNewDamage(adjustedDamage);
    }
}

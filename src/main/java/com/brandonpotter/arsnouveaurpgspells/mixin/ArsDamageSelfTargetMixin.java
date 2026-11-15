package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.hollingsworth.arsnouveau.api.spell.IDamageEffect;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Temporary testing hook: allow Ars Nouveau damage effects to hit their own
 * caster when Self is used. Normal ally protection remains unchanged.
 *
 * Remove this mixin after self-damage testing is complete.
 */
@Mixin(value = IDamageEffect.class, remap = false)
public abstract class ArsDamageSelfTargetMixin {

    @Inject(method = "canDamage", at = @At("HEAD"), cancellable = true, remap = false)
    private void arsnouveauRpgSpells$allowSelfDamage(
            LivingEntity shooter,
            SpellStats stats,
            SpellContext spellContext,
            SpellResolver resolver,
            Entity target,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (target == shooter && shooter.isAlive()) {
            cir.setReturnValue(true);
        }
    }
}

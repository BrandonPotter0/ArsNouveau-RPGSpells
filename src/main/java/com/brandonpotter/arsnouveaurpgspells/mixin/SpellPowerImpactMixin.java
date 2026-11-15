package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.perk.SpellDamagePerk;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.internals.impact.SpellImpacts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applies Ars Nouveau's Spell Power armor thread to the final RPG spell damage
 * amount immediately before Spell Engine passes it to Entity.hurt.
 */
@Mixin(value = SpellImpacts.class, remap = false)
public abstract class SpellPowerImpactMixin {

    @ModifyArg(
            method = "performImpact",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            ),
            index = 1
    )
    private static float arsnouveauRpgSpells$addArmorSpellPowerThread(
            float damage,
            @Local(argsOnly = true, ordinal = 0) LivingEntity caster
    ) {
        if (caster.level().isClientSide || damage <= 0.0F) {
            return damage;
        }

        int perkLevel = PerkUtil.countForPerk(SpellDamagePerk.INSTANCE, caster);
        if (perkLevel <= 0) {
            return damage;
        }

        return damage + (2.0F * perkLevel);
    }
}

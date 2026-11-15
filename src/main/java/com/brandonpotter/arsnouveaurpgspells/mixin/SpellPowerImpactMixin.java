package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.perk.SpellDamagePerk;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.internals.impact.SpellImpacts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Applies Ars Nouveau's Spell Power armor thread directly to Spell Engine's
 * calculated damage amount, while the spell caster is still in scope.
 */
@Mixin(value = SpellImpacts.class, remap = false)
public abstract class SpellPowerImpactMixin {

    @ModifyExpressionValue(
            method = "performImpact",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/spell_power/api/SpellPower$Result$Value;amount()D"
            ),
            remap = false
    )
    private static double arsnouveauRpgSpells$addArmorSpellPowerThread(
            double damage,
            @Local(argsOnly = true, ordinal = 0) LivingEntity caster
    ) {
        if (caster.level().isClientSide || damage <= 0.0D) {
            return damage;
        }

        int perkLevel = PerkUtil.countForPerk(SpellDamagePerk.INSTANCE, caster);
        if (perkLevel <= 0) {
            return damage;
        }

        return damage + (2.0D * perkLevel);
    }
}

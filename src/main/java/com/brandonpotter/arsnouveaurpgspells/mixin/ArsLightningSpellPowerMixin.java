package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.hollingsworth.arsnouveau.common.spell.effect.EffectLightning;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applies Spell Power's Lightning school to Ars Nouveau's dedicated
 * Lightning Entity damage. This effect creates its own entity and therefore
 * does not use the normal SpellDamageEvent damage path.
 */
@Mixin(value = EffectLightning.class, remap = false)
public abstract class ArsLightningSpellPowerMixin {

    @ModifyArg(
            method = "onResolve",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hollingsworth/arsnouveau/common/entity/LightningEntity;setDamage(F)V"
            ),
            index = 0
    )
    private float arsnouveauRpgSpells$applyLightningSpellPower(
            float damage,
            @Local(argsOnly = true, ordinal = 0) LivingEntity caster
    ) {
        if (caster.level().isClientSide || damage <= 0.0F) {
            return damage;
        }

        double baseline = SpellSchools.GENERIC.attributeBaseValue();
        if (baseline <= 0.0D) {
            return damage;
        }

        double lightningPower = SpellPower.getSpellPower(SpellSchools.LIGHTNING, caster).baseValue();
        double multiplier = lightningPower / baseline;
        if (Double.isFinite(multiplier) && multiplier > 0.0D) {
            return (float) (damage * multiplier);
        }

        return damage;
    }
}

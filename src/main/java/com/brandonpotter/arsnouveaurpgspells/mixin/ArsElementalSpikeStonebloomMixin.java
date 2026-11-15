package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applies More RPG Library's Stonebloom Earth bonus to Ars Elemental's Spike.
 *
 * This pseudo-targeted mixin keeps Ars Elemental optional. The Earth school is
 * looked up dynamically, so this class does not link against Ars Elemental or
 * More RPG Library classes.
 */
@Pseudo
@Mixin(targets = "alexthw.ars_elemental.common.entity.spikes.DripstoneSpikeEntity", remap = false)
public abstract class ArsElementalSpikeStonebloomMixin {

    @ModifyArg(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hollingsworth/arsnouveau/api/spell/IDamageEffect;attemptDamage(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lcom/hollingsworth/arsnouveau/api/spell/SpellStats;Lcom/hollingsworth/arsnouveau/api/spell/SpellContext;Lcom/hollingsworth/arsnouveau/api/spell/SpellResolver;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            ),
            index = 7,
            remap = false
    )
    private float arsnouveauRpgSpells$applyStonebloomToSpike(
            float damage,
            @Local(argsOnly = true, ordinal = 0) LivingEntity caster
    ) {
        if (caster.level().isClientSide || damage <= 0.0F) {
            return damage;
        }

        SpellSchool earthSchool = SpellSchools.getSchool("earth");
        if (earthSchool == null) {
            return damage;
        }

        double baseline = SpellSchools.GENERIC.attributeBaseValue();
        if (baseline <= 0.0D) {
            return damage;
        }

        double earthPower = SpellPower.getSpellPower(earthSchool, caster).baseValue();
        double multiplier = earthPower / baseline;
        if (Double.isFinite(multiplier) && multiplier > 0.0D) {
            return (float) (damage * multiplier);
        }

        return damage;
    }
}

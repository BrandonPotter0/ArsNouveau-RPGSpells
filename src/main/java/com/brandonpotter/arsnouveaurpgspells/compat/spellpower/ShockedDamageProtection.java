package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Ensures Ars Nouveau's post-mitigation Shocked lightning bonus is reduced
 * by the same proportional enchantment protection as the original hit.
 */
public final class ShockedDamageProtection {
    private ShockedDamageProtection() {
    }

    public static void onDamagePre(LivingDamageEvent.Pre event) {
        var entity = event.getEntity();
        var source = event.getSource();

        if (entity.level().isClientSide || !source.is(DamageTypes.LIGHTNING_BOLT)) {
            return;
        }

        MobEffectInstance shocked = entity.getEffect(ModPotions.SHOCKED_EFFECT);
        if (shocked == null) {
            return;
        }

        DamageContainer container = event.getContainer();

        // Ars Nouveau adds this bonus in LivingDamageEvent.Pre, after vanilla
        // armor/enchantment/effect reductions have already been captured.
        float shockedBonus = 3.0F + 3.0F * shocked.getAmplifier();
        if (shockedBonus <= 0.0F) {
            return;
        }

        // Reconstruct the damage amount entering the enchantment-reduction stage.
        // Armor and invulnerability reductions occur before enchantment reduction.
        float damageBeforeEnchantments = container.getOriginalDamage()
                - container.getReduction(DamageContainer.Reduction.INVULNERABILITY)
                - container.getReduction(DamageContainer.Reduction.ARMOR);

        if (damageBeforeEnchantments <= 0.0F) {
            return;
        }

        float enchantmentReduction = container.getReduction(DamageContainer.Reduction.ENCHANTMENTS);
        if (enchantmentReduction <= 0.0F) {
            return;
        }

        float protectionFraction = Math.clamp(
                enchantmentReduction / damageBeforeEnchantments,
                0.0F,
                1.0F
        );
        float protectedShockedBonus = shockedBonus * (1.0F - protectionFraction);

        // Only adjust Ars Nouveau's added Shocked bonus. The original lightning
        // damage has already received its normal enchantment reduction.
        event.setNewDamage(Math.max(
                0.0F,
                event.getNewDamage() - shockedBonus + protectedShockedBonus
        ));
    }
}

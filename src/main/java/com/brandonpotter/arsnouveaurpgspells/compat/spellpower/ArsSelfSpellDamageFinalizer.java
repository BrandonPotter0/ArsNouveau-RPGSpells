package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Final-stage safeguard for self-inflicted Ars Nouveau spell damage.
 *
 * Lodestone can restore the event's damage value after NeoForge has already
 * recorded armor and enchantment reductions in DamageContainer. Reconcile only
 * self-inflicted Ars spell damage, and only when the event value exceeds the
 * amount implied by those recorded reductions.
 */
public final class ArsSelfSpellDamageFinalizer {
    private static final TagKey<DamageType> IS_MAGIC =
            TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("c", "is_magic"));

    private ArsSelfSpellDamageFinalizer() {
    }

    public static void onDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();

        if (entity.level().isClientSide
                || source.getEntity() != entity
                || !source.typeHolder().unwrapKey()
                        .map(key -> key.location().toString().equals("ars_nouveau:spell"))
                        .orElse(false)
                || !source.is(IS_MAGIC)) {
            return;
        }

        DamageContainer container = event.getContainer();
        float trackedReductions =
                container.getReduction(DamageContainer.Reduction.INVULNERABILITY)
                + container.getReduction(DamageContainer.Reduction.ARMOR)
                + container.getReduction(DamageContainer.Reduction.ENCHANTMENTS)
                + container.getReduction(DamageContainer.Reduction.MOB_EFFECTS);

        float expectedDamage = Math.max(0.0F, event.getOriginalDamage() - trackedReductions);
        float currentDamage = event.getNewDamage();

        // Never increase damage or interfere with a handler that has already
        // reduced it below the amount represented by NeoForge's reductions.
        if (currentDamage > expectedDamage + 0.01F) {
            event.setNewDamage(expectedDamage);
        }
    }
}

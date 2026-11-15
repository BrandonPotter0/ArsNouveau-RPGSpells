package com.brandonpotter.arsnouveaurpgspells.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applies Stonebloom's Earth bonus to Ars Elemental's physical Dripstone Spike.
 *
 * This is deliberately scoped to DripstoneSpikeEntity.damage, so it does not
 * affect Ice Spikes, falling spikes, or other Ars Nouveau spell damage.
 */
@Pseudo
@Mixin(targets = "alexthw.ars_elemental.common.entity.spikes.DripstoneSpikeEntity", remap = false)
public abstract class ArsElementalSpikeStonebloomMixin {

    private static final ResourceKey<Enchantment> STONEBLOOM =
            ResourceKey.create(Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath("mrpg_lib", "stonebloom"));

    @Shadow
    private LivingEntity caster;

    @ModifyArg(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lalexthw/ars_elemental/common/glyphs/EffectSpike;attemptDamage(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lcom/hollingsworth/arsnouveau/api/spell/SpellStats;Lcom/hollingsworth/arsnouveau/api/spell/SpellContext;Lcom/hollingsworth/arsnouveau/api/spell/SpellResolver;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            ),
            index = 7,
            remap = false
    )
    private float arsnouveauRpgSpells$applyStonebloom(float damage) {
        if (damage <= 0.0F || caster == null || caster.level().isClientSide) {
            return damage;
        }

        Holder<Enchantment> stonebloom = caster.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(STONEBLOOM)
                .orElse(null);
        if (stonebloom == null) {
            return damage;
        }

        // Attribute enchantments apply from every equipped armor piece, so
        // sum the levels to mirror Stonebloom's normal armor attribute effect.
        int level = 0;
        for (ItemStack armorPiece : caster.getArmorSlots()) {
            level += EnchantmentHelper.getItemEnchantmentLevel(stonebloom, armorPiece);
        }

        if (level <= 0) {
            return damage;
        }

        return damage * (1.0F + 0.03F * level);
    }
}

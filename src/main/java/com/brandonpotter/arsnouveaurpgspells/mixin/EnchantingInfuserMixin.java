package com.brandonpotter.arsnouveaurpgspells.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * Optional compatibility with Enchanting Infuser.
 *
 * The Infuser builds its available-enchantment list from its own tag hierarchy.
 * Filter Spell Infinity at the final list boundary so it cannot be selected
 * there even when another datapack or mod adds it through a nested tag.
 */
@Pseudo
@Mixin(targets = "fuzs.enchantinginfuser.util.ModEnchantmentHelper", remap = false)
public abstract class EnchantingInfuserMixin {
    private static final ResourceLocation SPELL_INFINITY =
            ResourceLocation.fromNamespaceAndPath("spell_engine", "spell_infinity");

    @Inject(
            method = "getEnchantmentsForItem",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void arsNouveauRpgSpells$hideSpellInfinity(
            net.minecraft.core.RegistryAccess registryAccess,
            net.minecraft.world.item.ItemStack itemStack,
            net.minecraft.tags.TagKey<Enchantment> enchantmentTag,
            boolean includeTreasure,
            CallbackInfoReturnable<Collection<Holder<Enchantment>>> cir
    ) {
        Collection<Holder<Enchantment>> available = cir.getReturnValue();
        if (available == null || available.isEmpty()) {
            return;
        }

        cir.setReturnValue(available.stream()
                .filter(holder -> holder.unwrapKey()
                        .map(key -> !SPELL_INFINITY.equals(key.location()))
                        .orElse(true))
                .toList());
    }
}

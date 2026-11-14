package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.brandonpotter.arsnouveaurpgspells.mana.RuneManaCosts;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.cost.Ammo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Keeps Spell Engine's normal ammo result shape for display and integrations,
 * while preventing rune items from being required or consumed.
 */
@Mixin(Ammo.class)
public abstract class AmmoMixin {
    @Inject(method = "ammoForSpell", at = @At("RETURN"), cancellable = true)
    private static void arsNouveauRpgSpells$replaceRuneAmmoCost(
            Player player,
            Spell spell,
            ItemStack casterStack,
            CallbackInfoReturnable<Ammo.Result> cir
    ) {
        if (!RuneManaCosts.isRuneCostSpell(spell)) {
            return;
        }

        Ammo.Result original = cir.getReturnValue();
        if (original != null) {
            cir.setReturnValue(new Ammo.Result(true, original.item(), 0, List.of()));
        }
    }
}

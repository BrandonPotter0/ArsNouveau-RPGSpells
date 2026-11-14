package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.brandonpotter.arsnouveaurpgspells.compat.arselemental.FocusCompatibility;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.SpellModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds equipment-derived Focus modifiers without changing Spell Engine's cached
 * spell-container modifiers. The returned list is copied so Curio changes take
 * effect immediately and no cached list is mutated.
 */
@Mixin(value = SpellModifiers.class, remap = false)
public abstract class SpellModifiersMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ArsNouveauRpgSpells/FocusCompat");
    @Inject(
            method = "of(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Holder;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void arsNouveauRpgSpells$appendFocusModifiers(
            Player player,
            Holder<Spell> spellEntry,
            CallbackInfoReturnable<List<Spell.Modifier>> cir
    ) {
        LOGGER.info("Focus modifier hook reached for spell {}", spellEntry.value().school == null ? "<no school>" : spellEntry.value().school.id);
        List<Spell.Modifier> focusModifiers = FocusCompatibility.modifiersFor(player, spellEntry);
        LOGGER.info("Focus compatibility produced {} modifiers for player {}", focusModifiers.size(), player.getGameProfile().getName());
        if (focusModifiers.isEmpty()) {
            return;
        }

        List<Spell.Modifier> combined = new ArrayList<>(cir.getReturnValue());
        combined.addAll(focusModifiers);
        cir.setReturnValue(combined);
    }
}

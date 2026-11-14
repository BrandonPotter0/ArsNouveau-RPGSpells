package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.brandonpotter.arsnouveaurpgspells.mana.RuneManaCosts;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.gui.SpellTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

/**
 * Replaces the removed rune-item tooltip with the calculated Ars Nouveau mana cost.
 */
@Mixin(SpellTooltip.class)
public abstract class SpellTooltipMixin {
    @Inject(method = "addSpellDescription", at = @At("RETURN"))
    private static void arsNouveauRpgSpells$appendManaCost(
            Holder<Spell> spellEntry,
            Player player,
            ItemStack itemStack,
            boolean details,
            int indentLevel,
            ArrayList<Component> lines,
            CallbackInfo ci
    ) {
        Spell spell = spellEntry.value();
        if (!RuneManaCosts.isRuneCostSpell(spell)) {
            return;
        }

        int required = RuneManaCosts.calculateManaCost(spell);
        var mana = CapabilityRegistry.getMana(player);
        double available = mana == null ? 0D : mana.getCurrentMana();

        Component line = Component.translatable(
                "tooltip.arsnouveau_rpgspells.mana_cost",
                required,
                (int) Math.floor(available)
        ).withStyle(available + 1.0E-6D >= required ? ChatFormatting.GREEN : ChatFormatting.RED);

        lines.add(Component.literal("  ".repeat(Math.max(0, indentLevel))).append(line));
    }
}

package com.brandonpotter.arsnouveaurpgspells.mana;

import com.brandonpotter.arsnouveaurpgspells.config.ManaConfig;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellEvents;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCaster;

/**
 * Converts RPG Series rune-item requirements into Ars Nouveau mana costs.
 *
 * <p>The original rune amount is retained as the base unit. The result is then
 * multiplied by spell tier (tier zero is one base unit).</p>
 */
public final class RuneManaCosts {
    private RuneManaCosts() {
    }

    public static boolean isRuneCostSpell(Spell spell) {
        if (spell == null || spell.cost == null || spell.cost.item == null) {
            return false;
        }

        String itemId = spell.cost.item.id;
        return itemId != null
                && (itemId.startsWith("runes:") || itemId.startsWith("#runes:"));
    }

    public static int calculateManaCost(Spell spell) {
        if (!isRuneCostSpell(spell)) {
            return 0;
        }

        int runeAmount = Math.max(1, spell.cost.item.amount);
        int tier = Math.max(0, spell.tier);
        double base = ManaConfig.BASE_MANA_PER_RUNE.get();
        double multiplier = ManaConfig.MANA_MULTIPLIER.get();

        return (int) Math.ceil(base * runeAmount * (tier + 1D) * multiplier);
    }

    /**
     * Runs before Spell Engine's normal cooldown and ammo checks. Returning null
     * lets Spell Engine continue normally; an insufficient-mana result stops the
     * attempt before any delivery can occur.
     */
    public static SpellCast.Attempt onCastingAttempt(SpellEvents.CastingAttemptEvent.Args args) {
        Spell spell = args.spell().value();
        if (!isRuneCostSpell(spell)) {
            return null;
        }

        Player player = args.caster();
        if (player.getAbilities().instabuild) {
            return null;
        }

        // Let Spell Engine report cooldown failures with its normal result and tooltip.
        if (player instanceof SpellCaster.Player spellCaster
                && spellCaster.getCooldownManager().isCoolingDown(args.spell())) {
            return null;
        }

        int required = calculateManaCost(spell);
        double available = getCurrentMana(player);
        if (available + 1.0E-6D < required) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.arsnouveau_rpgspells.not_enough_mana",
                            required,
                            (int) Math.floor(available)
                    ),
                    true
            );
            return SpellCast.Attempt.none();
        }

        return null;
    }

    /**
     * Spell Engine fires this event only after a successful delivery completion.
     * The Ammo mixin leaves the original rune result intact for tooltips but sets
     * its consume count to zero, so this is the single mana deduction point.
     */
    public static void onSpellCostConsume(SpellEvents.SpellCostConsumeEvent.Args args) {
        Spell spell = args.spell().value();
        if (!isRuneCostSpell(spell) || args.caster().getAbilities().instabuild) {
            return;
        }

        IManaCap mana = CapabilityRegistry.getMana(args.caster());
        if (mana == null) {
            return;
        }

        mana.removeMana(calculateManaCost(spell));
    }

    private static double getCurrentMana(Player player) {
        IManaCap mana = CapabilityRegistry.getMana(player);
        return mana == null ? 0D : mana.getCurrentMana();
    }
}

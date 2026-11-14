package com.brandonpotter.arsnouveaurpgspells;

import com.brandonpotter.arsnouveaurpgspells.config.ManaConfig;
import com.brandonpotter.arsnouveaurpgspells.mana.RuneManaCosts;
import net.spell_engine.api.spell.event.SpellEvents;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

/**
 * Entry point for the Ars Nouveau Mana for RPG Spells compatibility mod.
 */
@Mod(ArsNouveauRpgSpells.MOD_ID)
public final class ArsNouveauRpgSpells {
    public static final String MOD_ID = "arsnouveau_rpgspells";

    public ArsNouveauRpgSpells(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ManaConfig.SPEC);

        SpellEvents.CASTING_ATTEMPT.PRE.register(RuneManaCosts::onCastingAttempt);
        SpellEvents.COST_CONSUME.register(RuneManaCosts::onSpellCostConsume);
    }
}

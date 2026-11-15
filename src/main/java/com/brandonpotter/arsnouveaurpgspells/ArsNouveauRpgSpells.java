package com.brandonpotter.arsnouveaurpgspells;

import com.brandonpotter.arsnouveaurpgspells.config.ManaConfig;
import com.brandonpotter.arsnouveaurpgspells.compat.spellpower.DamageDiagnostics;
import com.brandonpotter.arsnouveaurpgspells.compat.spellpower.ShockedDamageProtection;
import com.brandonpotter.arsnouveaurpgspells.compat.spellpower.SpellPowerCompatibility;
import com.brandonpotter.arsnouveaurpgspells.mana.RuneManaCosts;
import net.spell_engine.api.spell.event.SpellEvents;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
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
        NeoForge.EVENT_BUS.addListener(SpellPowerCompatibility::onArsSpellDamage);

        // Temporary diagnostics for identifying the damage source and mitigation result
        // when a player is hit by Ars Nouveau's Lightning glyph.
        NeoForge.EVENT_BUS.addListener(DamageDiagnostics::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ShockedDamageProtection::onDamagePre);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, DamageDiagnostics::onDamagePreHighest);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, DamageDiagnostics::onDamagePreHigh);
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, DamageDiagnostics::onDamagePreNormal);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, DamageDiagnostics::onDamagePreLow);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, DamageDiagnostics::onDamagePre);
        NeoForge.EVENT_BUS.addListener(DamageDiagnostics::onDamagePost);
    }
}

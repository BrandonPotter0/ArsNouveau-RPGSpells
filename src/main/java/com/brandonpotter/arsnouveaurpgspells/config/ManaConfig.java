package com.brandonpotter.arsnouveaurpgspells.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side balance settings for RPG spell mana costs.
 */
public final class ManaConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue BASE_MANA_PER_RUNE;
    public static final ModConfigSpec.DoubleValue MANA_MULTIPLIER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("mana_costs");

        BASE_MANA_PER_RUNE = builder
                .comment("Base Ars Nouveau mana cost for one original RPG Series rune requirement.")
                .defineInRange("baseManaPerRune", 30, 1, 100000);

        MANA_MULTIPLIER = builder
                .comment("Global multiplier applied after scaling the base cost by spell tier.")
                .defineInRange("manaMultiplier", 1.0D, 0.0D, 100.0D);

        builder.pop();
        SPEC = builder.build();
    }

    private ManaConfig() {
    }
}

package com.brandonpotter.arsnouveaurpgspells.compat.arselemental;

import com.hollingsworth.arsnouveau.api.util.CuriosUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.spell_engine.api.spell.Spell;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridges Ars Elemental's equipped Curio Focuses to Spell Engine RPG spell modifiers.
 *
 * This class deliberately uses item registry IDs instead of linking to Ars Elemental classes.
 * The integration therefore remains safe to load when Ars Elemental is not installed.
 */
public final class FocusCompatibility {
    private static final String ARS_ELEMENTAL = "ars_elemental:";

    private FocusCompatibility() {
    }

    public static List<Spell.Modifier> modifiersFor(Player player, Holder<Spell> spellEntry) {
        if (player == null || spellEntry == null || spellEntry.value().school == null) {
            return List.of();
        }

        IItemHandlerModifiable curios = CuriosUtil.getAllWornItems(player);
        if (curios == null) {
            return List.of();
        }

        String schoolId = spellEntry.value().school.id.toString();
        List<Spell.Modifier> modifiers = new ArrayList<>();

        for (int slot = 0; slot < curios.getSlots(); slot++) {
            ItemStack stack = curios.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (itemId == null || !ARS_ELEMENTAL.equals(itemId.getNamespace())) {
                continue;
            }

            String path = itemId.getPath();
            if (isNecroticFocus(path)) {
                addNecroticModifiers(modifiers, schoolId);
                continue;
            }

            FocusSchool focusSchool = FocusSchool.fromItemPath(path);
            if (focusSchool == null) {
                continue;
            }

            boolean greater = !path.startsWith("lesser_");
            boolean matching = focusSchool.matches(schoolId);
            boolean elementalSpell = isElementalSchool(schoolId);

            if (matching) {
                addElementalModifiers(modifiers, greater ? 2.0F : 1.0F, true);
            } else if (!greater && elementalSpell) {
                // Ars Elemental Lesser Focuses dampen other elemental schools.
                addElementalModifiers(modifiers, -1.0F, false);
            }
        }

        return modifiers;
    }

    private static boolean isNecroticFocus(String path) {
        return "necrotic_focus".equals(path);
    }

    private static void addElementalModifiers(List<Spell.Modifier> output, float amount, boolean matching) {
        // Power scaling applies only to damage/healing impacts, not utility or movement effects.
        output.add(impactModifier(Spell.Impact.Action.Type.DAMAGE, amount, 0, 0));
        output.add(impactModifier(Spell.Impact.Action.Type.HEAL, amount, 0, 0));
        // Ars Nouveau glyph amplification maps most closely to RPG status-effect amplifier.
        output.add(impactModifier(Spell.Impact.Action.Type.STATUS_EFFECT, 0, Math.round(amount), 0));

        // A matching elemental focus empowers the spell's summon impact as well.
        // Mismatched Lesser Focuses do not penalize summoning entities.
        if (matching) {
            output.add(impactModifier(Spell.Impact.Action.Type.SUMMON, 0, 0,
                    amount > 0 ? Math.round(amount) : 0));
        }
    }

    private static void addNecroticModifiers(List<Spell.Modifier> output, String schoolId) {
        if (!"spell_power:arcane".equals(schoolId)) {
            return;
        }

        // Ars Elemental's Necrotic Focus adds +1 damage modifier to its spell parts.
        output.add(impactModifier(Spell.Impact.Action.Type.DAMAGE, 1.0F, 0, 0));

        // Its RPG counterpart's closest analogue is an Arcane summon impact.
        output.add(impactModifier(Spell.Impact.Action.Type.SUMMON, 0, 0, 1));
    }

    private static Spell.Modifier impactModifier(
            Spell.Impact.Action.Type type,
            float powerMultiplier,
            int effectAmplifier,
            int summonCount
    ) {
        Spell.Modifier modifier = new Spell.Modifier();
        Spell.Modifier.ImpactFilter filter = new Spell.Modifier.ImpactFilter();
        filter.type = type;
        modifier.impact_filters = List.of(filter);

        if (powerMultiplier != 0) {
            modifier.power_modifier = new Spell.Impact.Modifier();
            modifier.power_modifier.power_multiplier = powerMultiplier;
        }
        modifier.effect_amplifier_add = effectAmplifier;
        modifier.summon_spawn_count_add = summonCount;
        return modifier;
    }

    private static boolean isElementalSchool(String schoolId) {
        return schoolId.equals("spell_power:fire")
                || schoolId.equals("spell_power:water")
                || schoolId.equals("spell_power:frost")
                || schoolId.equals("spell_power:air")
                || schoolId.equals("spell_power:earth");
    }

    private enum FocusSchool {
        FIRE("fire"),
        WATER("water", "frost"),
        AIR("air"),
        EARTH("earth");

        private final List<String> schools;

        FocusSchool(String... schools) {
            this.schools = List.of(schools);
        }

        boolean matches(String schoolId) {
            return schools.stream().anyMatch(school -> schoolId.equals("spell_power:" + school));
        }

        static FocusSchool fromItemPath(String path) {
            if (path.endsWith("fire_focus")) return FIRE;
            if (path.endsWith("water_focus")) return WATER;
            if (path.endsWith("air_focus")) return AIR;
            if (path.endsWith("earth_focus")) return EARTH;
            return null;
        }
    }
}

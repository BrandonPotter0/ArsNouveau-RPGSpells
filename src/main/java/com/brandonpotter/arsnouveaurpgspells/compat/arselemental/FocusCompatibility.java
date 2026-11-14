package com.brandonpotter.arsnouveaurpgspells.compat.arselemental;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import net.spell_engine.api.spell.Spell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger("ArsNouveauRpgSpells/FocusCompat");

    private FocusCompatibility() {
    }

    public static List<Spell.Modifier> modifiersFor(Player player, Holder<Spell> spellEntry) {
        if (player == null || spellEntry == null || spellEntry.value().school == null) {
            return List.of();
        }

        String schoolId = spellEntry.value().school.id.toString();
        List<Spell.Modifier> modifiers = new ArrayList<>();

        // Scan the named Curios slot handlers directly. Ars Elemental registers its
        // Focuses in Curios' dedicated "spell_focus" slot; the flattened handler can
        // omit slots that are inactive or otherwise not represented in its view.
        Optional<ICuriosItemHandler> curiosOptional = CuriosApi.getCuriosInventory(player);
        if (curiosOptional.isEmpty()) {
            LOGGER.info("No Curios inventory found for player {}", player.getGameProfile().getName());
            return List.of();
        }

        ICuriosItemHandler curios = curiosOptional.get();
        LOGGER.info("Curios inventory for {} exposes slot handlers: {}",
                player.getGameProfile().getName(), curios.getCurios().keySet());

        for (var entry : curios.getCurios().entrySet()) {
            String slotId = entry.getKey();
            ICurioStacksHandler slotHandler = entry.getValue();
            var stacks = slotHandler.getStacks();
            for (int slot = 0; slot < stacks.getSlots(); slot++) {
                ItemStack stack = stacks.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }

                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                LOGGER.info("Curios slot {}[{}] contains {}", slotId, slot, itemId);
                if (itemId == null || !ARS_ELEMENTAL.equals(itemId.getNamespace())) {
                    continue;
                }

                LOGGER.info("Ars Elemental Focus candidate detected in {}[{}]: {}", slotId, slot, itemId);
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

package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.slf4j.Logger;

/**
 * Temporary diagnostics for investigating Magic Protection damage mitigation.
 * Logs only damage received by players.
 */
public final class DamageDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TagKey<DamageType> IS_MAGIC =
            TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("c", "is_magic"));

    private DamageDiagnostics() {
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        DamageSource source = event.getSource();
        LOGGER.info(
                "[ArsNouveauRpgSpells DamageDebug] INCOMING player={} type={} isMagic={} bypassesEnchantments={} bypassesInvulnerability={} original={} current={} direct={} causing={}",
                player.getGameProfile().getName(),
                damageTypeId(source),
                source.is(IS_MAGIC),
                source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS),
                source.is(DamageTypeTags.BYPASSES_INVULNERABILITY),
                event.getOriginalAmount(),
                event.getAmount(),
                describeEntity(source.getDirectEntity()),
                describeEntity(source.getEntity())
        );
    }

    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        DamageSource source = event.getSource();
        DamageContainer container = event.getContainer();
        LOGGER.info(
                "[ArsNouveauRpgSpells DamageDebug] FINAL player={} type={} isMagic={} bypassesEnchantments={} original={} armorReduction={} enchantmentReduction={} mobEffectReduction={} absorptionReduction={} afterMitigation={} armorLevels=[head:{},chest:{},legs:{},feet:{}]",
                player.getGameProfile().getName(),
                damageTypeId(source),
                source.is(IS_MAGIC),
                source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS),
                event.getOriginalDamage(),
                container.getReduction(DamageContainer.Reduction.ARMOR),
                container.getReduction(DamageContainer.Reduction.ENCHANTMENTS),
                container.getReduction(DamageContainer.Reduction.MOB_EFFECTS),
                container.getReduction(DamageContainer.Reduction.ABSORPTION),
                event.getNewDamage(),
                player.getItemBySlot(EquipmentSlot.HEAD).getEnchantments().size(),
                player.getItemBySlot(EquipmentSlot.CHEST).getEnchantments().size(),
                player.getItemBySlot(EquipmentSlot.LEGS).getEnchantments().size(),
                player.getItemBySlot(EquipmentSlot.FEET).getEnchantments().size()
        );
    }

    private static String damageTypeId(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(key -> key.location().toString())
                .orElse("unknown");
    }

    private static String describeEntity(Entity entity) {
        if (entity == null) {
            return "none";
        }
        return entity.getType() + "/" + entity.getClass().getName();
    }
}

package com.brandonpotter.arsnouveaurpgspells.compat.spellpower;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Temporary diagnostics for investigating Magic Protection against Ars Nouveau Lightning.
 * Logs only damage received by players, so ordinary mob combat does not flood the log.
 */
public final class DamageDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TagKey<net.minecraft.world.damagesource.DamageType> IS_MAGIC =
            TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("c", "is_magic"));

    private DamageDiagnostics() {
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        DamageSource source = event.getSource();
        LOGGER.info(
                "[ArsNouveauRpgSpells DamageDebug] INCOMING player={} type={} isMagic={} original={} current={} direct={} causing={}",
                player.getGameProfile().getName(),
                damageTypeId(source),
                source.is(IS_MAGIC),
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
        LOGGER.info(
                "[ArsNouveauRpgSpells DamageDebug] FINAL player={} type={} isMagic={} original={} afterMitigation={}",
                player.getGameProfile().getName(),
                damageTypeId(source),
                source.is(IS_MAGIC),
                event.getOriginalDamage(),
                event.getNewDamage()
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
        return entity.getType().toString() + "/" + entity.getClass().getName();
    }
}

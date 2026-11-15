package com.brandonpotter.arsnouveaurpgspells.debug;

import com.brandonpotter.arsnouveaurpgspells.ArsNouveauRpgSpells;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.mojang.logging.LogUtils;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.slf4j.Logger;

/**
 * Temporary diagnostics for Ars Elemental's dripstone Spike damage pipeline.
 *
 * Logs only stalagmite damage, so normal combat does not flood the log.
 * Compare HIGHEST and LOWEST entries at each event stage to identify where
 * another event listener changes the damage amount.
 */
@EventBusSubscriber(modid = ArsNouveauRpgSpells.MOD_ID)
public final class SpikeDamageDebugger {
    private static final Logger LOGGER = LogUtils.getLogger();

    private SpikeDamageDebugger() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSpellDamagePreHighest(SpellDamageEvent.Pre event) {
        if (!isStalagmite(event.damageSource) || event.caster.level().isClientSide) {
            return;
        }

        LOGGER.info(
                "[SpikeDebug] SpellDamageEvent.Pre HIGHEST | caster={} | target={} | damage={}",
                name(event.caster),
                name(event.target),
                event.damage
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellDamagePreLowest(SpellDamageEvent.Pre event) {
        if (!isStalagmite(event.damageSource) || event.caster.level().isClientSide) {
            return;
        }

        LOGGER.info(
                "[SpikeDebug] SpellDamageEvent.Pre LOWEST | caster={} | target={} | damage={} | canceled={}",
                name(event.caster),
                name(event.target),
                event.damage,
                event.isCanceled()
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamageHighest(LivingIncomingDamageEvent event) {
        if (!isStalagmite(event.getSource()) || event.getEntity().level().isClientSide) {
            return;
        }

        LOGGER.info(
                "[SpikeDebug] LivingIncomingDamageEvent HIGHEST | target={} | health={} | amount={} | sourceEntity={}",
                name(event.getEntity()),
                event.getEntity().getHealth(),
                event.getAmount(),
                name(event.getSource().getEntity())
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onIncomingDamageLowest(LivingIncomingDamageEvent event) {
        if (!isStalagmite(event.getSource()) || event.getEntity().level().isClientSide) {
            return;
        }

        LOGGER.info(
                "[SpikeDebug] LivingIncomingDamageEvent LOWEST | target={} | health={} | amount={} | sourceEntity={} | canceled={}",
                name(event.getEntity()),
                event.getEntity().getHealth(),
                event.getAmount(),
                name(event.getSource().getEntity()),
                event.isCanceled()
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamagePreHighest(LivingDamageEvent.Pre event) {
        if (!isStalagmite(event.getSource()) || event.getEntity().level().isClientSide) {
            return;
        }

        LOGGER.info(
                "[SpikeDebug] LivingDamageEvent.Pre HIGHEST | target={} | health={} | newDamage={}",
                name(event.getEntity()),
                event.getEntity().getHealth(),
                event.getNewDamage()
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamagePreLowest(LivingDamageEvent.Pre event) {
        if (!isStalagmite(event.getSource()) || event.getEntity().level().isClientSide) {
            return;
        }

        LOGGER.info(
                "[SpikeDebug] LivingDamageEvent.Pre LOWEST | target={} | health={} | newDamage={}",
                name(event.getEntity()),
                event.getEntity().getHealth(),
                event.getNewDamage()
        );
    }

    private static boolean isStalagmite(net.minecraft.world.damagesource.DamageSource source) {
        return source != null && source.is(DamageTypes.STALAGMITE);
    }

    private static String name(Entity entity) {
        return entity == null ? "<none>" : entity.getType().toString();
    }
}

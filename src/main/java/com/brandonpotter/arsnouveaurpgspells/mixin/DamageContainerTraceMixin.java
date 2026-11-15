package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Temporary diagnostic: capture the caller when Ars Nouveau spell damage is
 * increased through DamageContainer.setNewDamage.
 */
@Mixin(value = DamageContainer.class, remap = false)
public abstract class DamageContainerTraceMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    private float newDamage;

    @Inject(method = "setNewDamage(F)V", at = @At("HEAD"), remap = false)
    private void arsnouveauRpgSpells$traceDamageIncrease(float requestedDamage, CallbackInfo ci) {
        DamageContainer container = (DamageContainer) (Object) this;
        DamageSource source = container.getSource();

        boolean isArsSpell = source.typeHolder().unwrapKey()
                .map(key -> key.location().toString().equals("ars_nouveau:spell"))
                .orElse(false);
        if (!isArsSpell || !(source.getEntity() instanceof Player causingPlayer)
                || requestedDamage <= this.newDamage + 0.05F) {
            return;
        }

        LOGGER.warn(
                "[ArsNouveauRpgSpells DamageDebug] DAMAGE_RESET causingPlayer={} current={} requested={} direct={} sourceEntity={}",
                causingPlayer.getGameProfile().getName(),
                this.newDamage,
                requestedDamage,
                source.getDirectEntity(),
                source.getEntity(),
                new Throwable("DamageContainer.setNewDamage caller")
        );
    }
}

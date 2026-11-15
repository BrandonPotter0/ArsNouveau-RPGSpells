package com.brandonpotter.arsnouveaurpgspells.mixin;

import com.hollingsworth.arsnouveau.api.spell.AbstractEffect;
import com.hollingsworth.arsnouveau.api.spell.IDamageEffect;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Temporary testing hook: allow Ars Nouveau damage effects to hit their own
 * caster when Self is used. Normal ally protection remains unchanged.
 *
 * AbstractEffect is a class; adding this implementation there overrides the
 * IDamageEffect interface default for its concrete damage-effect subclasses.
 * Remove this mixin after self-damage testing is complete.
 */
@Mixin(value = AbstractEffect.class, remap = false)
public abstract class ArsDamageSelfTargetMixin {

    public boolean canDamage(
            LivingEntity shooter,
            SpellStats stats,
            SpellContext spellContext,
            SpellResolver resolver,
            Entity target
    ) {
        return target instanceof LivingEntity living && living.getHealth() <= 0
                ? false
                : target == shooter || !target.isAlliedTo(shooter);
    }
}

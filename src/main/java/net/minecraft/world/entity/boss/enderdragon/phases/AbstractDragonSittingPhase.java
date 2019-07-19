package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.AbstractArrow;

public abstract class AbstractDragonSittingPhase extends AbstractDragonPhaseInstance {
    public AbstractDragonSittingPhase(EnderDragon param0) {
        super(param0);
    }

    @Override
    public boolean isSitting() {
        return true;
    }

    @Override
    public float onHurt(DamageSource param0, float param1) {
        if (param0.getDirectEntity() instanceof AbstractArrow) {
            param0.getDirectEntity().setSecondsOnFire(1);
            return 0.0F;
        } else {
            return super.onHurt(param0, param1);
        }
    }
}

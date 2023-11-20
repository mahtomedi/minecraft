package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class ExplosionDamageCalculator {
    public Optional<Float> getBlockExplosionResistance(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, FluidState param4) {
        return param3.isAir() && param4.isEmpty()
            ? Optional.empty()
            : Optional.of(Math.max(param3.getBlock().getExplosionResistance(), param4.getExplosionResistance()));
    }

    public boolean shouldBlockExplode(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, float param4) {
        return true;
    }

    public boolean shouldDamageEntity(Explosion param0, Entity param1) {
        return true;
    }

    public float getEntityDamageAmount(Explosion param0, Entity param1) {
        float var0 = param0.radius() * 2.0F;
        Vec3 var1 = param0.center();
        double var2 = Math.sqrt(param1.distanceToSqr(var1)) / (double)var0;
        double var3 = (1.0 - var2) * (double)Explosion.getSeenPercent(var1, param1);
        return (float)((var3 * var3 + var3) / 2.0 * 7.0 * (double)var0 + 1.0);
    }
}

package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class EntityBasedExplosionDamageCalculator extends ExplosionDamageCalculator {
    private final Entity source;

    public EntityBasedExplosionDamageCalculator(Entity param0) {
        this.source = param0;
    }

    @Override
    public Optional<Float> getBlockExplosionResistance(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, FluidState param4) {
        return super.getBlockExplosionResistance(param0, param1, param2, param3, param4)
            .map(param5 -> this.source.getBlockExplosionResistance(param0, param1, param2, param3, param4, param5));
    }

    @Override
    public boolean shouldBlockExplode(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, float param4) {
        return this.source.shouldBlockExplode(param0, param1, param2, param3, param4);
    }
}

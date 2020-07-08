package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class ExplosionDamageCalculator {
    public Optional<Float> getBlockExplosionResistance(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, FluidState param4) {
        return param3.isAir() && param4.isEmpty()
            ? Optional.empty()
            : Optional.of(Math.max(param3.getBlock().getExplosionResistance(), param4.getExplosionResistance()));
    }

    public boolean shouldBlockExplode(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, float param4) {
        return true;
    }
}

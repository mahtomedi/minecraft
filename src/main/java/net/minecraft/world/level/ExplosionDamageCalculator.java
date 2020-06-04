package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface ExplosionDamageCalculator {
    Optional<Float> getBlockExplosionResistance(Explosion var1, BlockGetter var2, BlockPos var3, BlockState var4, FluidState var5);

    boolean shouldBlockExplode(Explosion var1, BlockGetter var2, BlockPos var3, BlockState var4, float var5);
}

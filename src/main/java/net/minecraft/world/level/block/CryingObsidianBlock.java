package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CryingObsidianBlock extends Block {
    public CryingObsidianBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        if (param3.nextInt(5) == 0) {
            Direction var0 = Direction.getRandom(param3);
            if (var0 != Direction.UP) {
                BlockPos var1 = param2.relative(var0);
                BlockState var2 = param1.getBlockState(var1);
                if (!param0.canOcclude() || !var2.isFaceSturdy(param1, var1, var0.getOpposite())) {
                    double var3 = var0.getStepX() == 0 ? param3.nextDouble() : 0.5 + (double)var0.getStepX() * 0.6;
                    double var4 = var0.getStepY() == 0 ? param3.nextDouble() : 0.5 + (double)var0.getStepY() * 0.6;
                    double var5 = var0.getStepZ() == 0 ? param3.nextDouble() : 0.5 + (double)var0.getStepZ() * 0.6;
                    param1.addParticle(
                        ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
                        (double)param2.getX() + var3,
                        (double)param2.getY() + var4,
                        (double)param2.getZ() + var5,
                        0.0,
                        0.0,
                        0.0
                    );
                }
            }
        }
    }
}

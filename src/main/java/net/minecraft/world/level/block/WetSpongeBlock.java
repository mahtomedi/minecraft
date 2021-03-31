package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WetSpongeBlock extends Block {
    protected WetSpongeBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param1.dimensionType().ultraWarm()) {
            param1.setBlock(param2, Blocks.SPONGE.defaultBlockState(), 3);
            param1.levelEvent(2009, param2, 0);
            param1.playSound(null, param2, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (1.0F + param1.getRandom().nextFloat() * 0.2F) * 0.7F);
        }

    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        Direction var0 = Direction.getRandom(param3);
        if (var0 != Direction.UP) {
            BlockPos var1 = param2.relative(var0);
            BlockState var2 = param1.getBlockState(var1);
            if (!param0.canOcclude() || !var2.isFaceSturdy(param1, var1, var0.getOpposite())) {
                double var3 = (double)param2.getX();
                double var4 = (double)param2.getY();
                double var5 = (double)param2.getZ();
                if (var0 == Direction.DOWN) {
                    var4 -= 0.05;
                    var3 += param3.nextDouble();
                    var5 += param3.nextDouble();
                } else {
                    var4 += param3.nextDouble() * 0.8;
                    if (var0.getAxis() == Direction.Axis.X) {
                        var5 += param3.nextDouble();
                        if (var0 == Direction.EAST) {
                            ++var3;
                        } else {
                            var3 += 0.05;
                        }
                    } else {
                        var3 += param3.nextDouble();
                        if (var0 == Direction.SOUTH) {
                            ++var5;
                        } else {
                            var5 += 0.05;
                        }
                    }
                }

                param1.addParticle(ParticleTypes.DRIPPING_WATER, var3, var4, var5, 0.0, 0.0, 0.0);
            }
        }
    }
}

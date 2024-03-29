package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CherryLeavesBlock extends LeavesBlock {
    public static final MapCodec<CherryLeavesBlock> CODEC = simpleCodec(CherryLeavesBlock::new);

    @Override
    public MapCodec<CherryLeavesBlock> codec() {
        return CODEC;
    }

    public CherryLeavesBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        super.animateTick(param0, param1, param2, param3);
        if (param3.nextInt(10) == 0) {
            BlockPos var0 = param2.below();
            BlockState var1 = param1.getBlockState(var0);
            if (!isFaceFull(var1.getCollisionShape(param1, var0), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(param1, param2, param3, ParticleTypes.CHERRY_LEAVES);
            }
        }
    }
}

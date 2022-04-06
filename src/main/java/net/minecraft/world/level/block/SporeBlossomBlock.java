package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SporeBlossomBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0);
    private static final int ADD_PARTICLE_ATTEMPTS = 14;
    private static final int PARTICLE_XZ_RADIUS = 10;
    private static final int PARTICLE_Y_MAX = 10;

    public SporeBlossomBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return Block.canSupportCenter(param1, param2.above(), Direction.DOWN) && !param1.isWaterAt(param2);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1 == Direction.UP && !this.canSurvive(param0, param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        int var0 = param2.getX();
        int var1 = param2.getY();
        int var2 = param2.getZ();
        double var3 = (double)var0 + param3.nextDouble();
        double var4 = (double)var1 + 0.7;
        double var5 = (double)var2 + param3.nextDouble();
        param1.addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, var3, var4, var5, 0.0, 0.0, 0.0);
        BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

        for(int var7 = 0; var7 < 14; ++var7) {
            var6.set(var0 + Mth.nextInt(param3, -10, 10), var1 - param3.nextInt(10), var2 + Mth.nextInt(param3, -10, 10));
            BlockState var8 = param1.getBlockState(var6);
            if (!var8.isCollisionShapeFullBlock(param1, var6)) {
                param1.addParticle(
                    ParticleTypes.SPORE_BLOSSOM_AIR,
                    (double)var6.getX() + param3.nextDouble(),
                    (double)var6.getY() + param3.nextDouble(),
                    (double)var6.getZ() + param3.nextDouble(),
                    0.0,
                    0.0,
                    0.0
                );
            }
        }

    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }
}

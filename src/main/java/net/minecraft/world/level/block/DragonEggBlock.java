package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DragonEggBlock extends FallingBlock {
    protected static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public DragonEggBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public boolean use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        this.teleport(param0, param1, param2);
        return true;
    }

    @Override
    public void attack(BlockState param0, Level param1, BlockPos param2, Player param3) {
        this.teleport(param0, param1, param2);
    }

    private void teleport(BlockState param0, Level param1, BlockPos param2) {
        for(int var0 = 0; var0 < 1000; ++var0) {
            BlockPos var1 = param2.offset(
                param1.random.nextInt(16) - param1.random.nextInt(16),
                param1.random.nextInt(8) - param1.random.nextInt(8),
                param1.random.nextInt(16) - param1.random.nextInt(16)
            );
            if (param1.getBlockState(var1).isAir()) {
                if (param1.isClientSide) {
                    for(int var2 = 0; var2 < 128; ++var2) {
                        double var3 = param1.random.nextDouble();
                        float var4 = (param1.random.nextFloat() - 0.5F) * 0.2F;
                        float var5 = (param1.random.nextFloat() - 0.5F) * 0.2F;
                        float var6 = (param1.random.nextFloat() - 0.5F) * 0.2F;
                        double var7 = Mth.lerp(var3, (double)var1.getX(), (double)param2.getX()) + (param1.random.nextDouble() - 0.5) + 0.5;
                        double var8 = Mth.lerp(var3, (double)var1.getY(), (double)param2.getY()) + param1.random.nextDouble() - 0.5;
                        double var9 = Mth.lerp(var3, (double)var1.getZ(), (double)param2.getZ()) + (param1.random.nextDouble() - 0.5) + 0.5;
                        param1.addParticle(ParticleTypes.PORTAL, var7, var8, var9, (double)var4, (double)var5, (double)var6);
                    }
                } else {
                    param1.setBlock(var1, param0, 2);
                    param1.removeBlock(param2, false);
                }

                return;
            }
        }

    }

    @Override
    public int getTickDelay(LevelReader param0) {
        return 5;
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}

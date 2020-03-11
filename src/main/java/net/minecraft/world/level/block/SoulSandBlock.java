package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SoulSandBlock extends Block {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

    public SoulSandBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.block();
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        BubbleColumnBlock.growColumn(param1, param2.above(), false);
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        param1.getBlockTicks().scheduleTick(param2, this, this.getTickDelay(param1));
    }

    @Override
    public boolean isRedstoneConductor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return true;
    }

    @Override
    public int getTickDelay(LevelReader param0) {
        return 20;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        param1.getBlockTicks().scheduleTick(param2, this, this.getTickDelay(param1));
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    @Override
    public boolean isValidSpawn(BlockState param0, BlockGetter param1, BlockPos param2, EntityType<?> param3) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isViewBlocking(BlockState param0, BlockGetter param1, BlockPos param2) {
        return true;
    }
}

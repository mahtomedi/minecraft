package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BeetrootBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
        Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
    };

    public BeetrootBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected ItemLike getBaseSeedId() {
        return Items.BEETROOT_SEEDS;
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param3.nextInt(3) != 0) {
            super.tick(param0, param1, param2, param3);
        }

    }

    @Override
    protected int getBonemealAgeIncrease(Level param0) {
        return super.getBonemealAgeIncrease(param0) / 3;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_BY_AGE[param0.getValue(this.getAgeProperty())];
    }
}

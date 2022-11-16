package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SkullBlock extends AbstractSkullBlock {
    public static final int MAX = RotationSegment.getMaxSegmentIndex();
    private static final int ROTATIONS = MAX + 1;
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    protected static final VoxelShape PIGLIN_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);

    protected SkullBlock(SkullBlock.Type param0, BlockBehaviour.Properties param1) {
        super(param0, param1);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.getType() == SkullBlock.Types.PIGLIN ? PIGLIN_SHAPE : SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(ROTATION, Integer.valueOf(RotationSegment.convertToSegment(param0.getRotation() + 180.0F)));
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(ROTATION, Integer.valueOf(param1.rotate(param0.getValue(ROTATION), ROTATIONS)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.setValue(ROTATION, Integer.valueOf(param1.mirror(param0.getValue(ROTATION), ROTATIONS)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(ROTATION);
    }

    public interface Type {
    }

    public static enum Types implements SkullBlock.Type {
        SKELETON,
        WITHER_SKELETON,
        PLAYER,
        ZOMBIE,
        CREEPER,
        PIGLIN,
        DRAGON;
    }
}

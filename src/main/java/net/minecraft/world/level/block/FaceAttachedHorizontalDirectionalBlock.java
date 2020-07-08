package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class FaceAttachedHorizontalDirectionalBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    protected FaceAttachedHorizontalDirectionalBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return canAttach(param1, param2, getConnectedDirection(param0).getOpposite());
    }

    public static boolean canAttach(LevelReader param0, BlockPos param1, Direction param2) {
        BlockPos var0 = param1.relative(param2);
        return param0.getBlockState(var0).isFaceSturdy(param0, var0, param2.getOpposite());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        for(Direction var0 : param0.getNearestLookingDirections()) {
            BlockState var1;
            if (var0.getAxis() == Direction.Axis.Y) {
                var1 = this.defaultBlockState()
                    .setValue(FACE, var0 == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
                    .setValue(FACING, param0.getHorizontalDirection());
            } else {
                var1 = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, var0.getOpposite());
            }

            if (var1.canSurvive(param0.getLevel(), param0.getClickedPos())) {
                return var1;
            }
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return getConnectedDirection(param0).getOpposite() == param1 && !param0.canSurvive(param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    protected static Direction getConnectedDirection(BlockState param0) {
        switch((AttachFace)param0.getValue(FACE)) {
            case CEILING:
                return Direction.DOWN;
            case FLOOR:
                return Direction.UP;
            default:
                return param0.getValue(FACING);
        }
    }
}

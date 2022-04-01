package net.minecraft.world.level.block;

import com.google.common.base.Predicates;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ConfiguredStructureTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EndPortalFrameBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    protected static final VoxelShape BASE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 13.0, 16.0);
    protected static final VoxelShape EYE_SHAPE = Block.box(4.0, 13.0, 4.0, 12.0, 16.0, 12.0);
    protected static final VoxelShape FULL_SHAPE = Shapes.or(BASE_SHAPE, EYE_SHAPE);
    @Nullable
    private static BlockPattern portalShape;

    public EndPortalFrameBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Level var0 = param0.getLevel();
        if (var0 instanceof ServerLevel var1) {
            BlockPos var2 = param0.getClickedPos();
            if (var1.structureFeatureManager().getStructureWithPieceAt(var2, BuiltinStructures.STRONGHOLD).isValid()) {
                return this.defaultBlockState().setValue(FACING, Direction.UP);
            }

            BlockPos var3 = var1.findNearestMapFeature(ConfiguredStructureTags.EYE_OF_ENDER_LOCATED, var2, 100, false);
            if (var3 != null) {
                BlockPos var4 = var3.subtract(var2);
                Direction var5 = Direction.getNearest((float)var4.getX(), (float)var4.getY(), (float)var4.getZ());
                return this.defaultBlockState().setValue(FACING, var5);
            }
        }

        return this.defaultBlockState().setValue(FACING, Direction.getRandom(var0.random));
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        super.setPlacedBy(param0, param1, param2, param3, param4);
        BlockPattern.BlockPatternMatch var0 = getOrCreatePortalShape().find(param0, param1);
        if (var0 != null) {
            BlockPos var1 = var0.getFrontTopLeft().offset(-3, 0, -3);

            for(int var2 = 0; var2 < 3; ++var2) {
                for(int var3 = 0; var3 < 3; ++var3) {
                    param0.setBlock(var1.offset(var2, 0, var3), Blocks.END_PORTAL.defaultBlockState(), 2);
                }
            }

            param0.globalLevelEvent(1038, var1.offset(1, 0, 1), 0);
        }

    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }

    public static BlockPattern getOrCreatePortalShape() {
        if (portalShape == null) {
            portalShape = BlockPatternBuilder.start()
                .aisle("?xxx?", "x???x", "x???x", "x???x", "?xxx?")
                .where('?', BlockInWorld.hasState(BlockStatePredicate.ANY))
                .where('x', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(FACING, Predicates.equalTo(Direction.UP))))
                .build();
        }

        return portalShape;
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}

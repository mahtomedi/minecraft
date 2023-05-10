package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlock extends Block implements BucketPickup {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    protected final FlowingFluid fluid;
    private final List<FluidState> stateCache;
    public static final VoxelShape STABLE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    public static final ImmutableList<Direction> POSSIBLE_FLOW_DIRECTIONS = ImmutableList.of(
        Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST
    );

    protected LiquidBlock(FlowingFluid param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.fluid = param0;
        this.stateCache = Lists.newArrayList();
        this.stateCache.add(param0.getSource(false));

        for(int var0 = 1; var0 < 8; ++var0) {
            this.stateCache.add(param0.getFlowing(8 - var0, false));
        }

        this.stateCache.add(param0.getFlowing(8, true));
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return param3.isAbove(STABLE_SHAPE, param2, true)
                && param0.getValue(LEVEL) == 0
                && param3.canStandOnFluid(param1.getFluidState(param2.above()), param0.getFluidState())
            ? STABLE_SHAPE
            : Shapes.empty();
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return param0.getFluidState().isRandomlyTicking();
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        param0.getFluidState().randomTick(param1, param2, param3);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return !this.fluid.is(FluidTags.LAVA);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        int var0 = param0.getValue(LEVEL);
        return this.stateCache.get(Math.min(var0, 8));
    }

    @Override
    public boolean skipRendering(BlockState param0, BlockState param1, Direction param2) {
        return param1.getFluidState().getType().isSame(this.fluid);
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState param0, LootParams.Builder param1) {
        return Collections.emptyList();
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.empty();
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (this.shouldSpreadLiquid(param1, param2, param0)) {
            param1.scheduleTick(param2, param0.getFluidState().getType(), this.fluid.getTickDelay(param1));
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getFluidState().isSource() || param2.getFluidState().isSource()) {
            param3.scheduleTick(param4, param0.getFluidState().getType(), this.fluid.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (this.shouldSpreadLiquid(param1, param2, param0)) {
            param1.scheduleTick(param2, param0.getFluidState().getType(), this.fluid.getTickDelay(param1));
        }

    }

    private boolean shouldSpreadLiquid(Level param0, BlockPos param1, BlockState param2) {
        if (this.fluid.is(FluidTags.LAVA)) {
            boolean var0 = param0.getBlockState(param1.below()).is(Blocks.SOUL_SOIL);

            for(Direction var1 : POSSIBLE_FLOW_DIRECTIONS) {
                BlockPos var2 = param1.relative(var1.getOpposite());
                if (param0.getFluidState(var2).is(FluidTags.WATER)) {
                    Block var3 = param0.getFluidState(param1).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
                    param0.setBlockAndUpdate(param1, var3.defaultBlockState());
                    this.fizz(param0, param1);
                    return false;
                }

                if (var0 && param0.getBlockState(var2).is(Blocks.BLUE_ICE)) {
                    param0.setBlockAndUpdate(param1, Blocks.BASALT.defaultBlockState());
                    this.fizz(param0, param1);
                    return false;
                }
            }
        }

        return true;
    }

    private void fizz(LevelAccessor param0, BlockPos param1) {
        param0.levelEvent(1501, param1, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LEVEL);
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor param0, BlockPos param1, BlockState param2) {
        if (param2.getValue(LEVEL) == 0) {
            param0.setBlock(param1, Blocks.AIR.defaultBlockState(), 11);
            return new ItemStack(this.fluid.getBucket());
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return this.fluid.getPickupSound();
    }
}

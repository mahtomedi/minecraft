package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CandleBlock extends AbstractCandleBlock implements SimpleWaterloggedBlock {
    public static final IntegerProperty CANDLES = BlockStateProperties.CANDLES;
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final ToIntFunction<BlockState> LIGHT_EMISSION = param0 -> param0.getValue(LIT) ? 3 * param0.getValue(CANDLES) : 0;
    private static final Int2ObjectMap<List<Vec3>> PARTICLE_OFFSETS = Util.make(() -> {
        Int2ObjectMap<List<Vec3>> var0 = new Int2ObjectOpenHashMap<>();
        var0.defaultReturnValue(ImmutableList.of());
        var0.put(1, ImmutableList.of(new Vec3(0.5, 0.5, 0.5)));
        var0.put(2, ImmutableList.of(new Vec3(0.375, 0.44, 0.5), new Vec3(0.625, 0.5, 0.44)));
        var0.put(3, ImmutableList.of(new Vec3(0.5, 0.313, 0.625), new Vec3(0.375, 0.44, 0.5), new Vec3(0.56, 0.5, 0.44)));
        var0.put(4, ImmutableList.of(new Vec3(0.44, 0.313, 0.56), new Vec3(0.625, 0.44, 0.56), new Vec3(0.375, 0.44, 0.375), new Vec3(0.56, 0.5, 0.375)));
        return Int2ObjectMaps.unmodifiable(var0);
    });
    private static final VoxelShape ONE_AABB = Block.box(7.0, 0.0, 7.0, 9.0, 6.0, 9.0);
    private static final VoxelShape TWO_AABB = Block.box(5.0, 0.0, 6.0, 11.0, 6.0, 9.0);
    private static final VoxelShape THREE_AABB = Block.box(5.0, 0.0, 6.0, 10.0, 6.0, 11.0);
    private static final VoxelShape FOUR_AABB = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 10.0);

    public CandleBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(CANDLES, Integer.valueOf(1))
                .setValue(LIT, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param3.getItemInHand(param4).isEmpty() && param0.getValue(LIT)) {
            extinguish(param0, param1, param2);
            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        return !param1.isSecondaryUseActive() && param1.getItemInHand().getItem() == this.asItem() && param0.getValue(CANDLES) < 4
            ? true
            : super.canBeReplaced(param0, param1);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = param0.getLevel().getBlockState(param0.getClickedPos());
        if (var0.is(this)) {
            return var0.cycle(CANDLES);
        } else {
            FluidState var1 = param0.getLevel().getFluidState(param0.getClickedPos());
            boolean var2 = var1.getType() == Fluids.WATER;
            return super.getStateForPlacement(param0).setValue(WATERLOGGED, Boolean.valueOf(var2));
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        switch(param0.getValue(CANDLES)) {
            case 1:
            default:
                return ONE_AABB;
            case 2:
                return TWO_AABB;
            case 3:
                return THREE_AABB;
            case 4:
                return FOUR_AABB;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(CANDLES, LIT, WATERLOGGED);
    }

    @Override
    public boolean placeLiquid(LevelAccessor param0, BlockPos param1, BlockState param2, FluidState param3) {
        if (!param2.getValue(WATERLOGGED) && param3.getType() == Fluids.WATER) {
            BlockState var0 = param2.setValue(WATERLOGGED, Boolean.valueOf(true));
            if (param2.getValue(LIT)) {
                extinguish(var0, param0, param1);
            } else {
                param0.setBlock(param1, var0, 3);
            }

            param0.getLiquidTicks().scheduleTick(param1, param3.getType(), param3.getType().getTickDelay(param0));
            return true;
        } else {
            return false;
        }
    }

    public static boolean canLight(BlockState param0) {
        return param0.is(BlockTags.CANDLES, param0x -> param0x.hasProperty(LIT) && param0x.hasProperty(WATERLOGGED))
            && !param0.getValue(LIT)
            && !param0.getValue(WATERLOGGED);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected Iterable<Vec3> getParticleOffsets(BlockState param0) {
        return PARTICLE_OFFSETS.get(param0.getValue(CANDLES).intValue());
    }
}

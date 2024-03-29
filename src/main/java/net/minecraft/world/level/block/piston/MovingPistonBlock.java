package net.minecraft.world.level.block.piston;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MovingPistonBlock extends BaseEntityBlock {
    public static final MapCodec<MovingPistonBlock> CODEC = simpleCodec(MovingPistonBlock::new);
    public static final DirectionProperty FACING = PistonHeadBlock.FACING;
    public static final EnumProperty<PistonType> TYPE = PistonHeadBlock.TYPE;

    @Override
    public MapCodec<MovingPistonBlock> codec() {
        return CODEC;
    }

    public MovingPistonBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.DEFAULT));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return null;
    }

    public static BlockEntity newMovingBlockEntity(BlockPos param0, BlockState param1, BlockState param2, Direction param3, boolean param4, boolean param5) {
        return new PistonMovingBlockEntity(param0, param1, param2, param3, param4, param5);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return createTickerHelper(param2, BlockEntityType.PISTON, PistonMovingBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof PistonMovingBlockEntity) {
                ((PistonMovingBlockEntity)var0).finalTick();
            }

        }
    }

    @Override
    public void destroy(LevelAccessor param0, BlockPos param1, BlockState param2) {
        BlockPos var0 = param1.relative(param2.getValue(FACING).getOpposite());
        BlockState var1 = param0.getBlockState(var0);
        if (var1.getBlock() instanceof PistonBaseBlock && var1.getValue(PistonBaseBlock.EXTENDED)) {
            param0.removeBlock(var0, false);
        }

    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (!param1.isClientSide && param1.getBlockEntity(param2) == null) {
            param1.removeBlock(param2, false);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState param0, LootParams.Builder param1) {
        PistonMovingBlockEntity var0 = this.getBlockEntity(param1.getLevel(), BlockPos.containing(param1.getParameter(LootContextParams.ORIGIN)));
        return var0 == null ? Collections.emptyList() : var0.getMovedState().getDrops(param1);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        PistonMovingBlockEntity var0 = this.getBlockEntity(param1, param2);
        return var0 != null ? var0.getCollisionShape(param1, param2) : Shapes.empty();
    }

    @Nullable
    private PistonMovingBlockEntity getBlockEntity(BlockGetter param0, BlockPos param1) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        return var0 instanceof PistonMovingBlockEntity ? (PistonMovingBlockEntity)var0 : null;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader param0, BlockPos param1, BlockState param2) {
        return ItemStack.EMPTY;
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
        param0.add(FACING, TYPE);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}

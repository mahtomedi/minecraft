package net.minecraft.world.level.block.piston;

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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MovingPistonBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = PistonHeadBlock.FACING;
    public static final EnumProperty<PistonType> TYPE = PistonHeadBlock.TYPE;

    public MovingPistonBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.DEFAULT));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return null;
    }

    public static BlockEntity newMovingBlockEntity(BlockState param0, Direction param1, boolean param2, boolean param3) {
        return new PistonMovingBlockEntity(param0, param1, param2, param3);
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param0.getBlock() != param3.getBlock()) {
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
    public boolean isRedstoneConductor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
    }

    @Override
    public boolean isViewBlocking(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
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
    public List<ItemStack> getDrops(BlockState param0, LootContext.Builder param1) {
        PistonMovingBlockEntity var0 = this.getBlockEntity(param1.getLevel(), param1.getParameter(LootContextParams.BLOCK_POS));
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
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

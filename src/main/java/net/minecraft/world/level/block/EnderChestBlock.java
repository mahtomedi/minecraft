package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnderChestBlock extends AbstractChestBlock<EnderChestBlockEntity> implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    public static final TranslatableComponent CONTAINER_TITLE = new TranslatableComponent("container.enderchest");

    protected EnderChestBlock(Block.Properties param0) {
        super(param0, () -> BlockEntityType.ENDER_CHEST);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState param0, Level param1, BlockPos param2, boolean param3) {
        return DoubleBlockCombiner.Combiner::acceptNone;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        FluidState var0 = param0.getLevel().getFluidState(param0.getClickedPos());
        return this.defaultBlockState()
            .setValue(FACING, param0.getHorizontalDirection().getOpposite())
            .setValue(WATERLOGGED, Boolean.valueOf(var0.getType() == Fluids.WATER));
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        PlayerEnderChestContainer var0 = param3.getEnderChestInventory();
        BlockEntity var1 = param1.getBlockEntity(param2);
        if (var0 != null && var1 instanceof EnderChestBlockEntity) {
            BlockPos var2 = param2.above();
            if (param1.getBlockState(var2).isRedstoneConductor(param1, var2)) {
                return InteractionResult.SUCCESS;
            } else if (param1.isClientSide) {
                return InteractionResult.SUCCESS;
            } else {
                EnderChestBlockEntity var3 = (EnderChestBlockEntity)var1;
                var0.setActiveChest(var3);
                param3.openMenu(new SimpleMenuProvider((param1x, param2x, param3x) -> ChestMenu.threeRows(param1x, param2x, var0), CONTAINER_TITLE));
                param3.awardStat(Stats.OPEN_ENDERCHEST);
                return InteractionResult.SUCCESS;
            }
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new EnderChestBlockEntity();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        for(int var0 = 0; var0 < 3; ++var0) {
            int var1 = param3.nextInt(2) * 2 - 1;
            int var2 = param3.nextInt(2) * 2 - 1;
            double var3 = (double)param2.getX() + 0.5 + 0.25 * (double)var1;
            double var4 = (double)((float)param2.getY() + param3.nextFloat());
            double var5 = (double)param2.getZ() + 0.5 + 0.25 * (double)var2;
            double var6 = (double)(param3.nextFloat() * (float)var1);
            double var7 = ((double)param3.nextFloat() - 0.5) * 0.125;
            double var8 = (double)(param3.nextFloat() * (float)var2);
            param1.addParticle(ParticleTypes.PORTAL, var3, var4, var5, var6, var7, var8);
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
        param0.add(FACING, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}

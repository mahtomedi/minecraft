package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

public class DispenserBlock extends Block {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY = Util.make(
        new Object2ObjectOpenHashMap<>(), param0 -> param0.defaultReturnValue(new DefaultDispenseItemBehavior())
    );
    private static final int TRIGGER_DURATION = 4;

    protected DispenserBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, Boolean.valueOf(false)));
    }

    public static void registerBehavior(ItemLike param0, DispenseItemBehavior param1) {
    }

    protected Vec3 getSpitMotion(BlockState param0) {
        return Vec3.atLowerCornerOf(param0.getValue(FACING).getNormal());
    }

    protected void dispenseFrom(ServerLevel param0, BlockPos param1, BlockState param2) {
        BlockPos var0 = param1.relative(param2.getValue(FACING));
        BlockState var1 = param0.getBlockState(var0);
        if (!var1.isAir() && (!var1.is(Blocks.WATER) && !var1.is(Blocks.LAVA) || var1.getFluidState().isSource())) {
            FallingBlockEntity var6 = FallingBlockEntity.fall(param0, var0, var1, this.getSpitMotion(param2).add(0.0, 0.1, 0.0));
        }

    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        boolean var0 = param1.hasNeighborSignal(param2) || param1.hasNeighborSignal(param2.above());
        boolean var1 = param0.getValue(TRIGGERED);
        if (var0 && !var1) {
            param1.scheduleTick(param2, this, 4);
            param1.setBlock(param2, param0.setValue(TRIGGERED, Boolean.valueOf(true)), 4);
        } else if (!var0 && var1) {
            param1.setBlock(param2, param0.setValue(TRIGGERED, Boolean.valueOf(false)), 4);
        }

    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        this.dispenseFrom(param1, param2, param0);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getNearestLookingDirection().getOpposite());
    }

    public static Position getDispensePosition(BlockSource param0) {
        Direction var0 = param0.getBlockState().getValue(FACING);
        double var1 = param0.x() + 0.7 * (double)var0.getStepX();
        double var2 = param0.y() + 0.7 * (double)var0.getStepY();
        double var3 = param0.z() + 0.7 * (double)var0.getStepZ();
        return new PositionImpl(var1, var2, var3);
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
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
        param0.add(FACING, TRIGGERED);
    }
}

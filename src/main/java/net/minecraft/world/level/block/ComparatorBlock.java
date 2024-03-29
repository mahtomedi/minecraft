package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;

public class ComparatorBlock extends DiodeBlock implements EntityBlock {
    public static final MapCodec<ComparatorBlock> CODEC = simpleCodec(ComparatorBlock::new);
    public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;

    @Override
    public MapCodec<ComparatorBlock> codec() {
        return CODEC;
    }

    public ComparatorBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(MODE, ComparatorMode.COMPARE)
        );
    }

    @Override
    protected int getDelay(BlockState param0) {
        return 2;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1 == Direction.DOWN && !this.canSurviveOn(param3, param5, param2)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    protected int getOutputSignal(BlockGetter param0, BlockPos param1, BlockState param2) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        return var0 instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)var0).getOutputSignal() : 0;
    }

    private int calculateOutputSignal(Level param0, BlockPos param1, BlockState param2) {
        int var0 = this.getInputSignal(param0, param1, param2);
        if (var0 == 0) {
            return 0;
        } else {
            int var1 = this.getAlternateSignal(param0, param1, param2);
            if (var1 > var0) {
                return 0;
            } else {
                return param2.getValue(MODE) == ComparatorMode.SUBTRACT ? var0 - var1 : var0;
            }
        }
    }

    @Override
    protected boolean shouldTurnOn(Level param0, BlockPos param1, BlockState param2) {
        int var0 = this.getInputSignal(param0, param1, param2);
        if (var0 == 0) {
            return false;
        } else {
            int var1 = this.getAlternateSignal(param0, param1, param2);
            if (var0 > var1) {
                return true;
            } else {
                return var0 == var1 && param2.getValue(MODE) == ComparatorMode.COMPARE;
            }
        }
    }

    @Override
    protected int getInputSignal(Level param0, BlockPos param1, BlockState param2) {
        int var0 = super.getInputSignal(param0, param1, param2);
        Direction var1 = param2.getValue(FACING);
        BlockPos var2 = param1.relative(var1);
        BlockState var3 = param0.getBlockState(var2);
        if (var3.hasAnalogOutputSignal()) {
            var0 = var3.getAnalogOutputSignal(param0, var2);
        } else if (var0 < 15 && var3.isRedstoneConductor(param0, var2)) {
            var2 = var2.relative(var1);
            var3 = param0.getBlockState(var2);
            ItemFrame var4 = this.getItemFrame(param0, var1, var2);
            int var5 = Math.max(
                var4 == null ? Integer.MIN_VALUE : var4.getAnalogOutput(),
                var3.hasAnalogOutputSignal() ? var3.getAnalogOutputSignal(param0, var2) : Integer.MIN_VALUE
            );
            if (var5 != Integer.MIN_VALUE) {
                var0 = var5;
            }
        }

        return var0;
    }

    @Nullable
    private ItemFrame getItemFrame(Level param0, Direction param1, BlockPos param2) {
        List<ItemFrame> var0 = param0.getEntitiesOfClass(
            ItemFrame.class,
            new AABB(
                (double)param2.getX(),
                (double)param2.getY(),
                (double)param2.getZ(),
                (double)(param2.getX() + 1),
                (double)(param2.getY() + 1),
                (double)(param2.getZ() + 1)
            ),
            param1x -> param1x != null && param1x.getDirection() == param1
        );
        return var0.size() == 1 ? var0.get(0) : null;
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (!param3.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        } else {
            param0 = param0.cycle(MODE);
            float var0 = param0.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55F : 0.5F;
            param1.playSound(param3, param2, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, var0);
            param1.setBlock(param2, param0, 2);
            this.refreshOutputState(param1, param2, param0);
            return InteractionResult.sidedSuccess(param1.isClientSide);
        }
    }

    @Override
    protected void checkTickOnNeighbor(Level param0, BlockPos param1, BlockState param2) {
        if (!param0.getBlockTicks().willTickThisTick(param1, this)) {
            int var0 = this.calculateOutputSignal(param0, param1, param2);
            BlockEntity var1 = param0.getBlockEntity(param1);
            int var2 = var1 instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)var1).getOutputSignal() : 0;
            if (var0 != var2 || param2.getValue(POWERED) != this.shouldTurnOn(param0, param1, param2)) {
                TickPriority var3 = this.shouldPrioritize(param0, param1, param2) ? TickPriority.HIGH : TickPriority.NORMAL;
                param0.scheduleTick(param1, this, 2, var3);
            }

        }
    }

    private void refreshOutputState(Level param0, BlockPos param1, BlockState param2) {
        int var0 = this.calculateOutputSignal(param0, param1, param2);
        BlockEntity var1 = param0.getBlockEntity(param1);
        int var2 = 0;
        if (var1 instanceof ComparatorBlockEntity var3) {
            var2 = var3.getOutputSignal();
            var3.setOutputSignal(var0);
        }

        if (var2 != var0 || param2.getValue(MODE) == ComparatorMode.COMPARE) {
            boolean var4 = this.shouldTurnOn(param0, param1, param2);
            boolean var5 = param2.getValue(POWERED);
            if (var5 && !var4) {
                param0.setBlock(param1, param2.setValue(POWERED, Boolean.valueOf(false)), 2);
            } else if (!var5 && var4) {
                param0.setBlock(param1, param2.setValue(POWERED, Boolean.valueOf(true)), 2);
            }

            this.updateNeighborsInFront(param0, param1, param2);
        }

    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        this.refreshOutputState(param1, param2, param0);
    }

    @Override
    public boolean triggerEvent(BlockState param0, Level param1, BlockPos param2, int param3, int param4) {
        super.triggerEvent(param0, param1, param2, param3, param4);
        BlockEntity var0 = param1.getBlockEntity(param2);
        return var0 != null && var0.triggerEvent(param3, param4);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new ComparatorBlockEntity(param0, param1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, MODE, POWERED);
    }
}

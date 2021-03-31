package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RepeaterBlock extends DiodeBlock {
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

    protected RepeaterBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(DELAY, Integer.valueOf(1))
                .setValue(LOCKED, Boolean.valueOf(false))
                .setValue(POWERED, Boolean.valueOf(false))
        );
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (!param3.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        } else {
            param1.setBlock(param2, param0.cycle(DELAY), 3);
            return InteractionResult.sidedSuccess(param1.isClientSide);
        }
    }

    @Override
    protected int getDelay(BlockState param0) {
        return param0.getValue(DELAY) * 2;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = super.getStateForPlacement(param0);
        return var0.setValue(LOCKED, Boolean.valueOf(this.isLocked(param0.getLevel(), param0.getClickedPos(), var0)));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return !param3.isClientSide() && param1.getAxis() != param0.getValue(FACING).getAxis()
            ? param0.setValue(LOCKED, Boolean.valueOf(this.isLocked(param3, param4, param0)))
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean isLocked(LevelReader param0, BlockPos param1, BlockState param2) {
        return this.getAlternateSignal(param0, param1, param2) > 0;
    }

    @Override
    protected boolean isAlternateInput(BlockState param0) {
        return isDiode(param0);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param0.getValue(POWERED)) {
            Direction var0 = param0.getValue(FACING);
            double var1 = (double)param2.getX() + 0.5 + (param3.nextDouble() - 0.5) * 0.2;
            double var2 = (double)param2.getY() + 0.4 + (param3.nextDouble() - 0.5) * 0.2;
            double var3 = (double)param2.getZ() + 0.5 + (param3.nextDouble() - 0.5) * 0.2;
            float var4 = -5.0F;
            if (param3.nextBoolean()) {
                var4 = (float)(param0.getValue(DELAY) * 2 - 1);
            }

            var4 /= 16.0F;
            double var5 = (double)(var4 * (float)var0.getStepX());
            double var6 = (double)(var4 * (float)var0.getStepZ());
            param1.addParticle(DustParticleOptions.REDSTONE, var1 + var5, var2, var3 + var6, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, DELAY, LOCKED, POWERED);
    }
}

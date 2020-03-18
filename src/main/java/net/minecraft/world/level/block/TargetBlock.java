package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TargetBlock extends Block {
    private static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;

    public TargetBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(OUTPUT_POWER, Integer.valueOf(0)));
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        int var0 = updateRedstoneOutput(param0, param1, param2, param3);
        Entity var1 = param3.getOwner();
        if (var1 instanceof ServerPlayer) {
            ServerPlayer var2 = (ServerPlayer)var1;
            var2.awardStat(Stats.TARGET_HIT);
            CriteriaTriggers.TARGET_BLOCK_HIT.trigger(var2, param3, param2.getLocation(), var0);
        }

    }

    private static int updateRedstoneOutput(LevelAccessor param0, BlockState param1, BlockHitResult param2, Entity param3) {
        int var0 = getRedstoneStrength(param2, param2.getLocation());
        int var1 = param3 instanceof AbstractArrow ? 20 : 8;
        if (!param0.getBlockTicks().hasScheduledTick(param2.getBlockPos(), param1.getBlock())) {
            setOutputPower(param0, param1, var0, param2.getBlockPos(), var1);
        }

        return var0;
    }

    private static int getRedstoneStrength(BlockHitResult param0, Vec3 param1) {
        Direction var0 = param0.getDirection();
        double var1 = Math.abs(Mth.frac(param1.x) - 0.5);
        double var2 = Math.abs(Mth.frac(param1.y) - 0.5);
        double var3 = Math.abs(Mth.frac(param1.z) - 0.5);
        Direction.Axis var4 = var0.getAxis();
        double var5;
        if (var4 == Direction.Axis.Y) {
            var5 = Math.max(var1, var3);
        } else if (var4 == Direction.Axis.Z) {
            var5 = Math.max(var1, var2);
        } else {
            var5 = Math.max(var2, var3);
        }

        return Mth.ceil(15.0 * Mth.clamp((0.5 - var5) / 0.5, 0.0, 1.0));
    }

    private static void setOutputPower(LevelAccessor param0, BlockState param1, int param2, BlockPos param3, int param4) {
        param0.setBlock(param3, param1.setValue(OUTPUT_POWER, Integer.valueOf(param2)), 3);
        param0.getBlockTicks().scheduleTick(param3, param1.getBlock(), param4);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param0.getValue(OUTPUT_POWER) != 0) {
            param1.setBlock(param2, param0.setValue(OUTPUT_POWER, Integer.valueOf(0)), 3);
        }

    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(OUTPUT_POWER);
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(OUTPUT_POWER);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param1.isClientSide() && param0.getBlock() != param3.getBlock()) {
            if (param0.getValue(OUTPUT_POWER) > 0 && !param1.getBlockTicks().hasScheduledTick(param2, this)) {
                param1.setBlock(param2, param0.setValue(OUTPUT_POWER, Integer.valueOf(0)), 18);
            }

        }
    }
}

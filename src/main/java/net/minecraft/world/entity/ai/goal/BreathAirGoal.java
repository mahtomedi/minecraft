package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BreathAirGoal extends Goal {
    private final PathfinderMob mob;

    public BreathAirGoal(PathfinderMob param0) {
        this.mob = param0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getAirSupply() < 140;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        this.findAirPosition();
    }

    private void findAirPosition() {
        Iterable<BlockPos> var0 = BlockPos.betweenClosed(
            Mth.floor(this.mob.getX() - 1.0),
            Mth.floor(this.mob.getY()),
            Mth.floor(this.mob.getZ() - 1.0),
            Mth.floor(this.mob.getX() + 1.0),
            Mth.floor(this.mob.getY() + 8.0),
            Mth.floor(this.mob.getZ() + 1.0)
        );
        BlockPos var1 = null;

        for(BlockPos var2 : var0) {
            if (this.givesAir(this.mob.level, var2)) {
                var1 = var2;
                break;
            }
        }

        if (var1 == null) {
            var1 = new BlockPos(this.mob.getX(), this.mob.getY() + 8.0, this.mob.getZ());
        }

        this.mob.getNavigation().moveTo((double)var1.getX(), (double)(var1.getY() + 1), (double)var1.getZ(), 1.0);
    }

    @Override
    public void tick() {
        this.findAirPosition();
        this.mob.moveRelative(0.02F, new Vec3((double)this.mob.xxa, (double)this.mob.yya, (double)this.mob.zza));
        this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
    }

    private boolean givesAir(LevelReader param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        return (param0.getFluidState(param1).isEmpty() || var0.is(Blocks.BUBBLE_COLUMN)) && var0.isPathfindable(param0, param1, PathComputationType.LAND);
    }
}

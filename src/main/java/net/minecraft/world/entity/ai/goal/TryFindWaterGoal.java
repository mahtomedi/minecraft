package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;

public class TryFindWaterGoal extends Goal {
    private final PathfinderMob mob;

    public TryFindWaterGoal(PathfinderMob param0) {
        this.mob = param0;
    }

    @Override
    public boolean canUse() {
        return this.mob.isOnGround() && !this.mob.level.getFluidState(this.mob.blockPosition()).is(FluidTags.WATER);
    }

    @Override
    public void start() {
        BlockPos var0 = null;

        for(BlockPos var2 : BlockPos.betweenClosed(
            Mth.floor(this.mob.getX() - 2.0),
            Mth.floor(this.mob.getY() - 2.0),
            Mth.floor(this.mob.getZ() - 2.0),
            Mth.floor(this.mob.getX() + 2.0),
            this.mob.getBlockY(),
            Mth.floor(this.mob.getZ() + 2.0)
        )) {
            if (this.mob.level.getFluidState(var2).is(FluidTags.WATER)) {
                var0 = var2;
                break;
            }
        }

        if (var0 != null) {
            this.mob.getMoveControl().setWantedPosition((double)var0.getX(), (double)var0.getY(), (double)var0.getZ(), 1.0);
        }

    }
}

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
        return this.mob.onGround && !this.mob.level.getFluidState(new BlockPos(this.mob)).is(FluidTags.WATER);
    }

    @Override
    public void start() {
        BlockPos var0 = null;

        for(BlockPos var2 : BlockPos.betweenClosed(
            Mth.floor(this.mob.x - 2.0),
            Mth.floor(this.mob.y - 2.0),
            Mth.floor(this.mob.z - 2.0),
            Mth.floor(this.mob.x + 2.0),
            Mth.floor(this.mob.y),
            Mth.floor(this.mob.z + 2.0)
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

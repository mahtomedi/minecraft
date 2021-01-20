package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class StrollThroughVillageGoal extends Goal {
    private final PathfinderMob mob;
    private final int interval;
    @Nullable
    private BlockPos wantedPos;

    public StrollThroughVillageGoal(PathfinderMob param0, int param1) {
        this.mob = param0;
        this.interval = param1;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isVehicle()) {
            return false;
        } else if (this.mob.level.isDay()) {
            return false;
        } else if (this.mob.getRandom().nextInt(this.interval) != 0) {
            return false;
        } else {
            ServerLevel var0 = (ServerLevel)this.mob.level;
            BlockPos var1 = this.mob.blockPosition();
            if (!var0.isCloseToVillage(var1, 6)) {
                return false;
            } else {
                Vec3 var2 = LandRandomPos.getPos(this.mob, 15, 7, param1 -> (double)(-var0.sectionsToVillage(SectionPos.of(param1))));
                this.wantedPos = var2 == null ? null : new BlockPos(var2);
                return this.wantedPos != null;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.wantedPos != null && !this.mob.getNavigation().isDone() && this.mob.getNavigation().getTargetPos().equals(this.wantedPos);
    }

    @Override
    public void tick() {
        if (this.wantedPos != null) {
            PathNavigation var0 = this.mob.getNavigation();
            if (var0.isDone() && !this.wantedPos.closerThan(this.mob.position(), 10.0)) {
                Vec3 var1 = Vec3.atBottomCenterOf(this.wantedPos);
                Vec3 var2 = this.mob.position();
                Vec3 var3 = var2.subtract(var1);
                var1 = var3.scale(0.4).add(var1);
                Vec3 var4 = var1.subtract(var2).normalize().scale(10.0).add(var2);
                BlockPos var5 = new BlockPos(var4);
                var5 = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, var5);
                if (!var0.moveTo((double)var5.getX(), (double)var5.getY(), (double)var5.getZ(), 1.0)) {
                    this.moveRandomly();
                }
            }

        }
    }

    private void moveRandomly() {
        Random var0 = this.mob.getRandom();
        BlockPos var1 = this.mob
            .level
            .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset(-8 + var0.nextInt(16), 0, -8 + var0.nextInt(16)));
        this.mob.getNavigation().moveTo((double)var1.getX(), (double)var1.getY(), (double)var1.getZ(), 1.0);
    }
}

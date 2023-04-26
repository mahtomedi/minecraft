package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FleeSunGoal extends Goal {
    protected final PathfinderMob mob;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final Level level;

    public FleeSunGoal(PathfinderMob param0, double param1) {
        this.mob = param0;
        this.speedModifier = param1;
        this.level = param0.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() != null) {
            return false;
        } else if (!this.level.isDay()) {
            return false;
        } else if (!this.mob.isOnFire()) {
            return false;
        } else if (!this.level.canSeeSky(this.mob.blockPosition())) {
            return false;
        } else {
            return !this.mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() ? false : this.setWantedPos();
        }
    }

    protected boolean setWantedPos() {
        Vec3 var0 = this.getHidePos();
        if (var0 == null) {
            return false;
        } else {
            this.wantedX = var0.x;
            this.wantedY = var0.y;
            this.wantedZ = var0.z;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    @Nullable
    protected Vec3 getHidePos() {
        RandomSource var0 = this.mob.getRandom();
        BlockPos var1 = this.mob.blockPosition();

        for(int var2 = 0; var2 < 10; ++var2) {
            BlockPos var3 = var1.offset(var0.nextInt(20) - 10, var0.nextInt(6) - 3, var0.nextInt(20) - 10);
            if (!this.level.canSeeSky(var3) && this.mob.getWalkTargetValue(var3) < 0.0F) {
                return Vec3.atBottomCenterOf(var3);
            }
        }

        return null;
    }
}

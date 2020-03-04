package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class PanicGoal extends Goal {
    protected final PathfinderMob mob;
    protected final double speedModifier;
    protected double posX;
    protected double posY;
    protected double posZ;

    public PanicGoal(PathfinderMob param0, double param1) {
        this.mob = param0;
        this.speedModifier = param1;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getLastHurtByMob() == null && !this.mob.isOnFire()) {
            return false;
        } else {
            if (this.mob.isOnFire()) {
                BlockPos var0 = this.lookForWater(this.mob.level, this.mob, 5, 4);
                if (var0 != null) {
                    this.posX = (double)var0.getX();
                    this.posY = (double)var0.getY();
                    this.posZ = (double)var0.getZ();
                    return true;
                }
            }

            return this.findRandomPosition();
        }
    }

    protected boolean findRandomPosition() {
        Vec3 var0 = RandomPos.getPos(this.mob, 5, 4);
        if (var0 == null) {
            return false;
        } else {
            this.posX = var0.x;
            this.posY = var0.y;
            this.posZ = var0.z;
            return true;
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Nullable
    protected BlockPos lookForWater(BlockGetter param0, Entity param1, int param2, int param3) {
        BlockPos var0 = param1.blockPosition();
        int var1 = var0.getX();
        int var2 = var0.getY();
        int var3 = var0.getZ();
        float var4 = (float)(param2 * param2 * param3 * 2);
        BlockPos var5 = null;
        BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

        for(int var7 = var1 - param2; var7 <= var1 + param2; ++var7) {
            for(int var8 = var2 - param3; var8 <= var2 + param3; ++var8) {
                for(int var9 = var3 - param2; var9 <= var3 + param2; ++var9) {
                    var6.set(var7, var8, var9);
                    if (param0.getFluidState(var6).is(FluidTags.WATER)) {
                        float var10 = (float)((var7 - var1) * (var7 - var1) + (var8 - var2) * (var8 - var2) + (var9 - var3) * (var9 - var3));
                        if (var10 < var4) {
                            var4 = var10;
                            var5 = new BlockPos(var6);
                        }
                    }
                }
            }
        }

        return var5;
    }
}

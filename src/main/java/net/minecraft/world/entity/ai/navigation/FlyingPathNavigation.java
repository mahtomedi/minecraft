package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class FlyingPathNavigation extends PathNavigation {
    public FlyingPathNavigation(Mob param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected PathFinder createPathFinder(int param0) {
        this.nodeEvaluator = new FlyNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, param0);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.canFloat() && this.isInLiquid() || !this.mob.isPassenger();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return this.mob.position();
    }

    @Override
    public Path createPath(Entity param0, int param1) {
        return this.createPath(param0.blockPosition(), param1);
    }

    @Override
    public void tick() {
        ++this.tick;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                Vec3 var0 = this.path.getNextEntityPos(this.mob);
                if (Mth.floor(this.mob.getX()) == Mth.floor(var0.x)
                    && Mth.floor(this.mob.getY()) == Mth.floor(var0.y)
                    && Mth.floor(this.mob.getZ()) == Mth.floor(var0.z)) {
                    this.path.advance();
                }
            }

            DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
            if (!this.isDone()) {
                Vec3 var1 = this.path.getNextEntityPos(this.mob);
                this.mob.getMoveControl().setWantedPosition(var1.x, var1.y, var1.z, this.speedModifier);
            }
        }
    }

    @Override
    protected boolean canMoveDirectly(Vec3 param0, Vec3 param1, int param2, int param3, int param4) {
        int var0 = Mth.floor(param0.x);
        int var1 = Mth.floor(param0.y);
        int var2 = Mth.floor(param0.z);
        double var3 = param1.x - param0.x;
        double var4 = param1.y - param0.y;
        double var5 = param1.z - param0.z;
        double var6 = var3 * var3 + var4 * var4 + var5 * var5;
        if (var6 < 1.0E-8) {
            return false;
        } else {
            double var7 = 1.0 / Math.sqrt(var6);
            var3 *= var7;
            var4 *= var7;
            var5 *= var7;
            double var8 = 1.0 / Math.abs(var3);
            double var9 = 1.0 / Math.abs(var4);
            double var10 = 1.0 / Math.abs(var5);
            double var11 = (double)var0 - param0.x;
            double var12 = (double)var1 - param0.y;
            double var13 = (double)var2 - param0.z;
            if (var3 >= 0.0) {
                ++var11;
            }

            if (var4 >= 0.0) {
                ++var12;
            }

            if (var5 >= 0.0) {
                ++var13;
            }

            var11 /= var3;
            var12 /= var4;
            var13 /= var5;
            int var14 = var3 < 0.0 ? -1 : 1;
            int var15 = var4 < 0.0 ? -1 : 1;
            int var16 = var5 < 0.0 ? -1 : 1;
            int var17 = Mth.floor(param1.x);
            int var18 = Mth.floor(param1.y);
            int var19 = Mth.floor(param1.z);
            int var20 = var17 - var0;
            int var21 = var18 - var1;
            int var22 = var19 - var2;

            while(var20 * var14 > 0 || var21 * var15 > 0 || var22 * var16 > 0) {
                if (var11 < var13 && var11 <= var12) {
                    var11 += var8;
                    var0 += var14;
                    var20 = var17 - var0;
                } else if (var12 < var11 && var12 <= var13) {
                    var12 += var9;
                    var1 += var15;
                    var21 = var18 - var1;
                } else {
                    var13 += var10;
                    var2 += var16;
                    var22 = var19 - var2;
                }
            }

            return true;
        }
    }

    public void setCanOpenDoors(boolean param0) {
        this.nodeEvaluator.setCanOpenDoors(param0);
    }

    public void setCanPassDoors(boolean param0) {
        this.nodeEvaluator.setCanPassDoors(param0);
    }

    @Override
    public boolean isStableDestination(BlockPos param0) {
        return this.level.getBlockState(param0).entityCanStandOn(this.level, param0, this.mob);
    }
}

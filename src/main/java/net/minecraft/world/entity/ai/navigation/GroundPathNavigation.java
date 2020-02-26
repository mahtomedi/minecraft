package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation extends PathNavigation {
    private boolean avoidSun;

    public GroundPathNavigation(Mob param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected PathFinder createPathFinder(int param0) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, param0);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.isOnGround() || this.isInLiquid() || this.mob.isPassenger();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), (double)this.getSurfaceY(), this.mob.getZ());
    }

    @Override
    public Path createPath(BlockPos param0, int param1) {
        if (this.level.getBlockState(param0).isAir()) {
            BlockPos var0 = param0.below();

            while(var0.getY() > 0 && this.level.getBlockState(var0).isAir()) {
                var0 = var0.below();
            }

            if (var0.getY() > 0) {
                return super.createPath(var0.above(), param1);
            }

            while(var0.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(var0).isAir()) {
                var0 = var0.above();
            }

            param0 = var0;
        }

        if (!this.level.getBlockState(param0).getMaterial().isSolid()) {
            return super.createPath(param0, param1);
        } else {
            BlockPos var1 = param0.above();

            while(var1.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(var1).getMaterial().isSolid()) {
                var1 = var1.above();
            }

            return super.createPath(var1, param1);
        }
    }

    @Override
    public Path createPath(Entity param0, int param1) {
        return this.createPath(new BlockPos(param0), param1);
    }

    private int getSurfaceY() {
        if (this.mob.isInWater() && this.canFloat()) {
            int var0 = Mth.floor(this.mob.getY());
            Block var1 = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)var0, this.mob.getZ())).getBlock();
            int var2 = 0;

            while(var1 == Blocks.WATER) {
                var1 = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)(++var0), this.mob.getZ())).getBlock();
                if (++var2 > 16) {
                    return Mth.floor(this.mob.getY());
                }
            }

            return var0;
        } else {
            return Mth.floor(this.mob.getY() + 0.5);
        }
    }

    @Override
    protected void trimPath() {
        super.trimPath();
        if (this.avoidSun) {
            if (this.level.canSeeSky(new BlockPos(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()))) {
                return;
            }

            for(int var0 = 0; var0 < this.path.getSize(); ++var0) {
                Node var1 = this.path.get(var0);
                if (this.level.canSeeSky(new BlockPos(var1.x, var1.y, var1.z))) {
                    this.path.truncate(var0);
                    return;
                }
            }
        }

    }

    @Override
    protected boolean canMoveDirectly(Vec3 param0, Vec3 param1, int param2, int param3, int param4) {
        int var0 = Mth.floor(param0.x);
        int var1 = Mth.floor(param0.z);
        double var2 = param1.x - param0.x;
        double var3 = param1.z - param0.z;
        double var4 = var2 * var2 + var3 * var3;
        if (var4 < 1.0E-8) {
            return false;
        } else {
            double var5 = 1.0 / Math.sqrt(var4);
            var2 *= var5;
            var3 *= var5;
            param2 += 2;
            param4 += 2;
            if (!this.canWalkOn(var0, Mth.floor(param0.y), var1, param2, param3, param4, param0, var2, var3)) {
                return false;
            } else {
                param2 -= 2;
                param4 -= 2;
                double var6 = 1.0 / Math.abs(var2);
                double var7 = 1.0 / Math.abs(var3);
                double var8 = (double)var0 - param0.x;
                double var9 = (double)var1 - param0.z;
                if (var2 >= 0.0) {
                    ++var8;
                }

                if (var3 >= 0.0) {
                    ++var9;
                }

                var8 /= var2;
                var9 /= var3;
                int var10 = var2 < 0.0 ? -1 : 1;
                int var11 = var3 < 0.0 ? -1 : 1;
                int var12 = Mth.floor(param1.x);
                int var13 = Mth.floor(param1.z);
                int var14 = var12 - var0;
                int var15 = var13 - var1;

                while(var14 * var10 > 0 || var15 * var11 > 0) {
                    if (var8 < var9) {
                        var8 += var6;
                        var0 += var10;
                        var14 = var12 - var0;
                    } else {
                        var9 += var7;
                        var1 += var11;
                        var15 = var13 - var1;
                    }

                    if (!this.canWalkOn(var0, Mth.floor(param0.y), var1, param2, param3, param4, param0, var2, var3)) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    private boolean canWalkOn(int param0, int param1, int param2, int param3, int param4, int param5, Vec3 param6, double param7, double param8) {
        int var0 = param0 - param3 / 2;
        int var1 = param2 - param5 / 2;
        if (!this.canWalkAbove(var0, param1, var1, param3, param4, param5, param6, param7, param8)) {
            return false;
        } else {
            for(int var2 = var0; var2 < var0 + param3; ++var2) {
                for(int var3 = var1; var3 < var1 + param5; ++var3) {
                    double var4 = (double)var2 + 0.5 - param6.x;
                    double var5 = (double)var3 + 0.5 - param6.z;
                    if (!(var4 * param7 + var5 * param8 < 0.0)) {
                        BlockPathTypes var6 = this.nodeEvaluator
                            .getBlockPathType(this.level, var2, param1 - 1, var3, this.mob, param3, param4, param5, true, true);
                        if (var6 == BlockPathTypes.WATER) {
                            return false;
                        }

                        if (var6 == BlockPathTypes.LAVA) {
                            return false;
                        }

                        if (var6 == BlockPathTypes.OPEN) {
                            return false;
                        }

                        var6 = this.nodeEvaluator.getBlockPathType(this.level, var2, param1, var3, this.mob, param3, param4, param5, true, true);
                        float var7 = this.mob.getPathfindingMalus(var6);
                        if (var7 < 0.0F || var7 >= 8.0F) {
                            return false;
                        }

                        if (var6 == BlockPathTypes.DAMAGE_FIRE || var6 == BlockPathTypes.DANGER_FIRE || var6 == BlockPathTypes.DAMAGE_OTHER) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean canWalkAbove(int param0, int param1, int param2, int param3, int param4, int param5, Vec3 param6, double param7, double param8) {
        for(BlockPos var0 : BlockPos.betweenClosed(
            new BlockPos(param0, param1, param2), new BlockPos(param0 + param3 - 1, param1 + param4 - 1, param2 + param5 - 1)
        )) {
            double var1 = (double)var0.getX() + 0.5 - param6.x;
            double var2 = (double)var0.getZ() + 0.5 - param6.z;
            if (!(var1 * param7 + var2 * param8 < 0.0) && !this.level.getBlockState(var0).isPathfindable(this.level, var0, PathComputationType.LAND)) {
                return false;
            }
        }

        return true;
    }

    public void setCanOpenDoors(boolean param0) {
        this.nodeEvaluator.setCanOpenDoors(param0);
    }

    public boolean canOpenDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setAvoidSun(boolean param0) {
        this.avoidSun = param0;
    }
}

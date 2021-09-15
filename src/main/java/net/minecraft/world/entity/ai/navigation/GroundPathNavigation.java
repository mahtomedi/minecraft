package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
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

            while(var0.getY() > this.level.getMinBuildHeight() && this.level.getBlockState(var0).isAir()) {
                var0 = var0.below();
            }

            if (var0.getY() > this.level.getMinBuildHeight()) {
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
        return this.createPath(param0.blockPosition(), param1);
    }

    private int getSurfaceY() {
        if (this.mob.isInWater() && this.canFloat()) {
            int var0 = this.mob.getBlockY();
            BlockState var1 = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)var0, this.mob.getZ()));
            int var2 = 0;

            while(var1.is(Blocks.WATER)) {
                var1 = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)(++var0), this.mob.getZ()));
                if (++var2 > 16) {
                    return this.mob.getBlockY();
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

            for(int var0 = 0; var0 < this.path.getNodeCount(); ++var0) {
                Node var1 = this.path.getNode(var0);
                if (this.level.canSeeSky(new BlockPos(var1.x, var1.y, var1.z))) {
                    this.path.truncateNodes(var0);
                    return;
                }
            }
        }

    }

    protected boolean hasValidPathType(BlockPathTypes param0) {
        if (param0 == BlockPathTypes.WATER) {
            return false;
        } else if (param0 == BlockPathTypes.LAVA) {
            return false;
        } else {
            return param0 != BlockPathTypes.OPEN;
        }
    }

    public void setCanOpenDoors(boolean param0) {
        this.nodeEvaluator.setCanOpenDoors(param0);
    }

    public boolean canPassDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setCanPassDoors(boolean param0) {
        this.nodeEvaluator.setCanPassDoors(param0);
    }

    public boolean canOpenDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setAvoidSun(boolean param0) {
        this.avoidSun = param0;
    }
}

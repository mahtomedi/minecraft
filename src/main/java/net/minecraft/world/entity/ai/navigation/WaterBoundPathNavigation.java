package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class WaterBoundPathNavigation extends PathNavigation {
    private boolean allowBreaching;

    public WaterBoundPathNavigation(Mob param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected PathFinder createPathFinder(int param0) {
        this.allowBreaching = this.mob.getType() == EntityType.DOLPHIN;
        this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
        return new PathFinder(this.nodeEvaluator, param0);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.allowBreaching || this.mob.isInLiquid();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(0.5), this.mob.getZ());
    }

    @Override
    protected double getGroundY(Vec3 param0) {
        return param0.y;
    }

    @Override
    protected boolean canMoveDirectly(Vec3 param0, Vec3 param1) {
        return isClearForMovementBetween(this.mob, param0, param1, false);
    }

    @Override
    public boolean isStableDestination(BlockPos param0) {
        return !this.level.getBlockState(param0).isSolidRender(this.level, param0);
    }

    @Override
    public void setCanFloat(boolean param0) {
    }
}

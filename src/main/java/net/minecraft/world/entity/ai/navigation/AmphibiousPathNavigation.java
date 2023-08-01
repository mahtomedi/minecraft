package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class AmphibiousPathNavigation extends PathNavigation {
    public AmphibiousPathNavigation(Mob param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected PathFinder createPathFinder(int param0) {
        this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, param0);
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
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
        return this.mob.isInLiquid() ? isClearForMovementBetween(this.mob, param0, param1, false) : false;
    }

    @Override
    public boolean isStableDestination(BlockPos param0) {
        return !this.level.getBlockState(param0.below()).isAir();
    }

    @Override
    public void setCanFloat(boolean param0) {
    }
}

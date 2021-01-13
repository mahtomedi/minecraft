package net.minecraft.world.entity.ai.navigation;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WaterBoundPathNavigation extends PathNavigation {
    private boolean allowBreaching;

    public WaterBoundPathNavigation(Mob param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected PathFinder createPathFinder(int param0) {
        this.allowBreaching = this.mob instanceof Dolphin;
        this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
        return new PathFinder(this.nodeEvaluator, param0);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.allowBreaching || this.isInLiquid();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(0.5), this.mob.getZ());
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
    protected void followThePath() {
        if (this.path != null) {
            Vec3 var0 = this.getTempMobPos();
            float var1 = this.mob.getBbWidth();
            float var2 = var1 > 0.75F ? var1 / 2.0F : 0.75F - var1 / 2.0F;
            Vec3 var3 = this.mob.getDeltaMovement();
            if (Math.abs(var3.x) > 0.2 || Math.abs(var3.z) > 0.2) {
                var2 = (float)((double)var2 * var3.length() * 6.0);
            }

            int var4 = 6;
            Vec3 var5 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
            if (Math.abs(this.mob.getX() - var5.x) < (double)var2
                && Math.abs(this.mob.getZ() - var5.z) < (double)var2
                && Math.abs(this.mob.getY() - var5.y) < (double)(var2 * 2.0F)) {
                this.path.advance();
            }

            for(int var6 = Math.min(this.path.getNextNodeIndex() + 6, this.path.getNodeCount() - 1); var6 > this.path.getNextNodeIndex(); --var6) {
                var5 = this.path.getEntityPosAtNode(this.mob, var6);
                if (!(var5.distanceToSqr(var0) > 36.0) && this.canMoveDirectly(var0, var5, 0, 0, 0)) {
                    this.path.setNextNodeIndex(var6);
                    break;
                }
            }

            this.doStuckDetection(var0);
        }
    }

    @Override
    protected void doStuckDetection(Vec3 param0) {
        if (this.tick - this.lastStuckCheck > 100) {
            if (param0.distanceToSqr(this.lastStuckCheckPos) < 2.25) {
                this.stop();
            }

            this.lastStuckCheck = this.tick;
            this.lastStuckCheckPos = param0;
        }

        if (this.path != null && !this.path.isDone()) {
            Vec3i var0 = this.path.getNextNodePos();
            if (var0.equals(this.timeoutCachedNode)) {
                this.timeoutTimer += Util.getMillis() - this.lastTimeoutCheck;
            } else {
                this.timeoutCachedNode = var0;
                double var1 = param0.distanceTo(Vec3.atCenterOf(this.timeoutCachedNode));
                this.timeoutLimit = this.mob.getSpeed() > 0.0F ? var1 / (double)this.mob.getSpeed() * 100.0 : 0.0;
            }

            if (this.timeoutLimit > 0.0 && (double)this.timeoutTimer > this.timeoutLimit * 2.0) {
                this.timeoutCachedNode = Vec3i.ZERO;
                this.timeoutTimer = 0L;
                this.timeoutLimit = 0.0;
                this.stop();
            }

            this.lastTimeoutCheck = Util.getMillis();
        }

    }

    @Override
    protected boolean canMoveDirectly(Vec3 param0, Vec3 param1, int param2, int param3, int param4) {
        Vec3 var0 = new Vec3(param1.x, param1.y + (double)this.mob.getBbHeight() * 0.5, param1.z);
        return this.level.clip(new ClipContext(param0, var0, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob)).getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean isStableDestination(BlockPos param0) {
        return !this.level.getBlockState(param0).isSolidRender(this.level, param0);
    }

    @Override
    public void setCanFloat(boolean param0) {
    }
}

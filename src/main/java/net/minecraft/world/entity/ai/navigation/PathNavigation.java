package net.minecraft.world.entity.ai.navigation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public abstract class PathNavigation {
    private static final int MAX_TIME_RECOMPUTE = 20;
    protected final Mob mob;
    protected final Level level;
    @Nullable
    protected Path path;
    protected double speedModifier;
    protected int tick;
    protected int lastStuckCheck;
    protected Vec3 lastStuckCheckPos = Vec3.ZERO;
    protected Vec3i timeoutCachedNode = Vec3i.ZERO;
    protected long timeoutTimer;
    protected long lastTimeoutCheck;
    protected double timeoutLimit;
    protected float maxDistanceToWaypoint = 0.5F;
    protected boolean hasDelayedRecomputation;
    protected long timeLastRecompute;
    protected NodeEvaluator nodeEvaluator;
    private BlockPos targetPos;
    private int reachRange;
    private float maxVisitedNodesMultiplier = 1.0F;
    private final PathFinder pathFinder;
    private boolean isStuck;

    public PathNavigation(Mob param0, Level param1) {
        this.mob = param0;
        this.level = param1;
        int var0 = Mth.floor(param0.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0);
        this.pathFinder = this.createPathFinder(var0);
    }

    public void resetMaxVisitedNodesMultiplier() {
        this.maxVisitedNodesMultiplier = 1.0F;
    }

    public void setMaxVisitedNodesMultiplier(float param0) {
        this.maxVisitedNodesMultiplier = param0;
    }

    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    protected abstract PathFinder createPathFinder(int var1);

    public void setSpeedModifier(double param0) {
        this.speedModifier = param0;
    }

    public boolean hasDelayedRecomputation() {
        return this.hasDelayedRecomputation;
    }

    public void recomputePath() {
        if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
            if (this.targetPos != null) {
                this.path = null;
                this.path = this.createPath(this.targetPos, this.reachRange);
                this.timeLastRecompute = this.level.getGameTime();
                this.hasDelayedRecomputation = false;
            }
        } else {
            this.hasDelayedRecomputation = true;
        }

    }

    @Nullable
    public final Path createPath(double param0, double param1, double param2, int param3) {
        return this.createPath(new BlockPos(param0, param1, param2), param3);
    }

    @Nullable
    public Path createPath(Stream<BlockPos> param0, int param1) {
        return this.createPath(param0.collect(Collectors.toSet()), 8, false, param1);
    }

    @Nullable
    public Path createPath(Set<BlockPos> param0, int param1) {
        return this.createPath(param0, 8, false, param1);
    }

    @Nullable
    public Path createPath(BlockPos param0, int param1) {
        return this.createPath(ImmutableSet.of(param0), 8, false, param1);
    }

    @Nullable
    public Path createPath(BlockPos param0, int param1, int param2) {
        return this.createPath(ImmutableSet.of(param0), 8, false, param1, (float)param2);
    }

    @Nullable
    public Path createPath(Entity param0, int param1) {
        return this.createPath(ImmutableSet.of(param0.blockPosition()), 16, true, param1);
    }

    @Nullable
    protected Path createPath(Set<BlockPos> param0, int param1, boolean param2, int param3) {
        return this.createPath(param0, param1, param2, param3, (float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
    }

    @Nullable
    protected Path createPath(Set<BlockPos> param0, int param1, boolean param2, int param3, float param4) {
        if (param0.isEmpty()) {
            return null;
        } else if (this.mob.getY() < (double)this.level.getMinBuildHeight()) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && param0.contains(this.targetPos)) {
            return this.path;
        } else {
            this.level.getProfiler().push("pathfind");
            BlockPos var0 = param2 ? this.mob.blockPosition().above() : this.mob.blockPosition();
            int var1 = (int)(param4 + (float)param1);
            PathNavigationRegion var2 = new PathNavigationRegion(this.level, var0.offset(-var1, -var1, -var1), var0.offset(var1, var1, var1));
            Path var3 = this.pathFinder.findPath(var2, this.mob, param0, param4, param3, this.maxVisitedNodesMultiplier);
            this.level.getProfiler().pop();
            if (var3 != null && var3.getTarget() != null) {
                this.targetPos = var3.getTarget();
                this.reachRange = param3;
                this.resetStuckTimeout();
            }

            return var3;
        }
    }

    public boolean moveTo(double param0, double param1, double param2, double param3) {
        return this.moveTo(this.createPath(param0, param1, param2, 1), param3);
    }

    public boolean moveTo(Entity param0, double param1) {
        Path var0 = this.createPath(param0, 1);
        return var0 != null && this.moveTo(var0, param1);
    }

    public boolean moveTo(@Nullable Path param0, double param1) {
        if (param0 == null) {
            this.path = null;
            return false;
        } else {
            if (!param0.sameAs(this.path)) {
                this.path = param0;
            }

            if (this.isDone()) {
                return false;
            } else {
                this.trimPath();
                if (this.path.getNodeCount() <= 0) {
                    return false;
                } else {
                    this.speedModifier = param1;
                    Vec3 var0 = this.getTempMobPos();
                    this.lastStuckCheck = this.tick;
                    this.lastStuckCheckPos = var0;
                    return true;
                }
            }
        }
    }

    @Nullable
    public Path getPath() {
        return this.path;
    }

    public void tick() {
        ++this.tick;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                Vec3 var0 = this.getTempMobPos();
                Vec3 var1 = this.path.getNextEntityPos(this.mob);
                if (var0.y > var1.y && !this.mob.isOnGround() && Mth.floor(var0.x) == Mth.floor(var1.x) && Mth.floor(var0.z) == Mth.floor(var1.z)) {
                    this.path.advance();
                }
            }

            DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
            if (!this.isDone()) {
                Vec3 var2 = this.path.getNextEntityPos(this.mob);
                BlockPos var3 = new BlockPos(var2);
                this.mob
                    .getMoveControl()
                    .setWantedPosition(
                        var2.x,
                        this.level.getBlockState(var3.below()).isAir() ? var2.y : WalkNodeEvaluator.getFloorLevel(this.level, var3),
                        var2.z,
                        this.speedModifier
                    );
            }
        }
    }

    protected void followThePath() {
        Vec3 var0 = this.getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        Vec3i var1 = this.path.getNextNodePos();
        double var2 = Math.abs(this.mob.getX() - ((double)var1.getX() + 0.5));
        double var3 = Math.abs(this.mob.getY() - (double)var1.getY());
        double var4 = Math.abs(this.mob.getZ() - ((double)var1.getZ() + 0.5));
        boolean var5 = var2 < (double)this.maxDistanceToWaypoint && var4 < (double)this.maxDistanceToWaypoint && var3 < 1.0;
        if (var5 || this.mob.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(var0)) {
            this.path.advance();
        }

        this.doStuckDetection(var0);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 param0) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vec3 var0 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
            if (!param0.closerThan(var0, 2.0)) {
                return false;
            } else {
                Vec3 var1 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec3 var2 = var1.subtract(var0);
                Vec3 var3 = param0.subtract(var0);
                return var2.dot(var3) > 0.0;
            }
        }
    }

    protected void doStuckDetection(Vec3 param0) {
        if (this.tick - this.lastStuckCheck > 100) {
            if (param0.distanceToSqr(this.lastStuckCheckPos) < 2.25) {
                this.isStuck = true;
                this.stop();
            } else {
                this.isStuck = false;
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
                double var1 = param0.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
                this.timeoutLimit = this.mob.getSpeed() > 0.0F ? var1 / (double)this.mob.getSpeed() * 1000.0 : 0.0;
            }

            if (this.timeoutLimit > 0.0 && (double)this.timeoutTimer > this.timeoutLimit * 3.0) {
                this.timeoutPath();
            }

            this.lastTimeoutCheck = Util.getMillis();
        }

    }

    private void timeoutPath() {
        this.resetStuckTimeout();
        this.stop();
    }

    private void resetStuckTimeout() {
        this.timeoutCachedNode = Vec3i.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0;
        this.isStuck = false;
    }

    public boolean isDone() {
        return this.path == null || this.path.isDone();
    }

    public boolean isInProgress() {
        return !this.isDone();
    }

    public void stop() {
        this.path = null;
    }

    protected abstract Vec3 getTempMobPos();

    protected abstract boolean canUpdatePath();

    protected boolean isInLiquid() {
        return this.mob.isInWaterOrBubble() || this.mob.isInLava();
    }

    protected void trimPath() {
        if (this.path != null) {
            for(int var0 = 0; var0 < this.path.getNodeCount(); ++var0) {
                Node var1 = this.path.getNode(var0);
                Node var2 = var0 + 1 < this.path.getNodeCount() ? this.path.getNode(var0 + 1) : null;
                BlockState var3 = this.level.getBlockState(new BlockPos(var1.x, var1.y, var1.z));
                if (var3.is(BlockTags.CAULDRONS)) {
                    this.path.replaceNode(var0, var1.cloneAndMove(var1.x, var1.y + 1, var1.z));
                    if (var2 != null && var1.y >= var2.y) {
                        this.path.replaceNode(var0 + 1, var1.cloneAndMove(var2.x, var1.y + 1, var2.z));
                    }
                }
            }

        }
    }

    protected abstract boolean canMoveDirectly(Vec3 var1, Vec3 var2, int var3, int var4, int var5);

    public boolean isStableDestination(BlockPos param0) {
        BlockPos var0 = param0.below();
        return this.level.getBlockState(var0).isSolidRender(this.level, var0);
    }

    public NodeEvaluator getNodeEvaluator() {
        return this.nodeEvaluator;
    }

    public void setCanFloat(boolean param0) {
        this.nodeEvaluator.setCanFloat(param0);
    }

    public boolean canFloat() {
        return this.nodeEvaluator.canFloat();
    }

    public void recomputePath(BlockPos param0) {
        if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
            Node var0 = this.path.getEndNode();
            Vec3 var1 = new Vec3(((double)var0.x + this.mob.getX()) / 2.0, ((double)var0.y + this.mob.getY()) / 2.0, ((double)var0.z + this.mob.getZ()) / 2.0);
            if (param0.closerThan(var1, (double)(this.path.getNodeCount() - this.path.getNextNodeIndex()))) {
                this.recomputePath();
            }

        }
    }

    public float getMaxDistanceToWaypoint() {
        return this.maxDistanceToWaypoint;
    }

    public boolean isStuck() {
        return this.isStuck;
    }
}

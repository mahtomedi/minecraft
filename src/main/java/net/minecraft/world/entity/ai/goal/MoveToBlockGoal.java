package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelReader;

public abstract class MoveToBlockGoal extends Goal {
    private static final int GIVE_UP_TICKS = 1200;
    private static final int STAY_TICKS = 1200;
    private static final int INTERVAL_TICKS = 200;
    protected final PathfinderMob mob;
    public final double speedModifier;
    protected int nextStartTick;
    protected int tryTicks;
    private int maxStayTicks;
    protected BlockPos blockPos = BlockPos.ZERO;
    private boolean reachedTarget;
    private final int searchRange;
    private final int verticalSearchRange;
    protected int verticalSearchStart;

    public MoveToBlockGoal(PathfinderMob param0, double param1, int param2) {
        this(param0, param1, param2, 1);
    }

    public MoveToBlockGoal(PathfinderMob param0, double param1, int param2, int param3) {
        this.mob = param0;
        this.speedModifier = param1;
        this.searchRange = param2;
        this.verticalSearchStart = 0;
        this.verticalSearchRange = param3;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        } else {
            this.nextStartTick = this.nextStartTick(this.mob);
            return this.findNearestBlock();
        }
    }

    protected int nextStartTick(PathfinderMob param0) {
        return 200 + param0.getRandom().nextInt(200);
    }

    @Override
    public boolean canContinueToUse() {
        return this.tryTicks >= -this.maxStayTicks && this.tryTicks <= 1200 && this.isValidTarget(this.mob.level, this.blockPos);
    }

    @Override
    public void start() {
        this.moveMobToBlock();
        this.tryTicks = 0;
        this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
    }

    protected void moveMobToBlock() {
        this.mob
            .getNavigation()
            .moveTo(
                (double)((float)this.blockPos.getX()) + 0.5,
                (double)(this.blockPos.getY() + 1),
                (double)((float)this.blockPos.getZ()) + 0.5,
                this.speedModifier
            );
    }

    public double acceptedDistance() {
        return 1.0;
    }

    protected BlockPos getMoveToTarget() {
        return this.blockPos.above();
    }

    @Override
    public void tick() {
        BlockPos var0 = this.getMoveToTarget();
        if (!var0.closerThan(this.mob.position(), this.acceptedDistance())) {
            this.reachedTarget = false;
            ++this.tryTicks;
            if (this.shouldRecalculatePath()) {
                this.mob
                    .getNavigation()
                    .moveTo((double)((float)var0.getX()) + 0.5, (double)var0.getY(), (double)((float)var0.getZ()) + 0.5, this.speedModifier);
            }
        } else {
            this.reachedTarget = true;
            --this.tryTicks;
        }

    }

    public boolean shouldRecalculatePath() {
        return this.tryTicks % 40 == 0;
    }

    protected boolean isReachedTarget() {
        return this.reachedTarget;
    }

    protected boolean findNearestBlock() {
        int var0 = this.searchRange;
        int var1 = this.verticalSearchRange;
        BlockPos var2 = this.mob.blockPosition();
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

        for(int var4 = this.verticalSearchStart; var4 <= var1; var4 = var4 > 0 ? -var4 : 1 - var4) {
            for(int var5 = 0; var5 < var0; ++var5) {
                for(int var6 = 0; var6 <= var5; var6 = var6 > 0 ? -var6 : 1 - var6) {
                    for(int var7 = var6 < var5 && var6 > -var5 ? var5 : 0; var7 <= var5; var7 = var7 > 0 ? -var7 : 1 - var7) {
                        var3.setWithOffset(var2, var6, var4 - 1, var7);
                        if (this.mob.isWithinRestriction(var3) && this.isValidTarget(this.mob.level, var3)) {
                            this.blockPos = var3;
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    protected abstract boolean isValidTarget(LevelReader var1, BlockPos var2);
}

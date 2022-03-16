package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.frog.Frog;

public class Croak extends Behavior<Frog> {
    private static final int CROAK_TICKS = 40;
    private static final int TIME_OUT_DURATION = 100;
    private int croakCounter;

    public Croak() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 100);
    }

    protected boolean canStillUse(ServerLevel param0, Frog param1, long param2) {
        return this.croakCounter < 40;
    }

    protected void start(ServerLevel param0, Frog param1, long param2) {
        if (!param1.isInWaterOrBubble() && !param1.isInLava()) {
            param1.setPose(Pose.CROAKING);
            this.croakCounter = 0;
        }
    }

    protected void stop(ServerLevel param0, Frog param1, long param2) {
        param1.setPose(Pose.STANDING);
    }

    protected void tick(ServerLevel param0, Frog param1, long param2) {
        ++this.croakCounter;
    }
}

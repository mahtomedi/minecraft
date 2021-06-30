package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll extends Behavior<PathfinderMob> {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;
    private final float speedModifier;
    protected final int maxHorizontalDistance;
    protected final int maxVerticalDistance;
    private final boolean mayStrollFromWater;

    public RandomStroll(float param0) {
        this(param0, true);
    }

    public RandomStroll(float param0, boolean param1) {
        this(param0, 10, 7, param1);
    }

    public RandomStroll(float param0, int param1, int param2) {
        this(param0, param1, param2, true);
    }

    public RandomStroll(float param0, int param1, int param2, boolean param3) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = param0;
        this.maxHorizontalDistance = param1;
        this.maxVerticalDistance = param2;
        this.mayStrollFromWater = param3;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return this.mayStrollFromWater || !param1.isInWaterOrBubble();
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        Optional<Vec3> var0 = Optional.ofNullable(this.getTargetPos(param1));
        param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var0.map(param0x -> new WalkTarget(param0x, this.speedModifier, 0)));
    }

    @Nullable
    protected Vec3 getTargetPos(PathfinderMob param0) {
        return LandRandomPos.getPos(param0, this.maxHorizontalDistance, this.maxVerticalDistance);
    }
}

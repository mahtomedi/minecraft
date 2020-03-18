package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll extends Behavior<PathfinderMob> {
    private final float speedModifier;
    private final int maxHorizontalDistance;
    private final int maxVerticalDistance;

    public RandomStroll(float param0) {
        this(param0, 10, 7);
    }

    public RandomStroll(float param0, int param1, int param2) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = param0;
        this.maxHorizontalDistance = param1;
        this.maxVerticalDistance = param2;
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        Optional<Vec3> var0 = Optional.ofNullable(RandomPos.getLandPos(param1, this.maxHorizontalDistance, this.maxVerticalDistance));
        param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var0.map(param0x -> new WalkTarget(param0x, this.speedModifier, 0)));
    }
}

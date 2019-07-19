package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class StrollAroundPoi extends Behavior<PathfinderMob> {
    private final MemoryModuleType<GlobalPos> memoryType;
    private long nextOkStartTime;
    private final int maxDistanceFromPoi;

    public StrollAroundPoi(MemoryModuleType<GlobalPos> param0, int param1) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, param0, MemoryStatus.VALUE_PRESENT));
        this.memoryType = param0;
        this.maxDistanceFromPoi = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        Optional<GlobalPos> var0 = param1.getBrain().getMemory(this.memoryType);
        return var0.isPresent()
            && Objects.equals(param0.getDimension().getType(), var0.get().dimension())
            && var0.get().pos().closerThan(param1.position(), (double)this.maxDistanceFromPoi);
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        if (param2 > this.nextOkStartTime) {
            Optional<Vec3> var0 = Optional.ofNullable(RandomPos.getLandPos(param1, 8, 6));
            param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var0.map(param0x -> new WalkTarget(param0x, 0.4F, 1)));
            this.nextOkStartTime = param2 + 180L;
        }

    }
}

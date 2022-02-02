package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StrollToPoi extends Behavior<PathfinderMob> {
    private final MemoryModuleType<GlobalPos> memoryType;
    private final int closeEnoughDist;
    private final int maxDistanceFromPoi;
    private final float speedModifier;
    private long nextOkStartTime;

    public StrollToPoi(MemoryModuleType<GlobalPos> param0, float param1, int param2, int param3) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, param0, MemoryStatus.VALUE_PRESENT));
        this.memoryType = param0;
        this.speedModifier = param1;
        this.closeEnoughDist = param2;
        this.maxDistanceFromPoi = param3;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        Optional<GlobalPos> var0 = param1.getBrain().getMemory(this.memoryType);
        return var0.isPresent()
            && param0.dimension() == var0.get().dimension()
            && var0.get().pos().closerToCenterThan(param1.position(), (double)this.maxDistanceFromPoi);
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        if (param2 > this.nextOkStartTime) {
            Brain<?> var0 = param1.getBrain();
            Optional<GlobalPos> var1 = var0.getMemory(this.memoryType);
            var1.ifPresent(param1x -> var0.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(param1x.pos(), this.speedModifier, this.closeEnoughDist)));
            this.nextOkStartTime = param2 + 80L;
        }

    }
}

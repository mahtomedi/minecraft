package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InsideBrownianWalk extends Behavior<PathfinderMob> {
    private final float speedModifier;

    public InsideBrownianWalk(float param0) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return !param0.canSeeSky(param1.blockPosition());
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        BlockPos var0 = param1.blockPosition();
        List<BlockPos> var1 = BlockPos.betweenClosedStream(var0.offset(-1, -1, -1), var0.offset(1, 1, 1)).map(BlockPos::immutable).collect(Collectors.toList());
        Collections.shuffle(var1);
        Optional<BlockPos> var2 = var1.stream()
            .filter(param1x -> !param0.canSeeSky(param1x))
            .filter(param2x -> param0.loadedAndEntityCanStandOn(param2x, param1))
            .filter(param2x -> param0.noCollision(param1))
            .findFirst();
        var2.ifPresent(param1x -> param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(param1x, this.speedModifier, 0)));
    }
}

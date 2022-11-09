package net.minecraft.world.entity.ai.behavior;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InsideBrownianWalk {
    public static BehaviorControl<PathfinderMob> create(float param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor>group(param1.absent(MemoryModuleType.WALK_TARGET))
                    .apply(
                        param1,
                        param1x -> (param2, param3, param4) -> {
                                if (param2.canSeeSky(param3.blockPosition())) {
                                    return false;
                                } else {
                                    BlockPos var0x = param3.blockPosition();
                                    List<BlockPos> var1x = BlockPos.betweenClosedStream(var0x.offset(-1, -1, -1), var0x.offset(1, 1, 1))
                                        .map(BlockPos::immutable)
                                        .collect(Collectors.toList());
                                    Collections.shuffle(var1x);
                                    var1x.stream()
                                        .filter(param1xxx -> !param2.canSeeSky(param1xxx))
                                        .filter(param2x -> param2.loadedAndEntityCanStandOn(param2x, param3))
                                        .filter(param2x -> param2.noCollision(param3))
                                        .findFirst()
                                        .ifPresent(param2x -> param1x.set(new WalkTarget(param2x, param0, 0)));
                                    return true;
                                }
                            }
                    )
        );
    }
}

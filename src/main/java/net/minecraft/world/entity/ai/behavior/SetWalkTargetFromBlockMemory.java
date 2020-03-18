package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetFromBlockMemory extends Behavior<Villager> {
    private final MemoryModuleType<GlobalPos> memoryType;
    private final float speedModifier;
    private final int closeEnoughDist;
    private final int tooFarDistance;
    private final int tooLongUnreachableDuration;

    public SetWalkTargetFromBlockMemory(MemoryModuleType<GlobalPos> param0, float param1, int param2, int param3, int param4) {
        super(
            ImmutableMap.of(
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                param0,
                MemoryStatus.VALUE_PRESENT
            )
        );
        this.memoryType = param0;
        this.speedModifier = param1;
        this.closeEnoughDist = param2;
        this.tooFarDistance = param3;
        this.tooLongUnreachableDuration = param4;
    }

    private void dropPOI(Villager param0, long param1) {
        Brain<?> var0 = param0.getBrain();
        param0.releasePoi(this.memoryType);
        var0.eraseMemory(this.memoryType);
        var0.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, param1);
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        var0.getMemory(this.memoryType)
            .ifPresent(
                param4 -> {
                    if (this.tiredOfTryingToFindTarget(param0, param1)) {
                        this.dropPOI(param1, param2);
                    } else if (this.tooFar(param0, param1, param4)) {
                        Vec3 var0x = null;
                        int var1x = 0;
        
                        for(int var2x = 1000;
                            var1x < 1000 && (var0x == null || this.tooFar(param0, param1, GlobalPos.of(param1.dimension, new BlockPos(var0x))));
                            ++var1x
                        ) {
                            var0x = RandomPos.getPosTowards(param1, 15, 7, Vec3.atBottomCenterOf(param4.pos()));
                        }
        
                        if (var1x == 1000) {
                            this.dropPOI(param1, param2);
                            return;
                        }
        
                        var0.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var0x, this.speedModifier, this.closeEnoughDist));
                    } else if (!this.closeEnough(param0, param1, param4)) {
                        var0.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(param4.pos(), this.speedModifier, this.closeEnoughDist));
                    }
        
                }
            );
    }

    private boolean tiredOfTryingToFindTarget(ServerLevel param0, Villager param1) {
        Optional<Long> var0 = param1.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        if (var0.isPresent()) {
            return param0.getGameTime() - var0.get() > (long)this.tooLongUnreachableDuration;
        } else {
            return false;
        }
    }

    private boolean tooFar(ServerLevel param0, Villager param1, GlobalPos param2) {
        return param2.dimension() != param0.getDimension().getType() || param2.pos().distManhattan(param1.blockPosition()) > this.tooFarDistance;
    }

    private boolean closeEnough(ServerLevel param0, Villager param1, GlobalPos param2) {
        return param2.dimension() == param0.getDimension().getType() && param2.pos().distManhattan(param1.blockPosition()) <= this.closeEnoughDist;
    }
}

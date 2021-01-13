package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class VillageBoundRandomStroll extends Behavior<PathfinderMob> {
    private final float speedModifier;
    private final int maxXyDist;
    private final int maxYDist;

    public VillageBoundRandomStroll(float param0) {
        this(param0, 10, 7);
    }

    public VillageBoundRandomStroll(float param0, int param1, int param2) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = param0;
        this.maxXyDist = param1;
        this.maxYDist = param2;
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        BlockPos var0 = param1.blockPosition();
        if (param0.isVillage(var0)) {
            this.setRandomPos(param1);
        } else {
            SectionPos var1 = SectionPos.of(var0);
            SectionPos var2 = BehaviorUtils.findSectionClosestToVillage(param0, var1, 2);
            if (var2 != var1) {
                this.setTargetedPos(param1, var2);
            } else {
                this.setRandomPos(param1);
            }
        }

    }

    private void setTargetedPos(PathfinderMob param0, SectionPos param1) {
        Optional<Vec3> var0 = Optional.ofNullable(RandomPos.getPosTowards(param0, this.maxXyDist, this.maxYDist, Vec3.atBottomCenterOf(param1.center())));
        param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var0.map(param0x -> new WalkTarget(param0x, this.speedModifier, 0)));
    }

    private void setRandomPos(PathfinderMob param0) {
        Optional<Vec3> var0 = Optional.ofNullable(RandomPos.getLandPos(param0, this.maxXyDist, this.maxYDist));
        param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var0.map(param0x -> new WalkTarget(param0x, this.speedModifier, 0)));
    }
}

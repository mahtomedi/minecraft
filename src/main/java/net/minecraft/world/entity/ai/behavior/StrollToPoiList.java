package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;

public class StrollToPoiList extends Behavior<Villager> {
    private final MemoryModuleType<List<GlobalPos>> strollToMemoryType;
    private final MemoryModuleType<GlobalPos> mustBeCloseToMemoryType;
    private final float speedModifier;
    private final int closeEnoughDist;
    private final int maxDistanceFromPoi;
    private long nextOkStartTime;
    @Nullable
    private GlobalPos targetPos;

    public StrollToPoiList(MemoryModuleType<List<GlobalPos>> param0, float param1, int param2, int param3, MemoryModuleType<GlobalPos> param4) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, param0, MemoryStatus.VALUE_PRESENT, param4, MemoryStatus.VALUE_PRESENT));
        this.strollToMemoryType = param0;
        this.speedModifier = param1;
        this.closeEnoughDist = param2;
        this.maxDistanceFromPoi = param3;
        this.mustBeCloseToMemoryType = param4;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        Optional<List<GlobalPos>> var0 = param1.getBrain().getMemory(this.strollToMemoryType);
        Optional<GlobalPos> var1 = param1.getBrain().getMemory(this.mustBeCloseToMemoryType);
        if (var0.isPresent() && var1.isPresent()) {
            List<GlobalPos> var2 = var0.get();
            if (!var2.isEmpty()) {
                this.targetPos = var2.get(param0.getRandom().nextInt(var2.size()));
                return this.targetPos != null
                    && param0.dimension() == this.targetPos.dimension()
                    && var1.get().pos().closerThan(param1.position(), (double)this.maxDistanceFromPoi);
            }
        }

        return false;
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        if (param2 > this.nextOkStartTime && this.targetPos != null) {
            param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos.pos(), this.speedModifier, this.closeEnoughDist));
            this.nextOkStartTime = param2 + 100L;
        }

    }
}

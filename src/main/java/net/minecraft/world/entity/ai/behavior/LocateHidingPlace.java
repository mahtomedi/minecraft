package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public class LocateHidingPlace extends Behavior<LivingEntity> {
    private final float speed;
    private final int radius;
    private final int closeEnoughDist;
    private Optional<BlockPos> currentPos = Optional.empty();

    public LocateHidingPlace(int param0, float param1, int param2) {
        super(
            ImmutableMap.of(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HOME,
                MemoryStatus.REGISTERED,
                MemoryModuleType.HIDING_PLACE,
                MemoryStatus.REGISTERED
            )
        );
        this.radius = param0;
        this.speed = param1;
        this.closeEnoughDist = param2;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        Optional<BlockPos> var0 = param0.getPoiManager()
            .find(param0x -> param0x == PoiType.HOME, param0x -> true, new BlockPos(param1), this.closeEnoughDist + 1, PoiManager.Occupancy.ANY);
        if (var0.isPresent() && var0.get().closerThan(param1.position(), (double)this.closeEnoughDist)) {
            this.currentPos = var0;
        } else {
            this.currentPos = Optional.empty();
        }

        return true;
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        Optional<BlockPos> var1 = this.currentPos;
        if (!var1.isPresent()) {
            var1 = param0.getPoiManager()
                .getRandom(param0x -> param0x == PoiType.HOME, param0x -> true, PoiManager.Occupancy.ANY, new BlockPos(param1), this.radius, param1.getRandom());
            if (!var1.isPresent()) {
                Optional<GlobalPos> var2 = var0.getMemory(MemoryModuleType.HOME);
                if (var2.isPresent()) {
                    var1 = Optional.of(var2.get().pos());
                }
            }
        }

        if (var1.isPresent()) {
            var0.eraseMemory(MemoryModuleType.PATH);
            var0.eraseMemory(MemoryModuleType.LOOK_TARGET);
            var0.eraseMemory(MemoryModuleType.BREED_TARGET);
            var0.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
            var0.setMemory(MemoryModuleType.HIDING_PLACE, GlobalPos.of(param0.getDimension().getType(), var1.get()));
            if (!var1.get().closerThan(param1.position(), (double)this.closeEnoughDist)) {
                var0.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var1.get(), this.speed, this.closeEnoughDist));
            }
        }

    }
}

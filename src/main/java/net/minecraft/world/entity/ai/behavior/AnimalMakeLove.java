package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

public class AnimalMakeLove extends Behavior<Animal> {
    private static final int BREED_RANGE = 3;
    private static final int MIN_DURATION = 60;
    private static final int MAX_DURATION = 110;
    private final EntityType<? extends Animal> partnerType;
    private final float speedModifier;
    private long spawnChildAtTime;

    public AnimalMakeLove(EntityType<? extends Animal> param0, float param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.BREED_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.IS_PANICKING,
                MemoryStatus.VALUE_ABSENT
            ),
            110
        );
        this.partnerType = param0;
        this.speedModifier = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Animal param1) {
        return param1.isInLove() && this.findValidBreedPartner(param1).isPresent();
    }

    protected void start(ServerLevel param0, Animal param1, long param2) {
        Animal var0 = this.findValidBreedPartner(param1).get();
        param1.getBrain().setMemory(MemoryModuleType.BREED_TARGET, var0);
        var0.getBrain().setMemory(MemoryModuleType.BREED_TARGET, param1);
        BehaviorUtils.lockGazeAndWalkToEachOther(param1, var0, this.speedModifier);
        int var1 = 60 + param1.getRandom().nextInt(50);
        this.spawnChildAtTime = param2 + (long)var1;
    }

    protected boolean canStillUse(ServerLevel param0, Animal param1, long param2) {
        if (!this.hasBreedTargetOfRightType(param1)) {
            return false;
        } else {
            Animal var0 = this.getBreedTarget(param1);
            return var0.isAlive()
                && param1.canMate(var0)
                && BehaviorUtils.entityIsVisible(param1.getBrain(), var0)
                && param2 <= this.spawnChildAtTime
                && !param1.isPanicking()
                && !var0.isPanicking();
        }
    }

    protected void tick(ServerLevel param0, Animal param1, long param2) {
        Animal var0 = this.getBreedTarget(param1);
        BehaviorUtils.lockGazeAndWalkToEachOther(param1, var0, this.speedModifier);
        if (param1.closerThan(var0, 3.0)) {
            if (param2 >= this.spawnChildAtTime) {
                param1.spawnChildFromBreeding(param0, var0);
                param1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
                var0.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
            }

        }
    }

    protected void stop(ServerLevel param0, Animal param1, long param2) {
        param1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        param1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        param1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        this.spawnChildAtTime = 0L;
    }

    private Animal getBreedTarget(Animal param0) {
        return (Animal)param0.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
    }

    private boolean hasBreedTargetOfRightType(Animal param0) {
        Brain<?> var0 = param0.getBrain();
        return var0.hasMemoryValue(MemoryModuleType.BREED_TARGET) && var0.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType;
    }

    private Optional<? extends Animal> findValidBreedPartner(Animal param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findClosest(param1 -> {
            if (param1.getType() == this.partnerType && param1 instanceof Animal var0 && param0.canMate(var0) && !var0.isPanicking()) {
                return true;
            }

            return false;
        }).map(Animal.class::cast);
    }
}

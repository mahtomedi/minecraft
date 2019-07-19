package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.pathfinder.Path;

public class MakeLove extends Behavior<Villager> {
    private long birthTimestamp;

    public MakeLove() {
        super(
            ImmutableMap.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT),
            350,
            350
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        return this.isBreedingPossible(param1);
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return param2 <= this.birthTimestamp && this.isBreedingPossible(param1);
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        Villager var0 = this.getBreedingTarget(param1);
        BehaviorUtils.lockGazeAndWalkToEachOther(param1, var0);
        param0.broadcastEntityEvent(var0, (byte)18);
        param0.broadcastEntityEvent(param1, (byte)18);
        int var1 = 275 + param1.getRandom().nextInt(50);
        this.birthTimestamp = param2 + (long)var1;
    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        Villager var0 = this.getBreedingTarget(param1);
        if (!(param1.distanceToSqr(var0) > 5.0)) {
            BehaviorUtils.lockGazeAndWalkToEachOther(param1, var0);
            if (param2 >= this.birthTimestamp) {
                param1.eatAndDigestFood();
                var0.eatAndDigestFood();
                this.tryToGiveBirth(param0, param1, var0);
            } else if (param1.getRandom().nextInt(35) == 0) {
                param0.broadcastEntityEvent(var0, (byte)12);
                param0.broadcastEntityEvent(param1, (byte)12);
            }

        }
    }

    private void tryToGiveBirth(ServerLevel param0, Villager param1, Villager param2) {
        Optional<BlockPos> var0 = this.takeVacantBed(param0, param1);
        if (!var0.isPresent()) {
            param0.broadcastEntityEvent(param2, (byte)13);
            param0.broadcastEntityEvent(param1, (byte)13);
        } else {
            Optional<Villager> var1 = this.breed(param1, param2);
            if (var1.isPresent()) {
                this.giveBedToChild(param0, var1.get(), var0.get());
            } else {
                param0.getPoiManager().release(var0.get());
            }
        }

    }

    protected void stop(ServerLevel param0, Villager param1, long param2) {
        param1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
    }

    private Villager getBreedingTarget(Villager param0) {
        return param0.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
    }

    private boolean isBreedingPossible(Villager param0) {
        Brain<Villager> var0 = param0.getBrain();
        if (!var0.getMemory(MemoryModuleType.BREED_TARGET).isPresent()) {
            return false;
        } else {
            Villager var1 = this.getBreedingTarget(param0);
            return BehaviorUtils.targetIsValid(var0, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && param0.canBreed() && var1.canBreed();
        }
    }

    private Optional<BlockPos> takeVacantBed(ServerLevel param0, Villager param1) {
        return param0.getPoiManager().take(PoiType.HOME.getPredicate(), param1x -> this.canReach(param1, param1x), new BlockPos(param1), 48);
    }

    private boolean canReach(Villager param0, BlockPos param1) {
        Path var0 = param0.getNavigation().createPath(param1, PoiType.HOME.getValidRange());
        return var0 != null && var0.canReach();
    }

    private Optional<Villager> breed(Villager param0, Villager param1) {
        Villager var0 = param0.getBreedOffspring(param1);
        if (var0 == null) {
            return Optional.empty();
        } else {
            param0.setAge(6000);
            param1.setAge(6000);
            var0.setAge(-24000);
            var0.moveTo(param0.x, param0.y, param0.z, 0.0F, 0.0F);
            param0.level.addFreshEntity(var0);
            param0.level.broadcastEntityEvent(var0, (byte)12);
            return Optional.of(var0);
        }
    }

    private void giveBedToChild(ServerLevel param0, Villager param1, BlockPos param2) {
        GlobalPos var0 = GlobalPos.of(param0.getDimension().getType(), param2);
        param1.getBrain().setMemory(MemoryModuleType.HOME, var0);
    }
}

package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.pathfinder.Path;

public class VillagerMakeLove extends Behavior<Villager> {
    private static final int INTERACT_DIST_SQR = 5;
    private static final float SPEED_MODIFIER = 0.5F;
    private long birthTimestamp;

    public VillagerMakeLove() {
        super(
            ImmutableMap.of(
                MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
            ),
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
        AgeableMob var0 = param1.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
        BehaviorUtils.lockGazeAndWalkToEachOther(param1, var0, 0.5F);
        param0.broadcastEntityEvent(var0, (byte)18);
        param0.broadcastEntityEvent(param1, (byte)18);
        int var1 = 275 + param1.getRandom().nextInt(50);
        this.birthTimestamp = param2 + (long)var1;
    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        Villager var0 = (Villager)param1.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
        if (!(param1.distanceToSqr(var0) > 5.0)) {
            BehaviorUtils.lockGazeAndWalkToEachOther(param1, var0, 0.5F);
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
        if (var0.isEmpty()) {
            param0.broadcastEntityEvent(param2, (byte)13);
            param0.broadcastEntityEvent(param1, (byte)13);
        } else {
            Optional<Villager> var1 = this.breed(param0, param1, param2);
            if (var1.isPresent()) {
                this.giveBedToChild(param0, var1.get(), var0.get());
            } else {
                param0.getPoiManager().release(var0.get());
                DebugPackets.sendPoiTicketCountPacket(param0, var0.get());
            }
        }

    }

    protected void stop(ServerLevel param0, Villager param1, long param2) {
        param1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
    }

    private boolean isBreedingPossible(Villager param0) {
        Brain<Villager> var0 = param0.getBrain();
        Optional<AgeableMob> var1 = var0.getMemory(MemoryModuleType.BREED_TARGET).filter(param0x -> param0x.getType() == EntityType.VILLAGER);
        if (var1.isEmpty()) {
            return false;
        } else {
            return BehaviorUtils.targetIsValid(var0, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && param0.canBreed() && var1.get().canBreed();
        }
    }

    private Optional<BlockPos> takeVacantBed(ServerLevel param0, Villager param1) {
        return param0.getPoiManager()
            .take(param0x -> param0x.is(PoiTypes.HOME), (param1x, param2) -> this.canReach(param1, param2, param1x), param1.blockPosition(), 48);
    }

    private boolean canReach(Villager param0, BlockPos param1, Holder<PoiType> param2) {
        Path var0 = param0.getNavigation().createPath(param1, param2.value().validRange());
        return var0 != null && var0.canReach();
    }

    private Optional<Villager> breed(ServerLevel param0, Villager param1, Villager param2) {
        Villager var0 = param1.getBreedOffspring(param0, param2);
        if (var0 == null) {
            return Optional.empty();
        } else {
            param1.setAge(6000);
            param2.setAge(6000);
            var0.setAge(-24000);
            var0.moveTo(param1.getX(), param1.getY(), param1.getZ(), 0.0F, 0.0F);
            param0.addFreshEntityWithPassengers(var0);
            param0.broadcastEntityEvent(var0, (byte)12);
            return Optional.of(var0);
        }
    }

    private void giveBedToChild(ServerLevel param0, Villager param1, BlockPos param2) {
        GlobalPos var0 = GlobalPos.of(param0.dimension(), param2);
        param1.getBrain().setMemory(MemoryModuleType.HOME, var0);
    }
}

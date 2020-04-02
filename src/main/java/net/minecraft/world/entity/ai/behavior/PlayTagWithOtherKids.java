package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class PlayTagWithOtherKids extends Behavior<PathfinderMob> {
    public PlayTagWithOtherKids() {
        super(
            ImmutableMap.of(
                MemoryModuleType.VISIBLE_VILLAGER_BABIES,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.INTERACTION_TARGET,
                MemoryStatus.REGISTERED
            )
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return param0.getRandom().nextInt(10) == 0 && this.hasFriendsNearby(param1);
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        LivingEntity var0 = this.seeIfSomeoneIsChasingMe(param1);
        if (var0 != null) {
            this.fleeFromChaser(param0, param1, var0);
        } else {
            Optional<LivingEntity> var1 = this.findSomeoneBeingChased(param1);
            if (var1.isPresent()) {
                chaseKid(param1, var1.get());
            } else {
                this.findSomeoneToChase(param1).ifPresent(param1x -> chaseKid(param1, param1x));
            }
        }
    }

    private void fleeFromChaser(ServerLevel param0, PathfinderMob param1, LivingEntity param2) {
        for(int var0 = 0; var0 < 10; ++var0) {
            Vec3 var1 = RandomPos.getLandPos(param1, 20, 8);
            if (var1 != null && param0.isVillage(new BlockPos(var1))) {
                param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var1, 0.6F, 0));
                return;
            }
        }

    }

    private static void chaseKid(PathfinderMob param0, LivingEntity param1) {
        Brain<?> var0 = param0.getBrain();
        var0.setMemory(MemoryModuleType.INTERACTION_TARGET, param1);
        var0.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(param1));
        var0.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(param1), 0.6F, 1));
    }

    private Optional<LivingEntity> findSomeoneToChase(PathfinderMob param0) {
        return this.getFriendsNearby(param0).stream().findAny();
    }

    private Optional<LivingEntity> findSomeoneBeingChased(PathfinderMob param0) {
        Map<LivingEntity, Integer> var0 = this.checkHowManyChasersEachFriendHas(param0);
        return var0.entrySet()
            .stream()
            .sorted(Comparator.comparingInt(Entry::getValue))
            .filter(param0x -> param0x.getValue() > 0 && param0x.getValue() <= 5)
            .map(Entry::getKey)
            .findFirst();
    }

    private Map<LivingEntity, Integer> checkHowManyChasersEachFriendHas(PathfinderMob param0) {
        Map<LivingEntity, Integer> var0 = Maps.newHashMap();
        this.getFriendsNearby(param0).stream().filter(this::isChasingSomeone).forEach(param1 -> this.whoAreYouChasing(param1));
        return var0;
    }

    private List<LivingEntity> getFriendsNearby(PathfinderMob param0) {
        return param0.getBrain().getMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES).get();
    }

    private LivingEntity whoAreYouChasing(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
    }

    @Nullable
    private LivingEntity seeIfSomeoneIsChasingMe(LivingEntity param0) {
        return param0.getBrain()
            .getMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES)
            .get()
            .stream()
            .filter(param1 -> this.isFriendChasingMe(param0, param1))
            .findAny()
            .orElse(null);
    }

    private boolean isChasingSomeone(LivingEntity param0x) {
        return param0x.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
    }

    private boolean isFriendChasingMe(LivingEntity param0, LivingEntity param1) {
        return param1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).filter(param1x -> param1x == param0).isPresent();
    }

    private boolean hasFriendsNearby(PathfinderMob param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
    }
}

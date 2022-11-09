package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class PlayTagWithOtherKids {
    private static final int MAX_FLEE_XZ_DIST = 20;
    private static final int MAX_FLEE_Y_DIST = 8;
    private static final float FLEE_SPEED_MODIFIER = 0.6F;
    private static final float CHASE_SPEED_MODIFIER = 0.6F;
    private static final int MAX_CHASERS_PER_TARGET = 5;
    private static final int AVERAGE_WAIT_TIME_BETWEEN_RUNS = 10;

    public static BehaviorControl<PathfinderMob> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param0.present(MemoryModuleType.VISIBLE_VILLAGER_BABIES),
                        param0.absent(MemoryModuleType.WALK_TARGET),
                        param0.registered(MemoryModuleType.LOOK_TARGET),
                        param0.registered(MemoryModuleType.INTERACTION_TARGET)
                    )
                    .apply(param0, (param1, param2, param3, param4) -> (param5, param6, param7) -> {
                            if (param5.getRandom().nextInt(10) != 0) {
                                return false;
                            } else {
                                List<LivingEntity> var0x = param0.get(param1);
                                Optional<LivingEntity> var1x = var0x.stream().filter(param1x -> isFriendChasingMe(param6, param1x)).findAny();
                                if (!var1x.isPresent()) {
                                    Optional<LivingEntity> var4 = findSomeoneBeingChased(var0x);
                                    if (var4.isPresent()) {
                                        chaseKid(param4, param3, param2, var4.get());
                                        return true;
                                    } else {
                                        var0x.stream().findAny().ifPresent(param3x -> chaseKid(param4, param3, param2, param3x));
                                        return true;
                                    }
                                } else {
                                    for(int var4x = 0; var4x < 10; ++var4x) {
                                        Vec3 var3x = LandRandomPos.getPos(param6, 20, 8);
                                        if (var3x != null && param5.isVillage(new BlockPos(var3x))) {
                                            param2.set(new WalkTarget(var3x, 0.6F, 0));
                                            break;
                                        }
                                    }
        
                                    return true;
                                }
                            }
                        })
        );
    }

    private static void chaseKid(
        MemoryAccessor<?, LivingEntity> param0, MemoryAccessor<?, PositionTracker> param1, MemoryAccessor<?, WalkTarget> param2, LivingEntity param3
    ) {
        param0.set(param3);
        param1.set(new EntityTracker(param3, true));
        param2.set(new WalkTarget(new EntityTracker(param3, false), 0.6F, 1));
    }

    private static Optional<LivingEntity> findSomeoneBeingChased(List<LivingEntity> param0) {
        Map<LivingEntity, Integer> var0 = checkHowManyChasersEachFriendHas(param0);
        return var0.entrySet()
            .stream()
            .sorted(Comparator.comparingInt(Entry::getValue))
            .filter(param0x -> param0x.getValue() > 0 && param0x.getValue() <= 5)
            .map(Entry::getKey)
            .findFirst();
    }

    private static Map<LivingEntity, Integer> checkHowManyChasersEachFriendHas(List<LivingEntity> param0) {
        Map<LivingEntity, Integer> var0 = Maps.newHashMap();
        param0.stream()
            .filter(PlayTagWithOtherKids::isChasingSomeone)
            .forEach(param1 -> var0.compute(whoAreYouChasing(param1), (param0x, param1x) -> param1x == null ? 1 : param1x + 1));
        return var0;
    }

    private static LivingEntity whoAreYouChasing(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
    }

    private static boolean isChasingSomeone(LivingEntity param0x) {
        return param0x.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
    }

    private static boolean isFriendChasingMe(LivingEntity param0, LivingEntity param1) {
        return param1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).filter(param1x -> param1x == param0).isPresent();
    }
}

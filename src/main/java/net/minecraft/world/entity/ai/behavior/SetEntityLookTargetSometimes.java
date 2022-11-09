package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

@Deprecated
public class SetEntityLookTargetSometimes {
    public static BehaviorControl<LivingEntity> create(float param0, UniformInt param1) {
        return create(param0, param1, param0x -> true);
    }

    public static BehaviorControl<LivingEntity> create(EntityType<?> param0, float param1, UniformInt param2) {
        return create(param1, param2, param1x -> param0.equals(param1x.getType()));
    }

    private static BehaviorControl<LivingEntity> create(float param0, UniformInt param1, Predicate<LivingEntity> param2) {
        float var0 = param0 * param0;
        SetEntityLookTargetSometimes.Ticker var1 = new SetEntityLookTargetSometimes.Ticker(param1);
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor, MemoryAccessor>group(
                        param3.absent(MemoryModuleType.LOOK_TARGET), param3.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    )
                    .apply(
                        param3,
                        (param4, param5) -> (param6, param7, param8) -> {
                                Optional<LivingEntity> var0x = param3.<NearestVisibleLivingEntities>get(param5)
                                    .findClosest(param2.and(param2x -> param2x.distanceToSqr(param7) <= (double)var0));
                                if (var0x.isEmpty()) {
                                    return false;
                                } else if (!var1.tickDownAndCheck(param6.random)) {
                                    return false;
                                } else {
                                    param4.set(new EntityTracker(var0x.get(), true));
                                    return true;
                                }
                            }
                    )
        );
    }

    public static final class Ticker {
        private final UniformInt interval;
        private int ticksUntilNextStart;

        public Ticker(UniformInt param0) {
            if (param0.getMinValue() <= 1) {
                throw new IllegalArgumentException();
            } else {
                this.interval = param0;
            }
        }

        public boolean tickDownAndCheck(RandomSource param0) {
            if (this.ticksUntilNextStart == 0) {
                this.ticksUntilNextStart = this.interval.sample(param0) - 1;
                return false;
            } else {
                return --this.ticksUntilNextStart == 0;
            }
        }
    }
}

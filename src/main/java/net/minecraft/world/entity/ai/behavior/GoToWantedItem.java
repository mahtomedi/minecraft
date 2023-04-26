package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.K1;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;

public class GoToWantedItem {
    public static BehaviorControl<LivingEntity> create(float param0, boolean param1, int param2) {
        return create(param0x -> true, param0, param1, param2);
    }

    public static <E extends LivingEntity> BehaviorControl<E> create(Predicate<E> param0, float param1, boolean param2, int param3) {
        return BehaviorBuilder.create(
            param4 -> {
                BehaviorBuilder<E, ? extends MemoryAccessor<? extends K1, WalkTarget>> var0x = param2
                    ? param4.registered(MemoryModuleType.WALK_TARGET)
                    : param4.absent(MemoryModuleType.WALK_TARGET);
                return param4.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param4.registered(MemoryModuleType.LOOK_TARGET),
                        var0x,
                        param4.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
                        param4.registered(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)
                    )
                    .apply(
                        param4,
                        (param4x, param5, param6, param7) -> (param8, param9, param10) -> {
                                ItemEntity var0xx = param4.get(param6);
                                if (param4.tryGet(param7).isEmpty()
                                    && param0.test(param9)
                                    && var0xx.closerThan(param9, (double)param3)
                                    && param9.level().getWorldBorder().isWithinBounds(var0xx.blockPosition())) {
                                    WalkTarget var1x = new WalkTarget(new EntityTracker(var0xx, false), param1, 0);
                                    param4x.set(new EntityTracker(var0xx, true));
                                    param5.set(var1x);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                    );
            }
        );
    }
}

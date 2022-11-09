package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public class TriggerGate {
    public static <E extends LivingEntity> OneShot<E> triggerOneShuffled(List<Pair<? extends Trigger<? super E>, Integer>> param0) {
        return triggerGate(param0, GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE);
    }

    public static <E extends LivingEntity> OneShot<E> triggerGate(
        List<Pair<? extends Trigger<? super E>, Integer>> param0, GateBehavior.OrderPolicy param1, GateBehavior.RunningPolicy param2
    ) {
        ShufflingList<Trigger<? super E>> var0 = new ShufflingList<>();
        param0.forEach(param1x -> var0.add(param1x.getFirst(), param1x.getSecond()));
        return BehaviorBuilder.create(param3 -> param3.point((param3x, param4, param5) -> {
                if (param1 == GateBehavior.OrderPolicy.SHUFFLED) {
                    var0.shuffle();
                }

                for(Trigger<? super E> var0x : var0) {
                    if (var0x.trigger(param3x, param4, param5) && param2 == GateBehavior.RunningPolicy.RUN_ONE) {
                        break;
                    }
                }

                return true;
            }));
    }
}

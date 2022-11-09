package net.minecraft.world.entity.ai.behavior.warden;

import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TryToSniff {
    private static final IntProvider SNIFF_COOLDOWN = UniformInt.of(100, 200);

    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param0.registered(MemoryModuleType.IS_SNIFFING),
                        param0.registered(MemoryModuleType.WALK_TARGET),
                        param0.absent(MemoryModuleType.SNIFF_COOLDOWN),
                        param0.present(MemoryModuleType.NEAREST_ATTACKABLE),
                        param0.absent(MemoryModuleType.DISTURBANCE_LOCATION)
                    )
                    .apply(param0, (param0x, param1, param2, param3, param4) -> (param3x, param4x, param5) -> {
                            param0x.set(Unit.INSTANCE);
                            param2.setWithExpiry(Unit.INSTANCE, (long)SNIFF_COOLDOWN.sample(param3x.getRandom()));
                            param1.erase();
                            param4x.setPose(Pose.SNIFFING);
                            return true;
                        })
        );
    }
}

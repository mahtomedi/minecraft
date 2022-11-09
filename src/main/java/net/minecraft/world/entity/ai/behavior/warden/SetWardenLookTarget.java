package net.minecraft.world.entity.ai.behavior.warden;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class SetWardenLookTarget {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param0.registered(MemoryModuleType.LOOK_TARGET),
                        param0.registered(MemoryModuleType.DISTURBANCE_LOCATION),
                        param0.registered(MemoryModuleType.ROAR_TARGET),
                        param0.absent(MemoryModuleType.ATTACK_TARGET)
                    )
                    .apply(param0, (param1, param2, param3, param4) -> (param4x, param5, param6) -> {
                            Optional<BlockPos> var0x = param0.<LivingEntity>tryGet(param3).map(Entity::blockPosition).or(() -> param0.tryGet(param2));
                            if (var0x.isEmpty()) {
                                return false;
                            } else {
                                param1.set(new BlockPosTracker((BlockPos)var0x.get()));
                                return true;
                            }
                        })
        );
    }
}

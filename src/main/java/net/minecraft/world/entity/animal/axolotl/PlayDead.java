package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class PlayDead extends Behavior<Axolotl> {
    public PlayDead() {
        super(ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT, MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT), 200);
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Axolotl param1) {
        return param1.isInWaterOrBubble();
    }

    protected boolean canStillUse(ServerLevel param0, Axolotl param1, long param2) {
        return param1.isInWaterOrBubble() && param1.getBrain().hasMemoryValue(MemoryModuleType.PLAY_DEAD_TICKS);
    }

    protected void start(ServerLevel param0, Axolotl param1, long param2) {
        Brain<Axolotl> var0 = param1.getBrain();
        var0.eraseMemory(MemoryModuleType.WALK_TARGET);
        var0.eraseMemory(MemoryModuleType.LOOK_TARGET);
        param1.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
    }
}

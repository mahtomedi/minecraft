package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class RandomLookAround extends Behavior<Mob> {
    private final IntProvider interval;
    private final float maxYaw;
    private final float minPitch;
    private final float pitchRange;

    public RandomLookAround(IntProvider param0, float param1, float param2, float param3) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT));
        if (param2 > param3) {
            throw new IllegalArgumentException("Minimum pitch is larger than maximum pitch! " + param2 + " > " + param3);
        } else {
            this.interval = param0;
            this.maxYaw = param1;
            this.minPitch = param2;
            this.pitchRange = param3 - param2;
        }
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        RandomSource var0 = param1.getRandom();
        float var1 = Mth.clamp(var0.nextFloat() * this.pitchRange + this.minPitch, -90.0F, 90.0F);
        float var2 = Mth.wrapDegrees(param1.getYRot() + 2.0F * var0.nextFloat() * this.maxYaw - this.maxYaw);
        Vec3 var3 = Vec3.directionFromRotation(var1, var2);
        param1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(param1.getEyePosition().add(var3)));
        param1.getBrain().setMemory(MemoryModuleType.GAZE_COOLDOWN_TICKS, this.interval.sample(var0));
    }
}

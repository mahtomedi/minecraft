package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public class SonicBoom extends Behavior<Warden> {
    private static final int DISTANCE_XZ = 15;
    private static final int DISTANCE_Y = 20;
    private static final double KNOCKBACK_VERTICAL = 0.5;
    private static final double KNOCKBACK_HORIZONTAL = 2.5;
    public static final int COOLDOWN = 40;
    private static final int TICKS_BEFORE_PLAYING_SOUND = Mth.ceil(34.0);
    private static final int DURATION = Mth.ceil(60.0F);

    public SonicBoom() {
        super(
            ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.SONIC_BOOM_COOLDOWN,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN,
                MemoryStatus.REGISTERED,
                MemoryModuleType.SONIC_BOOM_SOUND_DELAY,
                MemoryStatus.REGISTERED
            ),
            DURATION
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Warden param1) {
        return param1.closerThan(param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get(), 15.0, 20.0);
    }

    protected boolean canStillUse(ServerLevel param0, Warden param1, long param2) {
        return true;
    }

    protected void start(ServerLevel param0, Warden param1, long param2) {
        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)DURATION);
        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_DELAY, Unit.INSTANCE, (long)TICKS_BEFORE_PLAYING_SOUND);
        param0.broadcastEntityEvent(param1, (byte)62);
        param1.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 1.0F);
    }

    protected void tick(ServerLevel param0, Warden param1, long param2) {
        param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(param1x -> param1.getLookControl().setLookAt(param1x.position()));
        if (!param1.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_DELAY)
            && !param1.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) {
            param1.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, Unit.INSTANCE, (long)(DURATION - TICKS_BEFORE_PLAYING_SOUND));
            param1.getBrain()
                .getMemory(MemoryModuleType.ATTACK_TARGET)
                .filter(param1::canTargetEntity)
                .filter(param1x -> param1.closerThan(param1x, 15.0, 20.0))
                .ifPresent(param2x -> {
                    Vec3 var0 = param1.position().add(0.0, 1.6F, 0.0);
                    Vec3 var1x = param2x.getEyePosition().subtract(var0);
                    Vec3 var2x = var1x.normalize();
    
                    for(int var3 = 1; var3 < Mth.floor(var1x.length()) + 7; ++var3) {
                        Vec3 var4 = var0.add(var2x.scale((double)var3));
                        param0.sendParticles(ParticleTypes.SONIC_BOOM, var4.x, var4.y, var4.z, 1, 0.0, 0.0, 0.0, 0.0);
                    }
    
                    param1.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
                    param2x.hurt(param0.damageSources().sonicBoom(param1), 10.0F);
                    double var5 = 0.5 * (1.0 - param2x.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    double var6 = 2.5 * (1.0 - param2x.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    param2x.push(var2x.x() * var6, var2x.y() * var5, var2x.z() * var6);
                });
        }
    }

    protected void stop(ServerLevel param0, Warden param1, long param2) {
        setCooldown(param1, 40);
    }

    public static void setCooldown(LivingEntity param0, int param1) {
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_COOLDOWN, Unit.INSTANCE, (long)param1);
    }
}

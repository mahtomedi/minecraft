package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.WindCharge;
import net.minecraft.world.phys.Vec3;

public class Shoot extends Behavior<Breeze> {
    private static final int ATTACK_RANGE_MIN_SQRT = 4;
    private static final int ATTACK_RANGE_MAX_SQRT = 256;
    private static final int UNCERTAINTY_BASE = 5;
    private static final int UNCERTAINTY_MULTIPLIER = 4;
    private static final float PROJECTILE_MOVEMENT_SCALE = 0.7F;
    private static final int SHOOT_INITIAL_DELAY_TICKS = Math.round(15.0F);
    private static final int SHOOT_RECOVER_DELAY_TICKS = Math.round(4.0F);
    private static final int SHOOT_COOLDOWN_TICKS = Math.round(10.0F);

    @VisibleForTesting
    public Shoot() {
        super(
            ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.BREEZE_SHOOT_COOLDOWN,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.BREEZE_SHOOT_CHARGING,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.BREEZE_SHOOT_RECOVERING,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.BREEZE_SHOOT,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.BREEZE_JUMP_TARGET,
                MemoryStatus.VALUE_ABSENT
            ),
            SHOOT_INITIAL_DELAY_TICKS + 1 + SHOOT_RECOVER_DELAY_TICKS
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Breeze param1) {
        return param1.onGround() && param1.getPose() == Pose.STANDING
            ? param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map(param1x -> isTargetWithinRange(param1, param1x)).map(param1x -> {
                if (!param1x) {
                    param1.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
                }
    
                return param1x;
            }).orElse(false)
            : false;
    }

    protected boolean canStillUse(ServerLevel param0, Breeze param1, long param2) {
        return param1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && param1.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_SHOOT);
    }

    protected void start(ServerLevel param0, Breeze param1, long param2) {
        param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(param1x -> param1.setPose(Pose.SHOOTING));
        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_CHARGING, Unit.INSTANCE, (long)SHOOT_INITIAL_DELAY_TICKS);
        param1.playSound(SoundEvents.BREEZE_INHALE, 1.0F, 1.0F);
    }

    protected void stop(ServerLevel param0, Breeze param1, long param2) {
        if (param1.getPose() == Pose.SHOOTING) {
            param1.setPose(Pose.STANDING);
        }

        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_COOLDOWN, Unit.INSTANCE, (long)SHOOT_COOLDOWN_TICKS);
        param1.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
    }

    protected void tick(ServerLevel param0, Breeze param1, long param2) {
        Brain<Breeze> var0 = param1.getBrain();
        LivingEntity var1 = var0.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (var1 != null && param1.onGround()) {
            param1.lookAt(EntityAnchorArgument.Anchor.EYES, var1.position());
            if (!var0.getMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING).isPresent() && !var0.getMemory(MemoryModuleType.BREEZE_SHOOT_RECOVERING).isPresent()) {
                var0.setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_RECOVERING, Unit.INSTANCE, (long)SHOOT_RECOVER_DELAY_TICKS);
                if (isFacingTarget(param1, var1)) {
                    double var2 = var1.getX() - param1.getX();
                    double var3 = var1.getY(0.3) - param1.getY(0.5);
                    double var4 = var1.getZ() - param1.getZ();
                    WindCharge var5 = new WindCharge(EntityType.WIND_CHARGE, param1, param0);
                    param1.playSound(SoundEvents.BREEZE_SHOOT, 1.5F, 1.0F);
                    var5.shoot(var2, var3, var4, 0.7F, (float)(5 - param0.getDifficulty().getId() * 4));
                    param0.addFreshEntity(var5);
                }

            }
        }
    }

    @VisibleForTesting
    public static boolean isFacingTarget(Breeze param0, LivingEntity param1) {
        Vec3 var0 = param0.getViewVector(1.0F);
        Vec3 var1 = param1.position().subtract(param0.position()).normalize();
        return var0.dot(var1) > 0.5;
    }

    private static boolean isTargetWithinRange(Breeze param0, LivingEntity param1) {
        double var0 = param0.position().distanceToSqr(param1.position());
        return var0 > 4.0 && var0 < 256.0;
    }
}

package net.minecraft.world.entity.monster.breeze;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class Slide extends Behavior<Breeze> {
    public Slide() {
        super(
            Map.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.BREEZE_JUMP_COOLDOWN,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.BREEZE_SHOOT,
                MemoryStatus.VALUE_ABSENT
            )
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Breeze param1) {
        return param1.onGround() && !param1.isInWater() && param1.getPose() == Pose.STANDING;
    }

    protected void start(ServerLevel param0, Breeze param1, long param2) {
        LivingEntity var0 = param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (var0 != null) {
            boolean var1 = param1.withinOuterCircleRange(var0.position());
            boolean var2 = param1.withinMiddleCircleRange(var0.position());
            boolean var3 = param1.withinInnerCircleRange(var0.position());
            Vec3 var4 = null;
            if (var1) {
                var4 = randomPointInMiddleCircle(param1, var0);
            } else if (var3) {
                Vec3 var5 = DefaultRandomPos.getPosAway(param1, 5, 5, var0.position());
                if (var5 != null && var0.distanceToSqr(var5.x, var5.y, var5.z) > var0.distanceToSqr(param1)) {
                    var4 = var5;
                }
            } else if (var2) {
                var4 = LandRandomPos.getPos(param1, 5, 3);
            }

            if (var4 != null) {
                param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(BlockPos.containing(var4), 0.6F, 1));
            }

        }
    }

    protected void stop(ServerLevel param0, Breeze param1, long param2) {
        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, 20L);
    }

    private static Vec3 randomPointInMiddleCircle(Breeze param0, LivingEntity param1) {
        Vec3 var0 = param1.position().subtract(param0.position());
        double var1 = var0.length() - Mth.lerp(param0.getRandom().nextDouble(), 8.0, 4.0);
        Vec3 var2 = var0.normalize().multiply(var1, var1, var1);
        return param0.position().add(var2);
    }
}

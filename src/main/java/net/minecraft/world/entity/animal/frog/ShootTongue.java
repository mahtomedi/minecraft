package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class ShootTongue extends Behavior<Frog> {
    public static final int TIME_OUT_DURATION = 100;
    public static final int CATCH_ANIMATION_DURATION = 6;
    public static final int TONGUE_ANIMATION_DURATION = 10;
    private static final float EATING_DISTANCE = 1.75F;
    private static final float EATING_MOVEMENT_FACTOR = 0.75F;
    public static final int UNREACHABLE_TONGUE_TARGETS_COOLDOWN_DURATION = 100;
    public static final int MAX_UNREACHBLE_TONGUE_TARGETS_IN_MEMORY = 5;
    private int eatAnimationTimer;
    private int calculatePathCounter;
    private final SoundEvent tongueSound;
    private final SoundEvent eatSound;
    private Vec3 itemSpawnPos;
    private ShootTongue.State state = ShootTongue.State.DONE;

    public ShootTongue(SoundEvent param0, SoundEvent param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.IS_PANICKING,
                MemoryStatus.VALUE_ABSENT
            ),
            100
        );
        this.tongueSound = param0;
        this.eatSound = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Frog param1) {
        LivingEntity var0 = param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        boolean var1 = this.canPathfindToTarget(param1, var0);
        if (!var1) {
            param1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            this.addUnreachableTargetToMemory(param1, var0);
        }

        return var1 && param1.getPose() != Pose.CROAKING && Frog.canEat(var0);
    }

    protected boolean canStillUse(ServerLevel param0, Frog param1, long param2) {
        return param1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
            && this.state != ShootTongue.State.DONE
            && !param1.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
    }

    protected void start(ServerLevel param0, Frog param1, long param2) {
        LivingEntity var0 = param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        BehaviorUtils.lookAtEntity(param1, var0);
        param1.setTongueTarget(var0);
        param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var0.position(), 2.0F, 0));
        this.calculatePathCounter = 10;
        this.state = ShootTongue.State.MOVE_TO_TARGET;
    }

    protected void stop(ServerLevel param0, Frog param1, long param2) {
        param1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        param1.eraseTongueTarget();
        param1.setPose(Pose.STANDING);
    }

    private void eatEntity(ServerLevel param0, Frog param1) {
        param0.playSound(null, param1, this.eatSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
        Optional<Entity> var0 = param1.getTongueTarget();
        if (var0.isPresent()) {
            Entity var1 = var0.get();
            if (var1.isAlive()) {
                param1.doHurtTarget(var1);
                if (!var1.isAlive()) {
                    var1.remove(Entity.RemovalReason.KILLED);
                }
            }
        }

    }

    protected void tick(ServerLevel param0, Frog param1, long param2) {
        LivingEntity var0 = param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        param1.setTongueTarget(var0);
        switch(this.state) {
            case MOVE_TO_TARGET:
                if (var0.distanceTo(param1) < 1.75F) {
                    param0.playSound(null, param1, this.tongueSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
                    param1.setPose(Pose.USING_TONGUE);
                    var0.setDeltaMovement(var0.position().vectorTo(param1.position()).normalize().scale(0.75));
                    this.itemSpawnPos = var0.position();
                    this.eatAnimationTimer = 0;
                    this.state = ShootTongue.State.CATCH_ANIMATION;
                } else if (this.calculatePathCounter <= 0) {
                    param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var0.position(), 2.0F, 0));
                    this.calculatePathCounter = 10;
                } else {
                    --this.calculatePathCounter;
                }
                break;
            case CATCH_ANIMATION:
                if (this.eatAnimationTimer++ >= 6) {
                    this.state = ShootTongue.State.EAT_ANIMATION;
                    this.eatEntity(param0, param1);
                }
                break;
            case EAT_ANIMATION:
                if (this.eatAnimationTimer >= 10) {
                    this.state = ShootTongue.State.DONE;
                } else {
                    ++this.eatAnimationTimer;
                }
            case DONE:
        }

    }

    private boolean canPathfindToTarget(Frog param0, LivingEntity param1) {
        Path var0 = param0.getNavigation().createPath(param1, 0);
        return var0 != null && var0.getDistToTarget() < 1.75F;
    }

    private void addUnreachableTargetToMemory(Frog param0, LivingEntity param1) {
        List<UUID> var0 = param0.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
        boolean var1 = !var0.contains(param1.getUUID());
        if (var0.size() == 5 && var1) {
            var0.remove(0);
        }

        if (var1) {
            var0.add(param1.getUUID());
        }

        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS, var0, 100L);
    }

    static enum State {
        MOVE_TO_TARGET,
        CATCH_ANIMATION,
        EAT_ANIMATION,
        DONE;
    }
}

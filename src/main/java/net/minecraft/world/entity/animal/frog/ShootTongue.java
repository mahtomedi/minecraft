package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ShootTongue extends Behavior<Frog> {
    public static final int TIME_OUT_DURATION = 100;
    public static final int TONGUE_ANIMATION_DURATION = 6;
    private static final float EATING_DISTANCE = 1.75F;
    private static final float EATING_MOVEMENT_FACTOR = 0.75F;
    private int eatAnimationTimer;
    private int calculatePathCounter;
    private final SoundEvent tongueSound;
    private final SoundEvent eatSound;
    private Vec3 itemSpawnPos;
    private boolean finishedEating;
    private ShootTongue.State state = ShootTongue.State.DONE;

    public ShootTongue(SoundEvent param0, SoundEvent param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT
            ),
            100
        );
        this.tongueSound = param0;
        this.eatSound = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Frog param1) {
        return super.checkExtraStartConditions(param0, param1) && Frog.canEat(param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get());
    }

    protected boolean canStillUse(ServerLevel param0, Frog param1, long param2) {
        return param1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.state != ShootTongue.State.DONE;
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
        param1.setPose(Pose.STANDING);
        param0.playSound(null, param1, this.eatSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
        Optional<Entity> var0 = param1.getTongueTarget();
        if (var0.isPresent()) {
            Entity var1 = var0.get();
            if (this.finishedEating && var1.isAlive()) {
                var1.remove(Entity.RemovalReason.KILLED);
                ItemStack var2 = getLootItem(param1, var1);
                param0.addFreshEntity(new ItemEntity(param0, this.itemSpawnPos.x(), this.itemSpawnPos.y(), this.itemSpawnPos.z(), var2));
            }
        }

        param1.eraseTongueTarget();
        this.finishedEating = false;
    }

    private static ItemStack getLootItem(Frog param0, Entity param1) {
        if (param1 instanceof MagmaCube) {
            return new ItemStack(switch(param0.getVariant()) {
                case TEMPERATE -> Items.OCHRE_FROGLIGHT;
                case WARM -> Items.PEARLESCENT_FROGLIGHT;
                case COLD -> Items.VERDANT_FROGLIGHT;
            });
        } else {
            return new ItemStack(Items.SLIME_BALL);
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
                    this.eatAnimationTimer = 6;
                    this.state = ShootTongue.State.EAT_ANIMATION;
                } else if (this.calculatePathCounter <= 0) {
                    param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var0.position(), 2.0F, 0));
                    this.calculatePathCounter = 10;
                } else {
                    --this.calculatePathCounter;
                }
                break;
            case EAT_ANIMATION:
                if (this.eatAnimationTimer <= 0) {
                    this.finishedEating = true;
                    this.state = ShootTongue.State.DONE;
                } else {
                    --this.eatAnimationTimer;
                }
            case DONE:
        }

    }

    static enum State {
        MOVE_TO_TARGET,
        EAT_ANIMATION,
        DONE;
    }
}
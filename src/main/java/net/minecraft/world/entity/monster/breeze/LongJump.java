package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LongJump extends Behavior<Breeze> {
    private static final int REQUIRED_AIR_BLOCKS_ABOVE = 4;
    private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 50.0;
    private static final int JUMP_COOLDOWN_TICKS = 10;
    private static final int JUMP_COOLDOWN_WHEN_HURT_TICKS = 2;
    private static final int INHALING_DURATION_TICKS = Math.round(10.0F);
    private static final float MAX_JUMP_VELOCITY = 1.4F;
    private static final ObjectArrayList<Integer> ALLOWED_ANGLES = new ObjectArrayList<>(Lists.newArrayList(40, 55, 60, 75, 80));

    @VisibleForTesting
    public LongJump() {
        super(
            Map.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.BREEZE_JUMP_COOLDOWN,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.BREEZE_JUMP_INHALING,
                MemoryStatus.REGISTERED,
                MemoryModuleType.BREEZE_JUMP_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.BREEZE_SHOOT,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT
            ),
            200
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Breeze param1) {
        if (!param1.onGround() && !param1.isInWater()) {
            return false;
        } else if (param1.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.VALUE_PRESENT)) {
            return true;
        } else {
            LivingEntity var0 = param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (var0 == null) {
                return false;
            } else if (outOfAggroRange(param1, var0)) {
                param1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                return false;
            } else if (tooCloseForJump(param1, var0)) {
                return false;
            } else if (!canJumpFromCurrentPosition(param0, param1)) {
                return false;
            } else {
                BlockPos var1 = snapToSurface(param1, randomPointBehindTarget(var0, param1.getRandom()));
                if (var1 == null) {
                    return false;
                } else if (!hasLineOfSight(param1, var1.getCenter()) && !hasLineOfSight(param1, var1.above(4).getCenter())) {
                    return false;
                } else {
                    param1.getBrain().setMemory(MemoryModuleType.BREEZE_JUMP_TARGET, var1);
                    return true;
                }
            }
        }
    }

    protected boolean canStillUse(ServerLevel param0, Breeze param1, long param2) {
        return param1.getPose() != Pose.STANDING && !param1.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_JUMP_COOLDOWN);
    }

    protected void start(ServerLevel param0, Breeze param1, long param2) {
        if (param1.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.VALUE_ABSENT)) {
            param1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_INHALING, Unit.INSTANCE, (long)INHALING_DURATION_TICKS);
        }

        param1.setPose(Pose.INHALING);
        param1.getBrain()
            .getMemory(MemoryModuleType.BREEZE_JUMP_TARGET)
            .ifPresent(param1x -> param1.lookAt(EntityAnchorArgument.Anchor.EYES, param1x.getCenter()));
    }

    protected void tick(ServerLevel param0, Breeze param1, long param2) {
        if (finishedInhaling(param1)) {
            Vec3 var0 = param1.getBrain()
                .getMemory(MemoryModuleType.BREEZE_JUMP_TARGET)
                .flatMap(param1x -> calculateOptimalJumpVector(param1, param1.getRandom(), Vec3.atBottomCenterOf(param1x)))
                .orElse(null);
            if (var0 == null) {
                param1.setPose(Pose.STANDING);
                return;
            }

            param1.playSound(SoundEvents.BREEZE_JUMP, 1.0F, 1.0F);
            param1.setPose(Pose.LONG_JUMPING);
            param1.setYRot(param1.yBodyRot);
            param1.setDiscardFriction(true);
            param1.setDeltaMovement(var0);
        } else if (finishedJumping(param1)) {
            param1.playSound(SoundEvents.BREEZE_LAND, 1.0F, 1.0F);
            param1.setPose(Pose.STANDING);
            param1.setDiscardFriction(false);
            boolean var1 = param1.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
            param1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, var1 ? 2L : 10L);
            param1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 100L);
        }

    }

    protected void stop(ServerLevel param0, Breeze param1, long param2) {
        if (param1.getPose() == Pose.LONG_JUMPING || param1.getPose() == Pose.INHALING) {
            param1.setPose(Pose.STANDING);
        }

        param1.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_TARGET);
        param1.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_INHALING);
    }

    private static boolean finishedInhaling(Breeze param0) {
        return param0.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_INHALING).isEmpty() && param0.getPose() == Pose.INHALING;
    }

    private static boolean finishedJumping(Breeze param0) {
        return param0.getPose() == Pose.LONG_JUMPING && param0.onGround();
    }

    private static Vec3 randomPointBehindTarget(LivingEntity param0, RandomSource param1) {
        int var0 = 90;
        float var1 = param0.yHeadRot + 180.0F + (float)param1.nextGaussian() * 90.0F / 2.0F;
        float var2 = Mth.lerp(param1.nextFloat(), 4.0F, 8.0F);
        Vec3 var3 = Vec3.directionFromRotation(0.0F, var1).scale((double)var2);
        return param0.position().add(var3);
    }

    @Nullable
    private static BlockPos snapToSurface(LivingEntity param0, Vec3 param1) {
        ClipContext var0 = new ClipContext(param1, param1.relative(Direction.DOWN, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, param0);
        HitResult var1 = param0.level().clip(var0);
        if (var1.getType() == HitResult.Type.BLOCK) {
            return BlockPos.containing(var1.getLocation()).above();
        } else {
            ClipContext var2 = new ClipContext(param1, param1.relative(Direction.UP, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, param0);
            HitResult var3 = param0.level().clip(var2);
            return var3.getType() == HitResult.Type.BLOCK ? BlockPos.containing(var1.getLocation()).above() : null;
        }
    }

    @VisibleForTesting
    public static boolean hasLineOfSight(Breeze param0, Vec3 param1) {
        Vec3 var0 = new Vec3(param0.getX(), param0.getY(), param0.getZ());
        if (param1.distanceTo(var0) > 50.0) {
            return false;
        } else {
            return param0.level().clip(new ClipContext(var0, param1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, param0)).getType()
                == HitResult.Type.MISS;
        }
    }

    private static boolean outOfAggroRange(Breeze param0, LivingEntity param1) {
        return !param1.closerThan(param0, 24.0);
    }

    private static boolean tooCloseForJump(Breeze param0, LivingEntity param1) {
        return param1.distanceTo(param0) - 4.0F <= 0.0F;
    }

    private static boolean canJumpFromCurrentPosition(ServerLevel param0, Breeze param1) {
        BlockPos var0 = param1.blockPosition();

        for(int var1 = 1; var1 <= 4; ++var1) {
            BlockPos var2 = var0.relative(Direction.UP, var1);
            if (!param0.getBlockState(var2).isAir() && !param0.getFluidState(var2).is(FluidTags.WATER)) {
                return false;
            }
        }

        return true;
    }

    private static Optional<Vec3> calculateOptimalJumpVector(Breeze param0, RandomSource param1, Vec3 param2) {
        for(int var1 : Util.shuffledCopy(ALLOWED_ANGLES, param1)) {
            Optional<Vec3> var2 = LongJumpUtil.calculateJumpVectorForAngle(param0, param2, 1.4F, var1, false);
            if (var2.isPresent()) {
                return var2;
            }
        }

        return Optional.empty();
    }
}

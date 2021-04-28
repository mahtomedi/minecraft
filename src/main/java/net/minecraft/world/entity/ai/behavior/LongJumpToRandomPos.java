package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.WeighedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends Mob> extends Behavior<E> {
    private static final int FIND_JUMP_TRIES = 20;
    private static final int PREPARE_JUMP_DURATION = 40;
    private static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
    public static final int TIME_OUT_DURATION = 200;
    private final UniformInt timeBetweenLongJumps;
    private final int maxLongJumpHeight;
    private final int maxLongJumpWidth;
    private final float maxJumpVelocity;
    private final List<LongJumpToRandomPos.PossibleJump> jumpCandidates = new ArrayList<>();
    private Optional<Vec3> initialPosition = Optional.empty();
    private Optional<LongJumpToRandomPos.PossibleJump> chosenJump = Optional.empty();
    private int findJumpTries;
    private long prepareJumpStart;
    private Function<E, SoundEvent> getJumpSound;

    public LongJumpToRandomPos(UniformInt param0, int param1, int param2, float param3, Function<E, SoundEvent> param4) {
        super(
            ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LONG_JUMP_MID_JUMP,
                MemoryStatus.VALUE_ABSENT
            ),
            200
        );
        this.timeBetweenLongJumps = param0;
        this.maxLongJumpHeight = param1;
        this.maxLongJumpWidth = param2;
        this.maxJumpVelocity = param3;
        this.getJumpSound = param4;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Mob param1) {
        return param1.isOnGround() && !param0.getBlockState(param1.blockPosition().below()).is(Blocks.HONEY_BLOCK);
    }

    protected boolean canStillUse(ServerLevel param0, Mob param1, long param2) {
        boolean var0 = this.initialPosition.isPresent()
            && this.initialPosition.get().equals(param1.position())
            && this.findJumpTries > 0
            && (this.chosenJump.isPresent() || !this.jumpCandidates.isEmpty());
        if (!var0 && !param1.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isPresent()) {
            param1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(param0.random) / 2);
        }

        return var0;
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        this.chosenJump = Optional.empty();
        this.findJumpTries = 20;
        this.jumpCandidates.clear();
        this.initialPosition = Optional.of(param1.position());
        BlockPos var0 = param1.blockPosition();
        int var1 = var0.getX();
        int var2 = var0.getY();
        int var3 = var0.getZ();
        Iterable<BlockPos> var4 = BlockPos.betweenClosed(
            var1 - this.maxLongJumpWidth,
            var2 - this.maxLongJumpHeight,
            var3 - this.maxLongJumpWidth,
            var1 + this.maxLongJumpWidth,
            var2 + this.maxLongJumpHeight,
            var3 + this.maxLongJumpWidth
        );
        PathNavigation var5 = param1.getNavigation();

        for(BlockPos var6 : var4) {
            double var7 = var6.distSqr(var0);
            if ((var1 != var6.getX() || var3 != var6.getZ())
                && var5.isStableDestination(var6)
                && param1.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(param1.level, var6.mutable())) == 0.0F) {
                Optional<Vec3> var8 = this.calculateOptimalJumpVector(param1, Vec3.atCenterOf(var6));
                var8.ifPresent(param2x -> this.jumpCandidates.add(new LongJumpToRandomPos.PossibleJump(new BlockPos(var6), param2x, Mth.ceil(var7))));
            }
        }

    }

    protected void tick(ServerLevel param0, E param1, long param2) {
        if (this.chosenJump.isPresent()) {
            if (param2 - this.prepareJumpStart >= 40L) {
                param1.setYRot(param1.yBodyRot);
                param1.setDiscardFriction(true);
                param1.setDeltaMovement(this.chosenJump.get().getJumpVector());
                param1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
                param0.playSound(null, param1, this.getJumpSound.apply(param1), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        } else {
            --this.findJumpTries;
            Optional<LongJumpToRandomPos.PossibleJump> var0 = WeighedRandom.getRandomItem(param0.random, this.jumpCandidates);
            if (var0.isPresent()) {
                this.jumpCandidates.remove(var0.get());
                param1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(var0.get().getJumpTarget()));
                PathNavigation var1 = param1.getNavigation();
                Path var2 = var1.createPath(var0.get().getJumpTarget(), 0, 8);
                if (var2 == null || !var2.canReach()) {
                    this.chosenJump = var0;
                    this.prepareJumpStart = param2;
                }
            }
        }

    }

    private Optional<Vec3> calculateOptimalJumpVector(Mob param0, Vec3 param1) {
        Optional<Vec3> var0 = Optional.empty();

        for(int var1 = 65; var1 < 85; var1 += 5) {
            Optional<Vec3> var2 = this.calculateJumpVectorForAngle(param0, param1, var1);
            if (!var0.isPresent() || var2.isPresent() && var2.get().lengthSqr() < var0.get().lengthSqr()) {
                var0 = var2;
            }
        }

        return var0;
    }

    private Optional<Vec3> calculateJumpVectorForAngle(Mob param0, Vec3 param1, int param2) {
        Vec3 var0 = param0.position();
        Vec3 var1 = new Vec3(param1.x - var0.x, 0.0, param1.z - var0.z).normalize().scale(0.5);
        param1 = param1.subtract(var1);
        Vec3 var2 = param1.subtract(var0);
        float var3 = (float)param2 * (float) Math.PI / 180.0F;
        double var4 = Math.atan2(var2.z, var2.x);
        double var5 = var2.subtract(0.0, var2.y, 0.0).lengthSqr();
        double var6 = Math.sqrt(var5);
        double var7 = var2.y;
        double var8 = Math.sin((double)(2.0F * var3));
        double var9 = 0.08;
        double var10 = Math.pow(Math.cos((double)var3), 2.0);
        double var11 = Math.sin((double)var3);
        double var12 = Math.cos((double)var3);
        double var13 = Math.sin(var4);
        double var14 = Math.cos(var4);
        double var15 = var5 * 0.08 / (var6 * var8 - 2.0 * var7 * var10);
        if (var15 < 0.0) {
            return Optional.empty();
        } else {
            double var16 = Math.sqrt(var15);
            if (var16 > (double)this.maxJumpVelocity) {
                return Optional.empty();
            } else {
                double var17 = var16 * var12;
                double var18 = var16 * var11;
                int var19 = Mth.ceil(var6 / var17) * 2;
                double var20 = 0.0;
                Vec3 var21 = null;

                for(int var22 = 0; var22 < var19 - 1; ++var22) {
                    var20 += var6 / (double)var19;
                    double var23 = var11 / var12 * var20 - Math.pow(var20, 2.0) * 0.08 / (2.0 * var15 * Math.pow(var12, 2.0));
                    double var24 = var20 * var14;
                    double var25 = var20 * var13;
                    Vec3 var26 = new Vec3(var0.x + var24, var0.y + var23, var0.z + var25);
                    if (var21 != null && !this.isClearTransition(param0, var21, var26)) {
                        return Optional.empty();
                    }

                    var21 = var26;
                }

                return Optional.of(new Vec3(var17 * var14, var18, var17 * var13).scale(0.95F));
            }
        }
    }

    private boolean isClearTransition(Mob param0, Vec3 param1, Vec3 param2) {
        EntityDimensions var0 = param0.getDimensions(Pose.LONG_JUMPING);
        Vec3 var1 = param2.subtract(param1);
        double var2 = (double)Math.min(var0.width, var0.height);
        int var3 = Mth.ceil(var1.length() / var2);
        Vec3 var4 = var1.normalize();
        Vec3 var5 = param1;

        for(int var6 = 0; var6 < var3; ++var6) {
            var5 = var6 == var3 - 1 ? param2 : var5.add(var4.scale(var2 * 0.9F));
            AABB var7 = var0.makeBoundingBox(var5);
            if (!param0.level.noCollision(param0, var7)) {
                return false;
            }
        }

        return true;
    }

    public static class PossibleJump extends WeighedRandom.WeighedRandomItem {
        private final BlockPos jumpTarget;
        private final Vec3 jumpVector;

        public PossibleJump(BlockPos param0, Vec3 param1, int param2) {
            super(param2);
            this.jumpTarget = param0;
            this.jumpVector = param1;
        }

        public BlockPos getJumpTarget() {
            return this.jumpTarget;
        }

        public Vec3 getJumpVector() {
            return this.jumpVector;
        }
    }
}

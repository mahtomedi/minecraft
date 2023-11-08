package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends Mob> extends Behavior<E> {
    protected static final int FIND_JUMP_TRIES = 20;
    private static final int PREPARE_JUMP_DURATION = 40;
    protected static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
    private static final int TIME_OUT_DURATION = 200;
    private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(65, 70, 75, 80);
    private final UniformInt timeBetweenLongJumps;
    protected final int maxLongJumpHeight;
    protected final int maxLongJumpWidth;
    protected final float maxJumpVelocity;
    protected List<LongJumpToRandomPos.PossibleJump> jumpCandidates = Lists.newArrayList();
    protected Optional<Vec3> initialPosition = Optional.empty();
    @Nullable
    protected Vec3 chosenJump;
    protected int findJumpTries;
    protected long prepareJumpStart;
    private final Function<E, SoundEvent> getJumpSound;
    private final BiPredicate<E, BlockPos> acceptableLandingSpot;

    public LongJumpToRandomPos(UniformInt param0, int param1, int param2, float param3, Function<E, SoundEvent> param4) {
        this(param0, param1, param2, param3, param4, LongJumpToRandomPos::defaultAcceptableLandingSpot);
    }

    public static <E extends Mob> boolean defaultAcceptableLandingSpot(E param0x, BlockPos param1x) {
        Level var0 = param0x.level();
        BlockPos var1 = param1x.below();
        return var0.getBlockState(var1).isSolidRender(var0, var1)
            && param0x.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(var0, param1x.mutable())) == 0.0F;
    }

    public LongJumpToRandomPos(UniformInt param0, int param1, int param2, float param3, Function<E, SoundEvent> param4, BiPredicate<E, BlockPos> param5) {
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
        this.acceptableLandingSpot = param5;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Mob param1) {
        boolean var0 = param1.onGround() && !param1.isInWater() && !param1.isInLava() && !param0.getBlockState(param1.blockPosition()).is(Blocks.HONEY_BLOCK);
        if (!var0) {
            param1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(param0.random) / 2);
        }

        return var0;
    }

    protected boolean canStillUse(ServerLevel param0, Mob param1, long param2) {
        boolean var0 = this.initialPosition.isPresent()
            && this.initialPosition.get().equals(param1.position())
            && this.findJumpTries > 0
            && !param1.isInWaterOrBubble()
            && (this.chosenJump != null || !this.jumpCandidates.isEmpty());
        if (!var0 && param1.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
            param1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(param0.random) / 2);
            param1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        }

        return var0;
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        this.chosenJump = null;
        this.findJumpTries = 20;
        this.initialPosition = Optional.of(param1.position());
        BlockPos var0 = param1.blockPosition();
        int var1 = var0.getX();
        int var2 = var0.getY();
        int var3 = var0.getZ();
        this.jumpCandidates = BlockPos.betweenClosedStream(
                var1 - this.maxLongJumpWidth,
                var2 - this.maxLongJumpHeight,
                var3 - this.maxLongJumpWidth,
                var1 + this.maxLongJumpWidth,
                var2 + this.maxLongJumpHeight,
                var3 + this.maxLongJumpWidth
            )
            .filter(param1x -> !param1x.equals(var0))
            .map(param1x -> new LongJumpToRandomPos.PossibleJump(param1x.immutable(), Mth.ceil(var0.distSqr(param1x))))
            .collect(Collectors.toCollection(Lists::newArrayList));
    }

    protected void tick(ServerLevel param0, E param1, long param2) {
        if (this.chosenJump != null) {
            if (param2 - this.prepareJumpStart >= 40L) {
                param1.setYRot(param1.yBodyRot);
                param1.setDiscardFriction(true);
                double var0 = this.chosenJump.length();
                double var1 = var0 + (double)param1.getJumpBoostPower();
                param1.setDeltaMovement(this.chosenJump.scale(var1 / var0));
                param1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
                param0.playSound(null, param1, this.getJumpSound.apply(param1), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        } else {
            --this.findJumpTries;
            this.pickCandidate(param0, param1, param2);
        }

    }

    protected void pickCandidate(ServerLevel param0, E param1, long param2) {
        while(!this.jumpCandidates.isEmpty()) {
            Optional<LongJumpToRandomPos.PossibleJump> var0 = this.getJumpCandidate(param0);
            if (!var0.isEmpty()) {
                LongJumpToRandomPos.PossibleJump var1 = var0.get();
                BlockPos var2 = var1.getJumpTarget();
                if (this.isAcceptableLandingPosition(param0, param1, var2)) {
                    Vec3 var3 = Vec3.atCenterOf(var2);
                    Vec3 var4 = this.calculateOptimalJumpVector(param1, var3);
                    if (var4 != null) {
                        param1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(var2));
                        PathNavigation var5 = param1.getNavigation();
                        Path var6 = var5.createPath(var2, 0, 8);
                        if (var6 == null || !var6.canReach()) {
                            this.chosenJump = var4;
                            this.prepareJumpStart = param2;
                            return;
                        }
                    }
                }
            }
        }

    }

    protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel param0) {
        Optional<LongJumpToRandomPos.PossibleJump> var0 = WeightedRandom.getRandomItem(param0.random, this.jumpCandidates);
        var0.ifPresent(this.jumpCandidates::remove);
        return var0;
    }

    private boolean isAcceptableLandingPosition(ServerLevel param0, E param1, BlockPos param2) {
        BlockPos var0 = param1.blockPosition();
        int var1 = var0.getX();
        int var2 = var0.getZ();
        return var1 == param2.getX() && var2 == param2.getZ() ? false : this.acceptableLandingSpot.test(param1, param2);
    }

    @Nullable
    protected Vec3 calculateOptimalJumpVector(Mob param0, Vec3 param1) {
        List<Integer> var0 = Lists.newArrayList(ALLOWED_ANGLES);
        Collections.shuffle(var0);

        for(int var1 : var0) {
            Optional<Vec3> var2 = LongJumpUtil.calculateJumpVectorForAngle(param0, param1, this.maxJumpVelocity, var1, true);
            if (var2.isPresent()) {
                return var2.get();
            }
        }

        return null;
    }

    public static class PossibleJump extends WeightedEntry.IntrusiveBase {
        private final BlockPos jumpTarget;

        public PossibleJump(BlockPos param0, int param1) {
            super(param1);
            this.jumpTarget = param0;
        }

        public BlockPos getJumpTarget() {
            return this.jumpTarget;
        }
    }
}

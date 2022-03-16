package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class Frog extends Animal {
    public static final Ingredient TEMPTATION_ITEM = Ingredient.of(Items.SLIME_BALL);
    protected static final ImmutableList<SensorType<? extends Sensor<? super Frog>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FROG_ATTACKABLES, SensorType.FROG_TEMPTATIONS, SensorType.IS_IN_WATER
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.NEAREST_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.BREED_TARGET,
        MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
        MemoryModuleType.LONG_JUMP_MID_JUMP,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.TEMPTING_PLAYER,
        MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
        MemoryModuleType.IS_TEMPTED,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.NEAREST_ATTACKABLE,
        MemoryModuleType.IS_IN_WATER,
        MemoryModuleType.IS_PREGNANT
    );
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<OptionalInt> DATA_TONGUE_TARGET_ID = SynchedEntityData.defineId(
        Frog.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT
    );
    private static final int FROG_FALL_DAMAGE_REDUCTION = 5;
    public final AnimationState jumpAnimationState = new AnimationState();
    public final AnimationState croakAnimationState = new AnimationState();
    public final AnimationState tongueAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState swimIdleAnimationState = new AnimationState();

    public Frog(EntityType<? extends Animal> param0, Level param1) {
        super(param0, param1);
        this.lookControl = new Frog.FrogLookControl(this);
        this.setPathfindingMalus(BlockPathTypes.WATER, 4.0F);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
        this.maxUpStep = 1.0F;
    }

    @Override
    protected Brain.Provider<Frog> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return FrogAi.makeBrain(this.brainProvider().makeBrain(param0));
    }

    @Override
    public Brain<Frog> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, 0);
        this.entityData.define(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
    }

    public void eraseTongueTarget() {
        this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
    }

    public Optional<Entity> getTongueTarget() {
        return this.entityData.get(DATA_TONGUE_TARGET_ID).stream().mapToObj(this.level::getEntity).filter(Objects::nonNull).findFirst();
    }

    public void setTongueTarget(Entity param0) {
        this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.of(param0.getId()));
    }

    @Override
    public int getHeadRotSpeed() {
        return 35;
    }

    @Override
    public int getMaxHeadYRot() {
        return 5;
    }

    public Frog.Variant getVariant() {
        return Frog.Variant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(Frog.Variant param0) {
        this.entityData.set(DATA_VARIANT, param0.getId());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Variant", this.getVariant().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setVariant(Frog.Variant.byId(param0.getInt("Variant")));
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    private boolean isMovingOnLand() {
        return this.onGround && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && !this.isInWaterOrBubble();
    }

    private boolean isMovingInWater() {
        return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && this.isInWaterOrBubble();
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("frogBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("frogActivityUpdate");
        FrogAi.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        if (this.level.isClientSide()) {
            if (this.isMovingOnLand()) {
                this.walkAnimationState.startIfStopped();
            } else {
                this.walkAnimationState.stop();
            }

            if (this.isMovingInWater()) {
                this.swimIdleAnimationState.stop();
                this.swimAnimationState.startIfStopped();
            } else if (this.isInWaterOrBubble()) {
                this.swimAnimationState.stop();
                this.swimIdleAnimationState.startIfStopped();
            } else {
                this.swimAnimationState.stop();
                this.swimIdleAnimationState.stop();
            }
        }

        super.tick();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_POSE.equals(param0)) {
            Pose var0 = this.getPose();
            if (var0 == Pose.LONG_JUMPING) {
                this.jumpAnimationState.start();
            } else {
                this.jumpAnimationState.stop();
            }

            if (var0 == Pose.CROAKING) {
                this.croakAnimationState.start();
            } else {
                this.croakAnimationState.stop();
            }

            if (var0 == Pose.USING_TONGUE) {
                this.tongueAnimationState.start();
            }
        }

        super.onSyncedDataUpdated(param0);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        Frog var0 = EntityType.FROG.create(param0);
        if (var0 != null) {
            FrogAi.initMemories(var0);
        }

        return var0;
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean param0) {
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel param0, Animal param1) {
        ServerPlayer var0 = this.getLoveCause();
        if (var0 == null) {
            var0 = param1.getLoveCause();
        }

        if (var0 != null) {
            var0.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(var0, this, param1, null);
        }

        this.setAge(6000);
        param1.setAge(6000);
        this.resetLove();
        param1.resetLove();
        this.getBrain().setMemory(MemoryModuleType.IS_PREGNANT, Unit.INSTANCE);
        param0.broadcastEntityEvent(this, (byte)18);
        if (param0.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            param0.addFreshEntity(new ExperienceOrb(param0, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
        }

    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        Holder<Biome> var0 = param0.getBiome(this.blockPosition());
        if (var0.is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
            this.setVariant(Frog.Variant.COLD);
        } else if (var0.is(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)) {
            this.setVariant(Frog.Variant.WARM);
        } else {
            this.setVariant(Frog.Variant.TEMPERATE);
        }

        FrogAi.initMemories(this);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.MAX_HEALTH, 10.0);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FROG_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.FROG_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FROG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.FROG_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    protected int calculateFallDamage(float param0, float param1) {
        return super.calculateFallDamage(param0, param1) - 5;
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), param0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        } else {
            super.travel(param0);
        }

    }

    public static boolean canEat(LivingEntity param0) {
        if (param0 instanceof Slime var0 && var0.getSize() == 1) {
            return true;
        }

        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new Frog.FrogPathNavigation(this, param0);
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return TEMPTATION_ITEM.test(param0);
    }

    class FrogLookControl extends LookControl {
        FrogLookControl(Mob param0) {
            super(param0);
        }

        @Override
        protected boolean resetXRotOnTick() {
            return Frog.this.getTongueTarget().isEmpty();
        }
    }

    static class FrogNodeEvaluator extends AmphibiousNodeEvaluator {
        private final BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();

        public FrogNodeEvaluator(boolean param0) {
            super(param0);
        }

        @Override
        public BlockPathTypes getBlockPathType(BlockGetter param0, int param1, int param2, int param3) {
            this.belowPos.set(param1, param2 - 1, param3);
            BlockState var0 = param0.getBlockState(this.belowPos);
            return var0.is(BlockTags.FROG_PREFER_JUMP_TO) ? BlockPathTypes.OPEN : getBlockPathTypeStatic(param0, this.belowPos.move(Direction.UP));
        }
    }

    static class FrogPathNavigation extends WaterBoundPathNavigation {
        FrogPathNavigation(Frog param0, Level param1) {
            super(param0, param1);
        }

        @Override
        protected PathFinder createPathFinder(int param0) {
            this.nodeEvaluator = new Frog.FrogNodeEvaluator(true);
            return new PathFinder(this.nodeEvaluator, param0);
        }

        @Override
        protected boolean canUpdatePath() {
            return true;
        }

        @Override
        public boolean isStableDestination(BlockPos param0) {
            return !this.level.getBlockState(param0.below()).isAir();
        }
    }

    public static enum Variant {
        TEMPERATE(0, "temperate"),
        WARM(1, "warm"),
        COLD(2, "cold");

        private static final Frog.Variant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(Frog.Variant::getId))
            .toArray(param0 -> new Frog.Variant[param0]);
        private final int id;
        private final String name;

        private Variant(int param0, String param1) {
            this.id = param0;
            this.name = param1;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public static Frog.Variant byId(int param0) {
            if (param0 < 0 || param0 >= BY_ID.length) {
                param0 = 0;
            }

            return BY_ID[param0];
        }
    }
}

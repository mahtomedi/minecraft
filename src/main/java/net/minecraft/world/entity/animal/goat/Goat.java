package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class Goat extends Animal {
    public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.scalable(0.9F, 1.3F).scale(0.7F);
    private static final int ADULT_ATTACK_DAMAGE = 2;
    private static final int BABY_ATTACK_DAMAGE = 1;
    protected static final ImmutableList<SensorType<? extends Sensor<? super Goat>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES,
        SensorType.NEAREST_PLAYERS,
        SensorType.NEAREST_ITEMS,
        SensorType.NEAREST_ADULT,
        SensorType.HURT_BY,
        SensorType.GOAT_TEMPTATIONS
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.ATE_RECENTLY,
        MemoryModuleType.BREED_TARGET,
        MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
        MemoryModuleType.LONG_JUMP_MID_JUMP,
        MemoryModuleType.TEMPTING_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ADULT,
        MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
        MemoryModuleType.IS_TEMPTED,
        MemoryModuleType.RAM_COOLDOWN_TICKS,
        MemoryModuleType.RAM_TARGET
    );
    public static final int GOAT_FALL_DAMAGE_REDUCTION = 10;
    public static final double GOAT_SCREAMING_CHANCE = 0.02;
    private static final EntityDataAccessor<Boolean> DATA_IS_SCREAMING_GOAT = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
    private boolean isLoweringHead;
    private int lowerHeadTick;

    public Goat(EntityType<? extends Goat> param0, Level param1) {
        super(param0, param1);
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
    }

    @Override
    protected Brain.Provider<Goat> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return GoatAi.makeBrain(this.brainProvider().makeBrain(param0));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2F).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void ageBoundaryReached() {
        if (this.isBaby()) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1.0);
        } else {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0);
        }

    }

    @Override
    protected int calculateFallDamage(float param0, float param1) {
        return super.calculateFallDamage(param0, param1) - 10;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_AMBIENT : SoundEvents.GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_HURT : SoundEvents.GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_DEATH : SoundEvents.GOAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.GOAT_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getMilkingSound() {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
    }

    public Goat getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        Goat var0 = EntityType.GOAT.create(param0);
        if (var0 != null) {
            GoatAi.initMemories(var0, param0.getRandom());
            boolean var1 = param1 instanceof Goat && ((Goat)param1).isScreamingGoat();
            var0.setScreamingGoat(var1 || param0.getRandom().nextDouble() < 0.02);
        }

        return var0;
    }

    @Override
    public Brain<Goat> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("goatBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("goatActivityUpdate");
        GoatAi.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public int getMaxHeadYRot() {
        return 15;
    }

    @Override
    public void setYHeadRot(float param0) {
        int var0 = this.getMaxHeadYRot();
        float var1 = Mth.degreesDifference(this.yBodyRot, param0);
        float var2 = Mth.clamp(var1, (float)(-var0), (float)var0);
        super.setYHeadRot(this.yBodyRot + var2);
    }

    @Override
    public SoundEvent getEatingSound(ItemStack param0) {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_EAT : SoundEvents.GOAT_EAT;
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.is(Items.BUCKET) && !this.isBaby()) {
            param0.playSound(this.getMilkingSound(), 1.0F, 1.0F);
            ItemStack var1 = ItemUtils.createFilledResult(var0, param0, Items.MILK_BUCKET.getDefaultInstance());
            param0.setItemInHand(param1, var1);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            InteractionResult var2 = super.mobInteract(param0, param1);
            if (var2.consumesAction() && this.isFood(var0)) {
                this.level.playSound(null, this, this.getEatingSound(var0), SoundSource.NEUTRAL, 1.0F, Mth.randomBetween(this.level.random, 0.8F, 1.2F));
            }

            return var2;
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        RandomSource var0 = param0.getRandom();
        GoatAi.initMemories(this, var0);
        this.setScreamingGoat(var0.nextDouble() < 0.02);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        return param0 == Pose.LONG_JUMPING ? LONG_JUMPING_DIMENSIONS.scale(this.getScale()) : super.getDimensions(param0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("IsScreamingGoat", this.isScreamingGoat());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setScreamingGoat(param0.getBoolean("IsScreamingGoat"));
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 58) {
            this.isLoweringHead = true;
        } else if (param0 == 59) {
            this.isLoweringHead = false;
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @Override
    public void aiStep() {
        if (this.isLoweringHead) {
            ++this.lowerHeadTick;
        } else {
            this.lowerHeadTick -= 2;
        }

        this.lowerHeadTick = Mth.clamp(this.lowerHeadTick, 0, 20);
        super.aiStep();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_SCREAMING_GOAT, false);
    }

    public boolean isScreamingGoat() {
        return this.entityData.get(DATA_IS_SCREAMING_GOAT);
    }

    public void setScreamingGoat(boolean param0) {
        this.entityData.set(DATA_IS_SCREAMING_GOAT, param0);
    }

    public float getRammingXHeadRot() {
        return (float)this.lowerHeadTick / 20.0F * 30.0F * (float) (Math.PI / 180.0);
    }

    public static boolean checkGoatSpawnRules(
        EntityType<? extends Animal> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4
    ) {
        return param1.getBlockState(param3.below()).is(BlockTags.GOATS_SPAWNABLE_ON) && isBrightEnoughToSpawn(param1, param3);
    }
}

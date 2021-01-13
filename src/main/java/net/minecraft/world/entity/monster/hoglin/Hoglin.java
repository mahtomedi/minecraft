package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Random;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Hoglin extends Animal implements Enemy, HoglinBase {
    private static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(Hoglin.class, EntityDataSerializers.BOOLEAN);
    private int attackAnimationRemainingTicks;
    private int timeInOverworld = 0;
    private boolean cannotBeHunted = false;
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Hoglin>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ADULT, SensorType.HOGLIN_SPECIFIC_SENSOR
    );
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.BREED_TARGET,
        MemoryModuleType.LIVING_ENTITIES,
        MemoryModuleType.VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.ATTACK_COOLING_DOWN,
        MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN,
        MemoryModuleType.AVOID_TARGET,
        MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
        MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
        MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS,
        MemoryModuleType.NEAREST_VISIBLE_ADULT,
        MemoryModuleType.NEAREST_REPELLENT,
        MemoryModuleType.PACIFIED
    );

    public Hoglin(EntityType<? extends Hoglin> param0, Level param1) {
        super(param0, param1);
        this.xpReward = 5;
    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return !this.isLeashed();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3F)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.6F)
            .add(Attributes.ATTACK_KNOCKBACK, 1.0)
            .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        if (!(param0 instanceof LivingEntity)) {
            return false;
        } else {
            this.attackAnimationRemainingTicks = 10;
            this.level.broadcastEntityEvent(this, (byte)4);
            this.playSound(SoundEvents.HOGLIN_ATTACK, 1.0F, this.getVoicePitch());
            HoglinAi.onHitTarget(this, (LivingEntity)param0);
            return HoglinBase.hurtAndThrowTarget(this, (LivingEntity)param0);
        }
    }

    @Override
    protected void blockedByShield(LivingEntity param0) {
        if (this.isAdult()) {
            HoglinBase.throwTarget(this, param0);
        }

    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        boolean var0 = super.hurt(param0, param1);
        if (this.level.isClientSide) {
            return false;
        } else {
            if (var0 && param0.getEntity() instanceof LivingEntity) {
                HoglinAi.wasHurtBy(this, (LivingEntity)param0.getEntity());
            }

            return var0;
        }
    }

    @Override
    protected Brain.Provider<Hoglin> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return HoglinAi.makeBrain(this.brainProvider().makeBrain(param0));
    }

    @Override
    public Brain<Hoglin> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("hoglinBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        HoglinAi.updateActivity(this);
        if (this.isConverting()) {
            ++this.timeInOverworld;
            if (this.timeInOverworld > 300) {
                this.playSound(SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED);
                this.finishConversion((ServerLevel)this.level);
            }
        } else {
            this.timeInOverworld = 0;
        }

    }

    @Override
    public void aiStep() {
        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
        }

        super.aiStep();
    }

    @Override
    protected void ageBoundaryReached() {
        if (this.isBaby()) {
            this.xpReward = 3;
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.5);
        } else {
            this.xpReward = 5;
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0);
        }

    }

    public static boolean checkHoglinSpawnRules(EntityType<Hoglin> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return !param1.getBlockState(param3.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param0.getRandom().nextFloat() < 0.2F) {
            this.setBaby(true);
        }

        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return !this.isPersistenceRequired();
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        if (HoglinAi.isPosNearNearestRepellent(this, param0)) {
            return -1.0F;
        } else {
            return param1.getBlockState(param0.below()).is(Blocks.CRIMSON_NYLIUM) ? 10.0F : 0.0F;
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        return (double)this.getBbHeight() - (this.isBaby() ? 0.2 : 0.15);
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        InteractionResult var0 = super.mobInteract(param0, param1);
        if (var0.consumesAction()) {
            this.setPersistenceRequired();
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 4) {
            this.attackAnimationRemainingTicks = 10;
            this.playSound(SoundEvents.HOGLIN_ATTACK, 1.0F, this.getVoicePitch());
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getAttackAnimationRemainingTicks() {
        return this.attackAnimationRemainingTicks;
    }

    @Override
    protected boolean shouldDropExperience() {
        return true;
    }

    @Override
    protected int getExperienceReward(Player param0) {
        return this.xpReward;
    }

    private void finishConversion(ServerLevel param0) {
        Zoglin var0 = this.convertTo(EntityType.ZOGLIN, true);
        if (var0 != null) {
            var0.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        }

    }

    @Override
    public boolean isFood(ItemStack param0) {
        return param0.getItem() == Items.CRIMSON_FUNGUS;
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (this.isImmuneToZombification()) {
            param0.putBoolean("IsImmuneToZombification", true);
        }

        param0.putInt("TimeInOverworld", this.timeInOverworld);
        if (this.cannotBeHunted) {
            param0.putBoolean("CannotBeHunted", true);
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setImmuneToZombification(param0.getBoolean("IsImmuneToZombification"));
        this.timeInOverworld = param0.getInt("TimeInOverworld");
        this.setCannotBeHunted(param0.getBoolean("CannotBeHunted"));
    }

    public void setImmuneToZombification(boolean param0) {
        this.getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, param0);
    }

    private boolean isImmuneToZombification() {
        return this.getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION);
    }

    public boolean isConverting() {
        return !this.level.dimensionType().piglinSafe() && !this.isImmuneToZombification() && !this.isNoAi();
    }

    private void setCannotBeHunted(boolean param0) {
        this.cannotBeHunted = param0;
    }

    public boolean canBeHunted() {
        return this.isAdult() && !this.cannotBeHunted;
    }

    @Nullable
    @Override
    public AgableMob getBreedOffspring(ServerLevel param0, AgableMob param1) {
        Hoglin var0 = EntityType.HOGLIN.create(param0);
        if (var0 != null) {
            var0.setPersistenceRequired();
        }

        return var0;
    }

    @Override
    public boolean canFallInLove() {
        return !HoglinAi.isPacified(this) && super.canFallInLove();
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.level.isClientSide ? null : HoglinAi.getSoundForCurrentActivity(this).orElse(null);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.HOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOGLIN_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.HOSTILE_SWIM;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.HOSTILE_SPLASH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.HOGLIN_STEP, 0.15F, 1.0F);
    }

    protected void playSound(SoundEvent param0) {
        this.playSound(param0, this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }
}

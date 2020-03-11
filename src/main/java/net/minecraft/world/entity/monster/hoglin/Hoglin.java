package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Hoglin extends Animal implements Enemy {
    private static final Logger LOGGER = LogManager.getLogger();
    private int attackAnimationRemainingTicks;
    private static int createCounter = 0;
    private static int dieCounter = 0;
    private static int killedByPiglinCounter = 0;
    private static int removeCounter = 0;
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Hoglin>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HOGLIN_SPECIFIC_SENSOR
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
        MemoryModuleType.NEAREST_REPELLENT,
        MemoryModuleType.PACIFIED
    );

    public Hoglin(EntityType<? extends Hoglin> param0, Level param1) {
        super(param0, param1);
        this.xpReward = 5;
    }

    @Override
    public void die(DamageSource param0) {
        super.die(param0);
    }

    @Override
    public void remove() {
        super.remove();
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4F);
        this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0);
    }

    private float getAttackDamage() {
        return (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        this.attackAnimationRemainingTicks = 10;
        this.level.broadcastEntityEvent(this, (byte)4);
        float var0 = this.getAttackDamage();
        float var2;
        if (!this.isAdult() && (int)var0 <= 0) {
            var2 = var0;
        } else {
            var2 = var0 / 2.0F + (float)this.random.nextInt((int)var0);
        }

        boolean var3 = param0.hurt(DamageSource.mobAttack(this), var2);
        if (var3) {
            this.doEnchantDamageEffects(this, param0);
            if (this.isAdult()) {
                this.throwTarget(param0);
            }
        }

        this.playSound(SoundEvents.HOGLIN_ATTACK, 1.0F, this.getVoicePitch());
        if (param0 instanceof LivingEntity) {
            HoglinAi.onHitTarget(this, (LivingEntity)param0);
        }

        return var3;
    }

    private void throwTarget(Entity param0) {
        param0.setDeltaMovement(
            param0.getDeltaMovement()
                .add((double)((this.random.nextFloat() - 0.5F) * 0.5F), (double)(this.random.nextFloat() * 0.5F), (double)(this.random.nextFloat() * -0.5F))
        );
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
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return HoglinAi.makeBrain(this, param0);
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
        HoglinAi.maybePlayActivitySound(this);
    }

    @Override
    public void aiStep() {
        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
        }

        super.aiStep();
    }

    public static boolean checkHoglinSpawnRules(EntityType<Hoglin> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return param1.getBlockState(param3.below()).getBlock() != Blocks.NETHER_WART_BLOCK;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param0.getRandom().nextFloat() < 0.2F) {
            this.setBaby(true);
            this.xpReward = 3;
            this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.5);
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
            return param1.getBlockState(param0.below()).getBlock() == Blocks.CRIMSON_NYLIUM ? 10.0F : 0.0F;
        }
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        boolean var0 = super.mobInteract(param0, param1);
        if (var0) {
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

    @Override
    public boolean isFood(ItemStack param0) {
        return param0.getItem() == Items.CRIMSON_FUNGUS;
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    @Nullable
    @Override
    public AgableMob getBreedOffspring(AgableMob param0) {
        Hoglin var0 = EntityType.HOGLIN.create(this.level);
        if (var0 != null) {
            var0.setPersistenceRequired();
        }

        return var0;
    }

    protected float getMovementSpeed() {
        return (float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
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
        return SoundEvents.HOGLIN_AMBIENT;
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
    public void playAmbientSound() {
        if (HoglinAi.isIdle(this)) {
            super.playAmbientSound();
        }

    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.HOGLIN_STEP, 0.15F, 1.0F);
    }

    protected void playAngrySound() {
        this.playSound(SoundEvents.HOGLIN_ANGRY, 1.0F, this.getVoicePitch());
    }

    protected void playRetreatSound() {
        this.playSound(SoundEvents.HOGLIN_RETREAT, 1.0F, this.getVoicePitch());
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }
}

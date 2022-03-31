package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Warden extends Monster implements VibrationListener.VibrationListenerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int GAME_EVENT_LISTENER_RANGE = 16;
    private static final int VIBRATION_COOLDOWN_TICKS = 40;
    private static final int MAX_HEALTH = 500;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
    private static final float KNOCKBACK_RESISTANCE = 1.0F;
    private static final float ATTACK_KNOCKBACK = 1.5F;
    private static final int ATTACK_DAMAGE = 30;
    private static final EntityDataAccessor<Integer> CLIENT_ANGER_LEVEL = SynchedEntityData.defineId(Warden.class, EntityDataSerializers.INT);
    private static final int DARKNESS_DISPLAY_LIMIT = 200;
    private static final int DARKNESS_DURATION = 260;
    private static final int DARKNESS_RADIUS = 20;
    private static final int DARKNESS_INTERVAL = 120;
    private static final int ANGERMANAGEMENT_TICK_DELAY = 20;
    private static final int ANGER_DECAY_SOUND_TRESHOLD = 70;
    private static final int DEFAULT_ANGER = 35;
    private static final int PROJECTILE_ANGER = 20;
    private static final int RECENT_PROJECTILE_TICK_THRESHOLD = 100;
    private static final int TOUCH_COOLDOWN_TICKS = 20;
    private static final int DIGGING_PARTICLES_AMOUNT = 30;
    private static final float DIGGING_PARTICLES_DURATION = 4.5F;
    private static final float DIGGING_PARTICLES_OFFSET = 0.7F;
    private float earAnimation;
    private float earAnimationO;
    private float heartAnimation;
    private float heartAnimationO;
    public AnimationState roarAnimationState = new AnimationState();
    public AnimationState sniffAnimationState = new AnimationState();
    public AnimationState emergeAnimationState = new AnimationState();
    public AnimationState diggingAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    private final DynamicGameEventListener dynamicGameEventListener;
    private VibrationListener vibrationListener;
    private AngerManagement angerManagement = new AngerManagement(Collections.emptyMap());

    public Warden(EntityType<? extends Monster> param0, Level param1) {
        super(param0, param1);
        this.vibrationListener = new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 16, this, null, 0, 0);
        this.dynamicGameEventListener = new DynamicGameEventListener(this.vibrationListener);
        this.xpReward = 5;
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return param0.noCollision(this);
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return 0.0F;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource param0) {
        return this.hasPose(Pose.DIGGING) || this.hasPose(Pose.EMERGING) || super.isInvulnerableTo(param0);
    }

    @Override
    protected boolean canRide(Entity param0) {
        return false;
    }

    @Override
    public boolean canDisableShield() {
        return true;
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.55F;
    }

    @Override
    public double getFluidJumpThreshold() {
        return (double)this.getEyeHeight() * 0.9;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> param0) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.4, 0.0));
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 500.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3F)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.ATTACK_KNOCKBACK, 1.5)
            .add(Attributes.ATTACK_DAMAGE, 30.0);
    }

    @Override
    public boolean occludesVibrations() {
        return true;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasPose(Pose.ROARING) ? null : this.getAngerLevel().getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.WARDEN_STEP, 10.0F, 1.0F);
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        this.level.broadcastEntityEvent(this, (byte)4);
        this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 10.0F, this.getVoicePitch());
        return super.doHurtTarget(param0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CLIENT_ANGER_LEVEL, 0);
    }

    public int getClientAngerLevel() {
        return this.entityData.get(CLIENT_ANGER_LEVEL);
    }

    private void syncClientAngerLevel() {
        this.entityData.set(CLIENT_ANGER_LEVEL, this.angerManagement.getActiveAnger());
    }

    @Override
    public void tick() {
        Level var2 = this.level;
        if (var2 instanceof ServerLevel var0) {
            this.vibrationListener.tick(var0);
            if (this.hasCustomName()) {
                WardenAi.setDigCooldown(this);
            }
        }

        super.tick();
        if (this.level.isClientSide()) {
            if (this.tickCount % this.getHeartBeatDelay() == 0) {
                this.heartAnimation = 0.0F;
                this.level
                    .playLocalSound(
                        this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, this.getSoundSource(), 5.0F, this.getVoicePitch(), false
                    );
            }

            this.earAnimationO = this.earAnimation;
            if (this.earAnimation < 1.0F) {
                this.earAnimation += 0.1F;
            }

            this.heartAnimationO = this.heartAnimation;
            if (this.heartAnimation < 1.0F) {
                this.heartAnimation += 0.1F;
            }

            switch(this.getPose()) {
                case EMERGING:
                    this.clientDiggingParticles(this.emergeAnimationState);
                    break;
                case DIGGING:
                    this.clientDiggingParticles(this.diggingAnimationState);
            }
        }

    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("wardenBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
        if ((this.tickCount + this.getId()) % 120 == 0) {
            applyDarknessAround((ServerLevel)this.level, this.position(), this, 20);
        }

        if (this.tickCount % 20 == 0) {
            int var0 = this.angerManagement.getActiveAnger();
            this.angerManagement.tick();
            this.onDecayedAnger(var0, this.angerManagement.getActiveAnger());
        }

        this.syncClientAngerLevel();
        WardenAi.updateActivity(this);
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 4) {
            this.attackAnimationState.start();
        } else if (param0 == 61) {
            this.earAnimation = 0.0F;
        } else {
            super.handleEntityEvent(param0);
        }

    }

    private int getHeartBeatDelay() {
        float var0 = (float)this.getClientAngerLevel() / (float)AngerLevel.ANGRY.getMinimumAnger();
        return 40 - Mth.floor(Mth.clamp(var0, 0.0F, 1.0F) * 30.0F);
    }

    public float getTendrilAnimation(float param0) {
        return Math.max(1.0F - Mth.lerp(param0, this.earAnimationO, this.earAnimation), 0.0F);
    }

    public float getHeartAnimation(float param0) {
        return Math.max(1.0F - Mth.lerp(param0, this.heartAnimationO, this.heartAnimation), 0.0F);
    }

    private void clientDiggingParticles(AnimationState param0) {
        if ((float)(Util.getMillis() - param0.startTime()) < 4500.0F) {
            Random var0 = this.getRandom();
            BlockState var1 = this.level.getBlockState(this.blockPosition().below());

            for(int var2 = 0; var2 < 30; ++var2) {
                double var3 = this.getX() + (double)Mth.randomBetween(var0, -0.7F, 0.7F);
                double var4 = this.getY();
                double var5 = this.getZ() + (double)Mth.randomBetween(var0, -0.7F, 0.7F);
                this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, var1), var3, var4, var5, 0.0, 0.0, 0.0);
            }
        }

    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_POSE.equals(param0)) {
            switch(this.getPose()) {
                case EMERGING:
                    this.emergeAnimationState.start();
                    break;
                case DIGGING:
                    this.diggingAnimationState.start();
                    break;
                case ROARING:
                    this.roarAnimationState.start();
                    break;
                case SNIFFING:
                    this.sniffAnimationState.start();
            }
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return WardenAi.makeBrain(this, param0);
    }

    @Override
    public Brain<Warden> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener, ServerLevel> param0) {
        Level var3 = this.level;
        if (var3 instanceof ServerLevel var0) {
            param0.accept(this.dynamicGameEventListener, var0);
        }

    }

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return GameEventTags.WARDEN_EVENTS_CAN_LISTEN;
    }

    public static boolean canTargetEntity(@Nullable Entity param0) {
        if (param0 instanceof LivingEntity var0
            && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(param0)
            && var0.getType() != EntityType.ARMOR_STAND
            && var0.getType() != EntityType.WARDEN
            && !var0.isDeadOrDying()) {
            return true;
        }

        return false;
    }

    public static void applyDarknessAround(ServerLevel param0, Vec3 param1, @Nullable Entity param2, int param3) {
        MobEffectInstance var0 = new MobEffectInstance(MobEffects.DARKNESS, 260, 0, false, false);
        MobEffectUtil.addEffectToPlayersAround(param0, param2, param1, (double)param3, var0, 200);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        AngerManagement.CODEC
            .encodeStart(NbtOps.INSTANCE, this.angerManagement)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("anger", param1));
        VibrationListener.codec(this)
            .encodeStart(NbtOps.INSTANCE, this.vibrationListener)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("listener", param1));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("anger")) {
            AngerManagement.CODEC
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.get("anger")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.angerManagement = param0x);
            this.syncClientAngerLevel();
        }

        if (param0.contains("listener", 10)) {
            VibrationListener.codec(this)
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.getCompound("listener")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.vibrationListener = param0x);
        }

    }

    private void onDecayedAnger(int param0, int param1) {
        if (param0 >= 70 && param1 < 70) {
            this.playListeningSound();
        }

    }

    private void playListeningSound() {
        if (!this.hasPose(Pose.ROARING)) {
            SoundEvent var0 = this.getAngerLevel() == AngerLevel.CALM ? SoundEvents.WARDEN_LISTENING : SoundEvents.WARDEN_LISTENING_ANGRY;
            this.playSound(var0, 10.0F, this.getVoicePitch());
        }

    }

    public AngerLevel getAngerLevel() {
        return AngerLevel.byAnger(this.angerManagement.getActiveAnger());
    }

    public void clearAnger(Entity param0) {
        this.angerManagement.clearAnger(param0);
    }

    public void increaseAngerAt(@Nullable Entity param0) {
        this.increaseAngerAt(param0, param0 instanceof Projectile ? 20 : 35);
    }

    @VisibleForTesting
    public void increaseAngerAt(@Nullable Entity param0, int param1) {
        if (canTargetEntity(param0)) {
            WardenAi.setDigCooldown(this);
            boolean var0 = this.getEntityAngryAt().filter(param0x -> !(param0x instanceof Player)).isPresent();
            int var1 = this.angerManagement.addAnger(param0, param1);
            if (param0 instanceof Player var2 && var0 && AngerLevel.byAnger(var1) == AngerLevel.ANGRY) {
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, var2);
            }

            this.playListeningSound();
        }

    }

    public Optional<LivingEntity> getEntityAngryAt() {
        return this.getAngerLevel() == AngerLevel.ANGRY ? this.angerManagement.getActiveEntity(this.level) : Optional.empty();
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    public double getMeleeAttackRangeSqr(LivingEntity param0) {
        return 8.0;
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity param0) {
        double var0 = this.distanceToSqr(param0.getX(), param0.getY() - (double)(this.getBbHeight() / 2.0F), param0.getZ());
        return var0 <= this.getMeleeAttackRangeSqr(param0);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        boolean var0 = super.hurt(param0, param1);
        if (this.level.isClientSide) {
            return false;
        } else {
            if (var0) {
                Entity var1 = param0.getEntity();
                this.increaseAngerAt(var1, AngerLevel.ANGRY.getMinimumAnger() + 20);
                if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()
                    && var1 instanceof LivingEntity var2
                    && (!(param0 instanceof IndirectEntityDamageSource) || this.closerThan(var2, 5.0))) {
                    this.brain.setMemory(MemoryModuleType.ATTACK_TARGET, var2);
                }
            }

            return var0;
        }
    }

    @Override
    public void playerTouch(Player param0) {
        if (!this.getBrain().hasMemoryValue(MemoryModuleType.TOUCH_COOLDOWN)) {
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.TOUCH_COOLDOWN, Unit.INSTANCE, 20L);
            this.increaseAngerAt(param0);
            WardenAi.setDisturbanceLocation(this, param0.blockPosition());
        }

    }

    @Override
    public boolean shouldListen(ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable Entity param4) {
        if (this.getBrain().getMemory(MemoryModuleType.VIBRATION_COOLDOWN).isPresent()) {
            return false;
        } else {
            Pose var0 = this.getPose();
            if (var0 != Pose.DIGGING && var0 != Pose.EMERGING) {
                return !(param4 instanceof LivingEntity) || canTargetEntity(param4);
            } else {
                return false;
            }
        }
    }

    @Override
    public void onSignalReceive(
        ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable Entity param4, @Nullable Entity param5, int param6
    ) {
        this.brain.setMemoryWithExpiry(MemoryModuleType.VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);
        param0.broadcastEntityEvent(this, (byte)61);
        this.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 5.0F, this.getVoicePitch());
        BlockPos var0 = param2;
        if (param5 != null) {
            if (this.getBrain().hasMemoryValue(MemoryModuleType.RECENT_PROJECTILE) && canTargetEntity(param5) && this.closerThan(param5, 30.0)) {
                var0 = param5.blockPosition();
                this.increaseAngerAt(param5);
            }

            this.getBrain().setMemoryWithExpiry(MemoryModuleType.RECENT_PROJECTILE, Unit.INSTANCE, 100L);
        } else {
            this.increaseAngerAt(param4);
        }

        if (this.getAngerLevel() != AngerLevel.ANGRY
            && (param5 != null || this.angerManagement.getActiveEntity(param0).map(param1x -> param1x == param4).orElse(true))) {
            WardenAi.setDisturbanceLocation(this, var0);
        }

    }

    @VisibleForTesting
    public AngerManagement getAngerManagement() {
        return this.angerManagement;
    }
}

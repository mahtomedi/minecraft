package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;

public class Warden extends Monster implements VibrationListener.VibrationListenerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int GAME_EVENT_LISTENER_RANGE = 16;
    private static final int VIBRATION_COOLDOWN_TICKS = 40;
    private static final int TIME_TO_USE_MELEE_UNTIL_SONIC_BOOM = 200;
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
    private static final int DEFAULT_ANGER = 35;
    private static final int PROJECTILE_ANGER = 10;
    private static final int ON_HURT_ANGER_BOOST = 20;
    private static final int RECENT_PROJECTILE_TICK_THRESHOLD = 100;
    private static final int TOUCH_COOLDOWN_TICKS = 20;
    private static final int DIGGING_PARTICLES_AMOUNT = 30;
    private static final float DIGGING_PARTICLES_DURATION = 4.5F;
    private static final float DIGGING_PARTICLES_OFFSET = 0.7F;
    private static final int PROJECTILE_ANGER_DISTANCE = 30;
    private int tendrilAnimation;
    private int tendrilAnimationO;
    private int heartAnimation;
    private int heartAnimationO;
    public AnimationState roarAnimationState = new AnimationState();
    public AnimationState sniffAnimationState = new AnimationState();
    public AnimationState emergeAnimationState = new AnimationState();
    public AnimationState diggingAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState sonicBoomAnimationState = new AnimationState();
    private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;
    private AngerManagement angerManagement = new AngerManagement(this::canTargetEntity, Collections.emptyList());

    public Warden(EntityType<? extends Monster> param0, Level param1) {
        super(param0, param1);
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 16, this));
        this.xpReward = 5;
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, this.hasPose(Pose.EMERGING) ? 1 : 0);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket param0) {
        super.recreateFromPacket(param0);
        if (param0.getData() == 1) {
            this.setPose(Pose.EMERGING);
        }

    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return super.checkSpawnObstruction(param0) && param0.noCollision(this, this.getType().getDimensions().makeBoundingBox(this.position()));
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return 0.0F;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource param0) {
        return this.isDiggingOrEmerging() && !param0.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? true : super.isInvulnerableTo(param0);
    }

    private boolean isDiggingOrEmerging() {
        return this.hasPose(Pose.DIGGING) || this.hasPose(Pose.EMERGING);
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

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 500.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3F)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.ATTACK_KNOCKBACK, 1.5)
            .add(Attributes.ATTACK_DAMAGE, 30.0);
    }

    @Override
    public boolean dampensVibrations() {
        return true;
    }

    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return !this.hasPose(Pose.ROARING) && !this.isDiggingOrEmerging() ? this.getAngerLevel().getAmbientSound() : null;
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
        SonicBoom.setCooldown(this, 40);
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
        this.entityData.set(CLIENT_ANGER_LEVEL, this.getActiveAnger());
    }

    @Override
    public void tick() {
        Level var2 = this.level;
        if (var2 instanceof ServerLevel var0) {
            this.dynamicGameEventListener.getListener().tick(var0);
            if (this.isPersistenceRequired() || this.requiresCustomPersistence()) {
                WardenAi.setDigCooldown(this);
            }
        }

        super.tick();
        if (this.level.isClientSide()) {
            if (this.tickCount % this.getHeartBeatDelay() == 0) {
                this.heartAnimation = 10;
                if (!this.isSilent()) {
                    this.level
                        .playLocalSound(
                            this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, this.getSoundSource(), 5.0F, this.getVoicePitch(), false
                        );
                }
            }

            this.tendrilAnimationO = this.tendrilAnimation;
            if (this.tendrilAnimation > 0) {
                --this.tendrilAnimation;
            }

            this.heartAnimationO = this.heartAnimation;
            if (this.heartAnimation > 0) {
                --this.heartAnimation;
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
        ServerLevel var0 = (ServerLevel)this.level;
        var0.getProfiler().push("wardenBrain");
        this.getBrain().tick(var0, this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
        if ((this.tickCount + this.getId()) % 120 == 0) {
            applyDarknessAround(var0, this.position(), this, 20);
        }

        if (this.tickCount % 20 == 0) {
            this.angerManagement.tick(var0, this::canTargetEntity);
            this.syncClientAngerLevel();
        }

        WardenAi.updateActivity(this);
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 4) {
            this.roarAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
        } else if (param0 == 61) {
            this.tendrilAnimation = 10;
        } else if (param0 == 62) {
            this.sonicBoomAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(param0);
        }

    }

    private int getHeartBeatDelay() {
        float var0 = (float)this.getClientAngerLevel() / (float)AngerLevel.ANGRY.getMinimumAnger();
        return 40 - Mth.floor(Mth.clamp(var0, 0.0F, 1.0F) * 30.0F);
    }

    public float getTendrilAnimation(float param0) {
        return (float)Mth.lerp(param0, this.tendrilAnimationO, this.tendrilAnimation) / 10.0F;
    }

    public float getHeartAnimation(float param0) {
        return (float)Mth.lerp(param0, this.heartAnimationO, this.heartAnimation) / 10.0F;
    }

    private void clientDiggingParticles(AnimationState param0) {
        if ((float)param0.getAccumulatedTime() < 4500.0F) {
            RandomSource var0 = this.getRandom();
            BlockState var1 = this.getBlockStateOn();
            if (var1.getRenderShape() != RenderShape.INVISIBLE) {
                for(int var2 = 0; var2 < 30; ++var2) {
                    double var3 = this.getX() + (double)Mth.randomBetween(var0, -0.7F, 0.7F);
                    double var4 = this.getY();
                    double var5 = this.getZ() + (double)Mth.randomBetween(var0, -0.7F, 0.7F);
                    this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, var1), var3, var4, var5, 0.0, 0.0, 0.0);
                }
            }
        }

    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_POSE.equals(param0)) {
            switch(this.getPose()) {
                case EMERGING:
                    this.emergeAnimationState.start(this.tickCount);
                    break;
                case DIGGING:
                    this.diggingAnimationState.start(this.tickCount);
                    break;
                case ROARING:
                    this.roarAnimationState.start(this.tickCount);
                    break;
                case SNIFFING:
                    this.sniffAnimationState.start(this.tickCount);
            }
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    public boolean ignoreExplosion() {
        return this.isDiggingOrEmerging();
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
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> param0) {
        Level var3 = this.level;
        if (var3 instanceof ServerLevel var0) {
            param0.accept(this.dynamicGameEventListener, var0);
        }

    }

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return GameEventTags.WARDEN_CAN_LISTEN;
    }

    @Override
    public boolean canTriggerAvoidVibration() {
        return true;
    }

    @Contract("null->false")
    public boolean canTargetEntity(@Nullable Entity param0x) {
        if (param0x instanceof LivingEntity var0
            && this.level == param0x.level
            && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(param0x)
            && !this.isAlliedTo(param0x)
            && var0.getType() != EntityType.ARMOR_STAND
            && var0.getType() != EntityType.WARDEN
            && !var0.isInvulnerable()
            && !var0.isDeadOrDying()
            && this.level.getWorldBorder().isWithinBounds(var0.getBoundingBox())) {
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
        AngerManagement.codec(this::canTargetEntity)
            .encodeStart(NbtOps.INSTANCE, this.angerManagement)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("anger", param1));
        VibrationListener.codec(this)
            .encodeStart(NbtOps.INSTANCE, this.dynamicGameEventListener.getListener())
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("listener", param1));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("anger")) {
            AngerManagement.codec(this::canTargetEntity)
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.get("anger")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.angerManagement = param0x);
            this.syncClientAngerLevel();
        }

        if (param0.contains("listener", 10)) {
            VibrationListener.codec(this)
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.getCompound("listener")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.dynamicGameEventListener.updateListener(param0x, this.level));
        }

    }

    private void playListeningSound() {
        if (!this.hasPose(Pose.ROARING)) {
            this.playSound(this.getAngerLevel().getListeningSound(), 10.0F, this.getVoicePitch());
        }

    }

    public AngerLevel getAngerLevel() {
        return AngerLevel.byAnger(this.getActiveAnger());
    }

    private int getActiveAnger() {
        return this.angerManagement.getActiveAnger(this.getTarget());
    }

    public void clearAnger(Entity param0) {
        this.angerManagement.clearAnger(param0);
    }

    public void increaseAngerAt(@Nullable Entity param0) {
        this.increaseAngerAt(param0, 35, true);
    }

    @VisibleForTesting
    public void increaseAngerAt(@Nullable Entity param0, int param1, boolean param2) {
        if (!this.isNoAi() && this.canTargetEntity(param0)) {
            WardenAi.setDigCooldown(this);
            boolean var0 = !(this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) instanceof Player);
            int var1 = this.angerManagement.increaseAnger(param0, param1);
            if (param0 instanceof Player && var0 && AngerLevel.byAnger(var1).isAngry()) {
                this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }

            if (param2) {
                this.playListeningSound();
            }
        }

    }

    public Optional<LivingEntity> getEntityAngryAt() {
        return this.getAngerLevel().isAngry() ? this.angerManagement.getActiveEntity() : Optional.empty();
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return false;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        if (param2 == MobSpawnType.TRIGGERED) {
            this.setPose(Pose.EMERGING);
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, (long)WardenAi.EMERGE_DURATION);
            this.playSound(SoundEvents.WARDEN_AGITATED, 5.0F, 1.0F);
        }

        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        boolean var0 = super.hurt(param0, param1);
        if (!this.level.isClientSide && !this.isNoAi() && !this.isDiggingOrEmerging()) {
            Entity var1 = param0.getEntity();
            this.increaseAngerAt(var1, AngerLevel.ANGRY.getMinimumAnger() + 20, false);
            if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()
                && var1 instanceof LivingEntity var2
                && (!param0.isIndirect() || this.closerThan(var2, 5.0))) {
                this.setAttackTarget(var2);
            }
        }

        return var0;
    }

    public void setAttackTarget(LivingEntity param0) {
        this.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
        this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, param0);
        this.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        SonicBoom.setCooldown(this, 200);
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        EntityDimensions var0 = super.getDimensions(param0);
        return this.isDiggingOrEmerging() ? EntityDimensions.fixed(var0.width, 1.0F) : var0;
    }

    @Override
    public boolean isPushable() {
        return !this.isDiggingOrEmerging() && super.isPushable();
    }

    @Override
    protected void doPush(Entity param0) {
        if (!this.isNoAi() && !this.getBrain().hasMemoryValue(MemoryModuleType.TOUCH_COOLDOWN)) {
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.TOUCH_COOLDOWN, Unit.INSTANCE, 20L);
            this.increaseAngerAt(param0);
            WardenAi.setDisturbanceLocation(this, param0.blockPosition());
        }

        super.doPush(param0);
    }

    @Override
    public boolean shouldListen(ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, GameEvent.Context param4) {
        if (!this.isNoAi()
            && !this.isDeadOrDying()
            && !this.getBrain().hasMemoryValue(MemoryModuleType.VIBRATION_COOLDOWN)
            && !this.isDiggingOrEmerging()
            && param0.getWorldBorder().isWithinBounds(param2)) {
            Entity var7 = param4.sourceEntity();
            if (var7 instanceof LivingEntity var0 && !this.canTargetEntity(var0)) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSignalReceive(
        ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable Entity param4, @Nullable Entity param5, float param6
    ) {
        if (!this.isDeadOrDying()) {
            this.brain.setMemoryWithExpiry(MemoryModuleType.VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);
            param0.broadcastEntityEvent(this, (byte)61);
            this.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 5.0F, this.getVoicePitch());
            BlockPos var0 = param2;
            if (param5 != null) {
                if (this.closerThan(param5, 30.0)) {
                    if (this.getBrain().hasMemoryValue(MemoryModuleType.RECENT_PROJECTILE)) {
                        if (this.canTargetEntity(param5)) {
                            var0 = param5.blockPosition();
                        }

                        this.increaseAngerAt(param5);
                    } else {
                        this.increaseAngerAt(param5, 10, true);
                    }
                }

                this.getBrain().setMemoryWithExpiry(MemoryModuleType.RECENT_PROJECTILE, Unit.INSTANCE, 100L);
            } else {
                this.increaseAngerAt(param4);
            }

            if (!this.getAngerLevel().isAngry()) {
                Optional<LivingEntity> var1 = this.angerManagement.getActiveEntity();
                if (param5 != null || var1.isEmpty() || var1.get() == param4) {
                    WardenAi.setDisturbanceLocation(this, var0);
                }
            }

        }
    }

    @VisibleForTesting
    public AngerManagement getAngerManagement() {
        return this.angerManagement;
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new GroundPathNavigation(this, param0) {
            @Override
            protected PathFinder createPathFinder(int param0) {
                this.nodeEvaluator = new WalkNodeEvaluator();
                this.nodeEvaluator.setCanPassDoors(true);
                return new PathFinder(this.nodeEvaluator, param0) {
                    @Override
                    protected float distance(Node param0, Node param1) {
                        return param0.distanceToXZ(param1);
                    }
                };
            }
        };
    }
}

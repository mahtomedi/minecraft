package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Allay extends PathfinderMob implements InventoryCarrier {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int VIBRATION_EVENT_LISTENER_RANGE = 16;
    private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 1, 1);
    private static final int LIFTING_ITEM_ANIMATION_DURATION = 5;
    private static final float DANCING_LOOP_DURATION = 55.0F;
    private static final float SPINNING_ANIMATION_DURATION = 15.0F;
    private static final Ingredient DUPLICATION_ITEM = Ingredient.of(Items.AMETHYST_SHARD);
    private static final int DUPLICATION_COOLDOWN_TICKS = 6000;
    private static final int NUM_OF_DUPLICATION_HEARTS = 3;
    private static final double RIDING_OFFSET = 0.4;
    private static final EntityDataAccessor<Boolean> DATA_DANCING = SynchedEntityData.defineId(Allay.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAN_DUPLICATE = SynchedEntityData.defineId(Allay.class, EntityDataSerializers.BOOLEAN);
    protected static final ImmutableList<SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.PATH,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
        MemoryModuleType.LIKED_PLAYER,
        MemoryModuleType.LIKED_NOTEBLOCK_POSITION,
        MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS,
        MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
        MemoryModuleType.IS_PANICKING
    );
    public static final ImmutableList<Float> THROW_SOUND_PITCHES = ImmutableList.of(
        0.5625F, 0.625F, 0.75F, 0.9375F, 1.0F, 1.0F, 1.125F, 1.25F, 1.5F, 1.875F, 2.0F, 2.25F, 2.5F, 3.0F, 3.75F, 4.0F
    );
    private final DynamicGameEventListener<VibrationListener> dynamicVibrationListener;
    private final VibrationListener.VibrationListenerConfig vibrationListenerConfig;
    private final DynamicGameEventListener<Allay.JukeboxListener> dynamicJukeboxListener;
    private final SimpleContainer inventory = new SimpleContainer(1);
    @Nullable
    private BlockPos jukeboxPos;
    private long duplicationCooldown;
    private float holdingItemAnimationTicks;
    private float holdingItemAnimationTicks0;
    private float dancingAnimationTicks;
    private float spinningAnimationTicks;
    private float spinningAnimationTicks0;

    public Allay(EntityType<? extends Allay> param0, Level param1) {
        super(param0, param1);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setCanPickUpLoot(this.canPickUpLoot());
        PositionSource var0 = new EntityPositionSource(this, this.getEyeHeight());
        this.vibrationListenerConfig = new Allay.AllayVibrationListenerConfig();
        this.dynamicVibrationListener = new DynamicGameEventListener<>(new VibrationListener(var0, 16, this.vibrationListenerConfig));
        this.dynamicJukeboxListener = new DynamicGameEventListener<>(new Allay.JukeboxListener(var0, GameEvent.JUKEBOX_PLAY.getNotificationRadius()));
    }

    @Override
    protected Brain.Provider<Allay> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return AllayAi.makeBrain(this.brainProvider().makeBrain(param0));
    }

    @Override
    public Brain<Allay> getBrain() {
        return super.getBrain();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.FLYING_SPEED, 0.1F)
            .add(Attributes.MOVEMENT_SPEED, 0.1F)
            .add(Attributes.ATTACK_DAMAGE, 2.0)
            .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        FlyingPathNavigation var0 = new FlyingPathNavigation(this, param0);
        var0.setCanOpenDoors(false);
        var0.setCanFloat(true);
        var0.setCanPassDoors(true);
        return var0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DANCING, false);
        this.entityData.define(DATA_CAN_DUPLICATE, true);
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, param0);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, param0);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                this.moveRelative(this.getSpeed(), param0);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
            }
        }

        this.calculateEntityAnimation(false);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.6F;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        Entity var1 = param0.getEntity();
        if (var1 instanceof Player var0) {
            Optional<UUID> var1x = this.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
            if (var1x.isPresent() && var0.getUUID().equals(var1x.get())) {
                return false;
            }
        }

        return super.hurt(param0, param1);
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
    }

    @Override
    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasItemInSlot(EquipmentSlot.MAINHAND) ? SoundEvents.ALLAY_AMBIENT_WITH_ITEM : SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("allayBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("allayActivityUpdate");
        AllayAi.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide && this.isAlive() && this.tickCount % 10 == 0) {
            this.heal(1.0F);
        }

        if (this.isDancing() && this.shouldStopDancing() && this.tickCount % 20 == 0) {
            this.setDancing(false);
            this.jukeboxPos = null;
        }

        this.updateDuplicationCooldown();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            this.holdingItemAnimationTicks0 = this.holdingItemAnimationTicks;
            if (this.hasItemInHand()) {
                this.holdingItemAnimationTicks = Mth.clamp(this.holdingItemAnimationTicks + 1.0F, 0.0F, 5.0F);
            } else {
                this.holdingItemAnimationTicks = Mth.clamp(this.holdingItemAnimationTicks - 1.0F, 0.0F, 5.0F);
            }

            if (this.isDancing()) {
                ++this.dancingAnimationTicks;
                this.spinningAnimationTicks0 = this.spinningAnimationTicks;
                if (this.isSpinning()) {
                    ++this.spinningAnimationTicks;
                } else {
                    --this.spinningAnimationTicks;
                }

                this.spinningAnimationTicks = Mth.clamp(this.spinningAnimationTicks, 0.0F, 15.0F);
            } else {
                this.dancingAnimationTicks = 0.0F;
                this.spinningAnimationTicks = 0.0F;
                this.spinningAnimationTicks0 = 0.0F;
            }
        } else {
            this.dynamicVibrationListener.getListener().tick(this.level);
            if (this.isPanicking()) {
                this.setDancing(false);
            }
        }

    }

    @Override
    public boolean canPickUpLoot() {
        return !this.isOnPickupCooldown() && this.hasItemInHand();
    }

    public boolean hasItemInHand() {
        return !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
    }

    @Override
    public boolean canTakeItem(ItemStack param0) {
        return false;
    }

    private boolean isOnPickupCooldown() {
        return this.getBrain().checkMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT);
    }

    @Override
    protected InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        ItemStack var1 = this.getItemInHand(InteractionHand.MAIN_HAND);
        if (this.isDancing() && this.isDuplicationItem(var0) && this.canDuplicate()) {
            this.duplicateAllay();
            this.level.broadcastEntityEvent(this, (byte)18);
            this.level.playSound(param0, this, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 2.0F, 1.0F);
            this.removeInteractionItem(param0, var0);
            return InteractionResult.SUCCESS;
        } else if (var1.isEmpty() && !var0.isEmpty()) {
            ItemStack var2 = var0.copy();
            var2.setCount(1);
            this.setItemInHand(InteractionHand.MAIN_HAND, var2);
            this.removeInteractionItem(param0, var0);
            this.level.playSound(param0, this, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 2.0F, 1.0F);
            this.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, param0.getUUID());
            return InteractionResult.SUCCESS;
        } else if (!var1.isEmpty() && param1 == InteractionHand.MAIN_HAND && var0.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.level.playSound(param0, this, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 2.0F, 1.0F);
            this.swing(InteractionHand.MAIN_HAND);

            for(ItemStack var3 : this.getInventory().removeAllItems()) {
                BehaviorUtils.throwItem(this, var3, this.position());
            }

            this.getBrain().eraseMemory(MemoryModuleType.LIKED_PLAYER);
            param0.addItem(var1);
            return InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(param0, param1);
        }
    }

    public void setJukeboxPlaying(BlockPos param0, boolean param1) {
        if (param1) {
            if (!this.isDancing()) {
                this.jukeboxPos = param0;
                this.setDancing(true);
            }
        } else if (param0.equals(this.jukeboxPos) || this.jukeboxPos == null) {
            this.jukeboxPos = null;
            this.setDancing(false);
        }

    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected Vec3i getPickupReach() {
        return ITEM_PICKUP_REACH;
    }

    @Override
    public boolean wantsToPickUp(ItemStack param0) {
        ItemStack var0 = this.getItemInHand(InteractionHand.MAIN_HAND);
        return !var0.isEmpty()
            && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
            && this.inventory.canAddItem(param0)
            && this.allayConsidersItemEqual(var0, param0);
    }

    private boolean allayConsidersItemEqual(ItemStack param0, ItemStack param1) {
        return param0.sameItem(param1) && !this.hasNonMatchingPotion(param0, param1);
    }

    private boolean hasNonMatchingPotion(ItemStack param0, ItemStack param1) {
        CompoundTag var0 = param0.getTag();
        boolean var1 = var0 != null && var0.contains("Potion");
        if (!var1) {
            return false;
        } else {
            CompoundTag var2 = param1.getTag();
            boolean var3 = var2 != null && var2.contains("Potion");
            if (!var3) {
                return true;
            } else {
                Tag var4 = var0.get("Potion");
                Tag var5 = var2.get("Potion");
                return var4 != null && var5 != null && !var4.equals(var5);
            }
        }
    }

    @Override
    protected void pickUpItem(ItemEntity param0) {
        InventoryCarrier.pickUpItem(this, this, param0);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public boolean isFlapping() {
        return !this.isOnGround();
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> param0) {
        Level var3 = this.level;
        if (var3 instanceof ServerLevel var0) {
            param0.accept(this.dynamicVibrationListener, var0);
            param0.accept(this.dynamicJukeboxListener, var0);
        }

    }

    public boolean isDancing() {
        return this.entityData.get(DATA_DANCING);
    }

    public boolean isPanicking() {
        return this.brain.getMemory(MemoryModuleType.IS_PANICKING).isPresent();
    }

    public void setDancing(boolean param0) {
        if (!this.level.isClientSide && this.isEffectiveAi() && (!param0 || !this.isPanicking())) {
            this.entityData.set(DATA_DANCING, param0);
        }
    }

    private boolean shouldStopDancing() {
        return this.jukeboxPos == null
            || !this.jukeboxPos.closerToCenterThan(this.position(), (double)GameEvent.JUKEBOX_PLAY.getNotificationRadius())
            || !this.level.getBlockState(this.jukeboxPos).is(Blocks.JUKEBOX);
    }

    public float getHoldingItemAnimationProgress(float param0) {
        return Mth.lerp(param0, this.holdingItemAnimationTicks0, this.holdingItemAnimationTicks) / 5.0F;
    }

    public boolean isSpinning() {
        float var0 = this.dancingAnimationTicks % 55.0F;
        return var0 < 15.0F;
    }

    public float getSpinningProgress(float param0) {
        return Mth.lerp(param0, this.spinningAnimationTicks0, this.spinningAnimationTicks) / 15.0F;
    }

    @Override
    public boolean equipmentHasChanged(ItemStack param0, ItemStack param1) {
        return !this.allayConsidersItemEqual(param0, param1);
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
        ItemStack var0 = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!var0.isEmpty() && !EnchantmentHelper.hasVanishingCurse(var0)) {
            this.spawnAtLocation(var0);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        this.writeInventoryToTag(param0);
        VibrationListener.codec(this.vibrationListenerConfig)
            .encodeStart(NbtOps.INSTANCE, this.dynamicVibrationListener.getListener())
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("listener", param1));
        param0.putLong("DuplicationCooldown", this.duplicationCooldown);
        param0.putBoolean("CanDuplicate", this.canDuplicate());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.readInventoryFromTag(param0);
        if (param0.contains("listener", 10)) {
            VibrationListener.codec(this.vibrationListenerConfig)
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.getCompound("listener")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.dynamicVibrationListener.updateListener(param0x, this.level));
        }

        this.duplicationCooldown = (long)param0.getInt("DuplicationCooldown");
        this.entityData.set(DATA_CAN_DUPLICATE, param0.getBoolean("CanDuplicate"));
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        return false;
    }

    private void updateDuplicationCooldown() {
        if (this.duplicationCooldown > 0L) {
            --this.duplicationCooldown;
        }

        if (!this.level.isClientSide() && this.duplicationCooldown == 0L && !this.canDuplicate()) {
            this.entityData.set(DATA_CAN_DUPLICATE, true);
        }

    }

    private boolean isDuplicationItem(ItemStack param0) {
        return DUPLICATION_ITEM.test(param0);
    }

    private void duplicateAllay() {
        Allay var0 = EntityType.ALLAY.create(this.level);
        if (var0 != null) {
            var0.moveTo(this.position());
            var0.setPersistenceRequired();
            var0.resetDuplicationCooldown();
            this.resetDuplicationCooldown();
            this.level.addFreshEntity(var0);
        }

    }

    private void resetDuplicationCooldown() {
        this.duplicationCooldown = 6000L;
        this.entityData.set(DATA_CAN_DUPLICATE, false);
    }

    private boolean canDuplicate() {
        return this.entityData.get(DATA_CAN_DUPLICATE);
    }

    private void removeInteractionItem(Player param0, ItemStack param1) {
        if (!param0.getAbilities().instabuild) {
            param1.shrink(1);
        }

    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)this.getEyeHeight() * 0.6, (double)this.getBbWidth() * 0.1);
    }

    @Override
    public double getMyRidingOffset() {
        return 0.4;
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 18) {
            for(int var0 = 0; var0 < 3; ++var0) {
                this.spawnHeartParticle();
            }
        } else {
            super.handleEntityEvent(param0);
        }

    }

    private void spawnHeartParticle() {
        double var0 = this.random.nextGaussian() * 0.02;
        double var1 = this.random.nextGaussian() * 0.02;
        double var2 = this.random.nextGaussian() * 0.02;
        this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), var0, var1, var2);
    }

    class AllayVibrationListenerConfig implements VibrationListener.VibrationListenerConfig {
        @Override
        public boolean shouldListen(ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, GameEvent.Context param4) {
            if (Allay.this.isNoAi()) {
                return false;
            } else {
                Optional<GlobalPos> var0 = Allay.this.getBrain().getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
                if (var0.isEmpty()) {
                    return true;
                } else {
                    GlobalPos var1 = var0.get();
                    return var1.dimension().equals(param0.dimension()) && var1.pos().equals(param2);
                }
            }
        }

        @Override
        public void onSignalReceive(
            ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable Entity param4, @Nullable Entity param5, float param6
        ) {
            if (param3 == GameEvent.NOTE_BLOCK_PLAY) {
                AllayAi.hearNoteblock(Allay.this, new BlockPos(param2));
            }

        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.ALLAY_CAN_LISTEN;
        }
    }

    class JukeboxListener implements GameEventListener {
        private final PositionSource listenerSource;
        private final int listenerRadius;

        public JukeboxListener(PositionSource param0, int param1) {
            this.listenerSource = param0;
            this.listenerRadius = param1;
        }

        @Override
        public PositionSource getListenerSource() {
            return this.listenerSource;
        }

        @Override
        public int getListenerRadius() {
            return this.listenerRadius;
        }

        @Override
        public boolean handleGameEvent(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3) {
            if (param1 == GameEvent.JUKEBOX_PLAY) {
                Allay.this.setJukeboxPlaying(BlockPos.containing(param3), true);
                return true;
            } else if (param1 == GameEvent.JUKEBOX_STOP_PLAY) {
                Allay.this.setJukeboxPlaying(BlockPos.containing(param3), false);
                return true;
            } else {
                return false;
            }
        }
    }
}

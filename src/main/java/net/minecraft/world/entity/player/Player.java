package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Player extends LivingEntity {
    public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.8F);
    private static final Map<Pose, EntityDimensions> POSES = ImmutableMap.<Pose, EntityDimensions>builder()
        .put(Pose.STANDING, STANDING_DIMENSIONS)
        .put(Pose.SLEEPING, SLEEPING_DIMENSIONS)
        .put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F))
        .put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F))
        .put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F))
        .put(Pose.CROUCHING, EntityDimensions.scalable(0.6F, 1.5F))
        .put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F))
        .build();
    private static final EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_SCORE_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> DATA_PLAYER_MAIN_HAND = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_LEFT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
    protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_RIGHT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
    private long timeEntitySatOnShoulder;
    private final Inventory inventory = new Inventory(this);
    protected PlayerEnderChestContainer enderChestInventory = new PlayerEnderChestContainer();
    public final InventoryMenu inventoryMenu;
    public AbstractContainerMenu containerMenu;
    protected FoodData foodData = new FoodData();
    protected int jumpTriggerTime;
    public float oBob;
    public float bob;
    public int takeXpDelay;
    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;
    private int sleepCounter;
    protected boolean wasUnderwater;
    private final Abilities abilities = new Abilities();
    public int experienceLevel;
    public int totalExperience;
    public float experienceProgress;
    protected int enchantmentSeed;
    protected final float defaultFlySpeed = 0.02F;
    private int lastLevelUpTime;
    private final GameProfile gameProfile;
    @OnlyIn(Dist.CLIENT)
    private boolean reducedDebugInfo;
    private ItemStack lastItemInMainHand = ItemStack.EMPTY;
    private final ItemCooldowns cooldowns = this.createItemCooldowns();
    @Nullable
    public FishingHook fishing;

    public Player(Level param0, BlockPos param1, float param2, GameProfile param3) {
        super(EntityType.PLAYER, param0);
        this.setUUID(createPlayerUUID(param3));
        this.gameProfile = param3;
        this.inventoryMenu = new InventoryMenu(this.inventory, !param0.isClientSide, this);
        this.containerMenu = this.inventoryMenu;
        this.moveTo((double)param1.getX() + 0.5, (double)(param1.getY() + 1), (double)param1.getZ() + 0.5, param2, 0.0F);
        this.rotOffs = 180.0F;
    }

    public boolean blockActionRestricted(Level param0, BlockPos param1, GameType param2) {
        if (!param2.isBlockPlacingRestricted()) {
            return false;
        } else if (param2 == GameType.SPECTATOR) {
            return true;
        } else if (this.mayBuild()) {
            return false;
        } else {
            ItemStack var0 = this.getMainHandItem();
            return var0.isEmpty() || !var0.hasAdventureModeBreakTagForBlock(param0.getTagManager(), new BlockInWorld(param0, param1, false));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
            .add(Attributes.ATTACK_DAMAGE, 1.0)
            .add(Attributes.MOVEMENT_SPEED, 0.1F)
            .add(Attributes.ATTACK_SPEED)
            .add(Attributes.LUCK);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PLAYER_ABSORPTION_ID, 0.0F);
        this.entityData.define(DATA_SCORE_ID, 0);
        this.entityData.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte)0);
        this.entityData.define(DATA_PLAYER_MAIN_HAND, (byte)1);
        this.entityData.define(DATA_SHOULDER_LEFT, new CompoundTag());
        this.entityData.define(DATA_SHOULDER_RIGHT, new CompoundTag());
    }

    @Override
    public void tick() {
        this.noPhysics = this.isSpectator();
        if (this.isSpectator()) {
            this.onGround = false;
        }

        if (this.takeXpDelay > 0) {
            --this.takeXpDelay;
        }

        if (this.isSleeping()) {
            ++this.sleepCounter;
            if (this.sleepCounter > 100) {
                this.sleepCounter = 100;
            }

            if (!this.level.isClientSide && this.level.isDay()) {
                this.stopSleepInBed(false, true);
            }
        } else if (this.sleepCounter > 0) {
            ++this.sleepCounter;
            if (this.sleepCounter >= 110) {
                this.sleepCounter = 0;
            }
        }

        this.updateIsUnderwater();
        super.tick();
        if (!this.level.isClientSide && this.containerMenu != null && !this.containerMenu.stillValid(this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }

        this.moveCloak();
        if (!this.level.isClientSide) {
            this.foodData.tick(this);
            this.awardStat(Stats.PLAY_ONE_MINUTE);
            if (this.isAlive()) {
                this.awardStat(Stats.TIME_SINCE_DEATH);
            }

            if (this.isDiscrete()) {
                this.awardStat(Stats.CROUCH_TIME);
            }

            if (!this.isSleeping()) {
                this.awardStat(Stats.TIME_SINCE_REST);
            }
        }

        int var0 = 29999999;
        double var1 = Mth.clamp(this.getX(), -2.9999999E7, 2.9999999E7);
        double var2 = Mth.clamp(this.getZ(), -2.9999999E7, 2.9999999E7);
        if (var1 != this.getX() || var2 != this.getZ()) {
            this.setPos(var1, this.getY(), var2);
        }

        ++this.attackStrengthTicker;
        ItemStack var3 = this.getMainHandItem();
        if (!ItemStack.matches(this.lastItemInMainHand, var3)) {
            if (!ItemStack.isSameIgnoreDurability(this.lastItemInMainHand, var3)) {
                this.resetAttackStrengthTicker();
            }

            this.lastItemInMainHand = var3.copy();
        }

        this.turtleHelmetTick();
        this.cooldowns.tick();
        this.updatePlayerPose();
    }

    public boolean isSecondaryUseActive() {
        return this.isShiftKeyDown();
    }

    protected boolean wantsToStopRiding() {
        return this.isShiftKeyDown();
    }

    protected boolean isStayingOnGroundSurface() {
        return this.isShiftKeyDown();
    }

    protected boolean updateIsUnderwater() {
        this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
        return this.wasUnderwater;
    }

    private void turtleHelmetTick() {
        ItemStack var0 = this.getItemBySlot(EquipmentSlot.HEAD);
        if (var0.is(Items.TURTLE_HELMET) && !this.isEyeInFluid(FluidTags.WATER)) {
            this.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false, true));
        }

    }

    protected ItemCooldowns createItemCooldowns() {
        return new ItemCooldowns();
    }

    private void moveCloak() {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double var0 = this.getX() - this.xCloak;
        double var1 = this.getY() - this.yCloak;
        double var2 = this.getZ() - this.zCloak;
        double var3 = 10.0;
        if (var0 > 10.0) {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }

        if (var2 > 10.0) {
            this.zCloak = this.getZ();
            this.zCloakO = this.zCloak;
        }

        if (var1 > 10.0) {
            this.yCloak = this.getY();
            this.yCloakO = this.yCloak;
        }

        if (var0 < -10.0) {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }

        if (var2 < -10.0) {
            this.zCloak = this.getZ();
            this.zCloakO = this.zCloak;
        }

        if (var1 < -10.0) {
            this.yCloak = this.getY();
            this.yCloakO = this.yCloak;
        }

        this.xCloak += var0 * 0.25;
        this.zCloak += var2 * 0.25;
        this.yCloak += var1 * 0.25;
    }

    protected void updatePlayerPose() {
        if (this.canEnterPose(Pose.SWIMMING)) {
            Pose var0;
            if (this.isFallFlying()) {
                var0 = Pose.FALL_FLYING;
            } else if (this.isSleeping()) {
                var0 = Pose.SLEEPING;
            } else if (this.isSwimming()) {
                var0 = Pose.SWIMMING;
            } else if (this.isAutoSpinAttack()) {
                var0 = Pose.SPIN_ATTACK;
            } else if (this.isShiftKeyDown() && !this.abilities.flying) {
                var0 = Pose.CROUCHING;
            } else {
                var0 = Pose.STANDING;
            }

            Pose var6;
            if (this.isSpectator() || this.isPassenger() || this.canEnterPose(var0)) {
                var6 = var0;
            } else if (this.canEnterPose(Pose.CROUCHING)) {
                var6 = Pose.CROUCHING;
            } else {
                var6 = Pose.SWIMMING;
            }

            this.setPose(var6);
        }
    }

    @Override
    public int getPortalWaitTime() {
        return this.abilities.invulnerable ? 1 : 80;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.PLAYER_SWIM;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.PLAYER_SPLASH;
    }

    @Override
    protected SoundEvent getSwimHighSpeedSplashSound() {
        return SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
    }

    @Override
    public int getDimensionChangingDelay() {
        return 10;
    }

    @Override
    public void playSound(SoundEvent param0, float param1, float param2) {
        this.level.playSound(this, this.getX(), this.getY(), this.getZ(), param0, this.getSoundSource(), param1, param2);
    }

    public void playNotifySound(SoundEvent param0, SoundSource param1, float param2, float param3) {
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.PLAYERS;
    }

    @Override
    protected int getFireImmuneTicks() {
        return 20;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 9) {
            this.completeUsingItem();
        } else if (param0 == 23) {
            this.reducedDebugInfo = false;
        } else if (param0 == 22) {
            this.reducedDebugInfo = true;
        } else if (param0 == 43) {
            this.addParticlesAroundSelf(ParticleTypes.CLOUD);
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    private void addParticlesAroundSelf(ParticleOptions param0) {
        for(int var0 = 0; var0 < 5; ++var0) {
            double var1 = this.random.nextGaussian() * 0.02;
            double var2 = this.random.nextGaussian() * 0.02;
            double var3 = this.random.nextGaussian() * 0.02;
            this.level.addParticle(param0, this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), var1, var2, var3);
        }

    }

    protected void closeContainer() {
        this.containerMenu = this.inventoryMenu;
    }

    @Override
    public void rideTick() {
        if (!this.level.isClientSide && this.wantsToStopRiding() && this.isPassenger()) {
            this.stopRiding();
            this.setShiftKeyDown(false);
        } else {
            double var0 = this.getX();
            double var1 = this.getY();
            double var2 = this.getZ();
            super.rideTick();
            this.oBob = this.bob;
            this.bob = 0.0F;
            this.checkRidingStatistics(this.getX() - var0, this.getY() - var1, this.getZ() - var2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void resetPos() {
        this.setPose(Pose.STANDING);
        super.resetPos();
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    @Override
    protected void serverAiStep() {
        super.serverAiStep();
        this.updateSwingTime();
        this.yHeadRot = this.yRot;
    }

    @Override
    public void aiStep() {
        if (this.jumpTriggerTime > 0) {
            --this.jumpTriggerTime;
        }

        if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            if (this.getHealth() < this.getMaxHealth() && this.tickCount % 20 == 0) {
                this.heal(1.0F);
            }

            if (this.foodData.needsFood() && this.tickCount % 10 == 0) {
                this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
            }
        }

        this.inventory.tick();
        this.oBob = this.bob;
        super.aiStep();
        this.flyingSpeed = 0.02F;
        if (this.isSprinting()) {
            this.flyingSpeed = (float)((double)this.flyingSpeed + 0.005999999865889549);
        }

        this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
        float var1;
        if (this.onGround && !this.isDeadOrDying() && !this.isSwimming()) {
            var1 = Math.min(0.1F, Mth.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement())));
        } else {
            var1 = 0.0F;
        }

        this.bob += (var1 - this.bob) * 0.4F;
        if (this.getHealth() > 0.0F && !this.isSpectator()) {
            AABB var2;
            if (this.isPassenger() && !this.getVehicle().isRemoved()) {
                var2 = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0, 0.0, 1.0);
            } else {
                var2 = this.getBoundingBox().inflate(1.0, 0.5, 1.0);
            }

            List<Entity> var4 = this.level.getEntities(this, var2);
            List<Entity> var5 = Lists.newArrayList();

            for(int var6 = 0; var6 < var4.size(); ++var6) {
                Entity var7 = var4.get(var6);
                if (var7.getType() == EntityType.EXPERIENCE_ORB) {
                    var5.add(var7);
                } else if (!var7.isRemoved()) {
                    this.touch(var7);
                }
            }

            if (!var5.isEmpty()) {
                this.touch(Util.getRandom(var5, this.random));
            }
        }

        this.playShoulderEntityAmbientSound(this.getShoulderEntityLeft());
        this.playShoulderEntityAmbientSound(this.getShoulderEntityRight());
        if (!this.level.isClientSide && (this.fallDistance > 0.5F || this.isInWater()) || this.abilities.flying || this.isSleeping()) {
            this.removeEntitiesOnShoulder();
        }

    }

    private void playShoulderEntityAmbientSound(@Nullable CompoundTag param0) {
        if (param0 != null && (!param0.contains("Silent") || !param0.getBoolean("Silent")) && this.level.random.nextInt(200) == 0) {
            String var0 = param0.getString("id");
            EntityType.byString(var0)
                .filter(param0x -> param0x == EntityType.PARROT)
                .ifPresent(
                    param0x -> {
                        if (!Parrot.imitateNearbyMobs(this.level, this)) {
                            this.level
                                .playSound(
                                    null,
                                    this.getX(),
                                    this.getY(),
                                    this.getZ(),
                                    Parrot.getAmbient(this.level, this.level.random),
                                    this.getSoundSource(),
                                    1.0F,
                                    Parrot.getPitch(this.level.random)
                                );
                        }
        
                    }
                );
        }

    }

    private void touch(Entity param0) {
        param0.playerTouch(this);
    }

    public int getScore() {
        return this.entityData.get(DATA_SCORE_ID);
    }

    public void setScore(int param0) {
        this.entityData.set(DATA_SCORE_ID, param0);
    }

    public void increaseScore(int param0) {
        int var0 = this.getScore();
        this.entityData.set(DATA_SCORE_ID, var0 + param0);
    }

    @Override
    public void die(DamageSource param0) {
        super.die(param0);
        this.reapplyPosition();
        if (!this.isSpectator()) {
            this.dropAllDeathLoot(param0);
        }

        if (param0 != null) {
            this.setDeltaMovement(
                (double)(-Mth.cos((this.hurtDir + this.yRot) * (float) (Math.PI / 180.0)) * 0.1F),
                0.1F,
                (double)(-Mth.sin((this.hurtDir + this.yRot) * (float) (Math.PI / 180.0)) * 0.1F)
            );
        } else {
            this.setDeltaMovement(0.0, 0.1, 0.0);
        }

        this.awardStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.clearFire();
        this.setSharedFlag(0, false);
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (!this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            this.destroyVanishingCursedItems();
            this.inventory.dropAll();
        }

    }

    protected void destroyVanishingCursedItems() {
        for(int var0 = 0; var0 < this.inventory.getContainerSize(); ++var0) {
            ItemStack var1 = this.inventory.getItem(var0);
            if (!var1.isEmpty() && EnchantmentHelper.hasVanishingCurse(var1)) {
                this.inventory.removeItemNoUpdate(var0);
            }
        }

    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        if (param0 == DamageSource.ON_FIRE) {
            return SoundEvents.PLAYER_HURT_ON_FIRE;
        } else if (param0 == DamageSource.DROWN) {
            return SoundEvents.PLAYER_HURT_DROWN;
        } else if (param0 == DamageSource.SWEET_BERRY_BUSH) {
            return SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH;
        } else {
            return param0 == DamageSource.FREEZE ? SoundEvents.PLAYER_HURT_FREEZE : SoundEvents.PLAYER_HURT;
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    public boolean drop(boolean param0) {
        return this.drop(
                this.inventory
                    .removeItem(this.inventory.selected, param0 && !this.inventory.getSelected().isEmpty() ? this.inventory.getSelected().getCount() : 1),
                false,
                true
            )
            != null;
    }

    @Nullable
    public ItemEntity drop(ItemStack param0, boolean param1) {
        return this.drop(param0, false, param1);
    }

    @Nullable
    public ItemEntity drop(ItemStack param0, boolean param1, boolean param2) {
        if (param0.isEmpty()) {
            return null;
        } else {
            if (this.level.isClientSide) {
                this.swing(InteractionHand.MAIN_HAND);
            }

            double var0 = this.getEyeY() - 0.3F;
            ItemEntity var1 = new ItemEntity(this.level, this.getX(), var0, this.getZ(), param0);
            var1.setPickUpDelay(40);
            if (param2) {
                var1.setThrower(this.getUUID());
            }

            if (param1) {
                float var2 = this.random.nextFloat() * 0.5F;
                float var3 = this.random.nextFloat() * (float) (Math.PI * 2);
                var1.setDeltaMovement((double)(-Mth.sin(var3) * var2), 0.2F, (double)(Mth.cos(var3) * var2));
            } else {
                float var4 = 0.3F;
                float var5 = Mth.sin(this.xRot * (float) (Math.PI / 180.0));
                float var6 = Mth.cos(this.xRot * (float) (Math.PI / 180.0));
                float var7 = Mth.sin(this.yRot * (float) (Math.PI / 180.0));
                float var8 = Mth.cos(this.yRot * (float) (Math.PI / 180.0));
                float var9 = this.random.nextFloat() * (float) (Math.PI * 2);
                float var10 = 0.02F * this.random.nextFloat();
                var1.setDeltaMovement(
                    (double)(-var7 * var6 * 0.3F) + Math.cos((double)var9) * (double)var10,
                    (double)(-var5 * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
                    (double)(var8 * var6 * 0.3F) + Math.sin((double)var9) * (double)var10
                );
            }

            return var1;
        }
    }

    public float getDestroySpeed(BlockState param0) {
        float var0 = this.inventory.getDestroySpeed(param0);
        if (var0 > 1.0F) {
            int var1 = EnchantmentHelper.getBlockEfficiency(this);
            ItemStack var2 = this.getMainHandItem();
            if (var1 > 0 && !var2.isEmpty()) {
                var0 += (float)(var1 * var1 + 1);
            }
        }

        if (MobEffectUtil.hasDigSpeed(this)) {
            var0 *= 1.0F + (float)(MobEffectUtil.getDigSpeedAmplification(this) + 1) * 0.2F;
        }

        if (this.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float var3;
            switch(this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0:
                    var3 = 0.3F;
                    break;
                case 1:
                    var3 = 0.09F;
                    break;
                case 2:
                    var3 = 0.0027F;
                    break;
                case 3:
                default:
                    var3 = 8.1E-4F;
            }

            var0 *= var3;
        }

        if (this.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
            var0 /= 5.0F;
        }

        if (!this.onGround) {
            var0 /= 5.0F;
        }

        return var0;
    }

    public boolean hasCorrectToolForDrops(BlockState param0) {
        return !param0.requiresCorrectToolForDrops() || this.inventory.getSelected().isCorrectToolForDrops(param0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setUUID(createPlayerUUID(this.gameProfile));
        ListTag var0 = param0.getList("Inventory", 10);
        this.inventory.load(var0);
        this.inventory.selected = param0.getInt("SelectedItemSlot");
        this.sleepCounter = param0.getShort("SleepTimer");
        this.experienceProgress = param0.getFloat("XpP");
        this.experienceLevel = param0.getInt("XpLevel");
        this.totalExperience = param0.getInt("XpTotal");
        this.enchantmentSeed = param0.getInt("XpSeed");
        if (this.enchantmentSeed == 0) {
            this.enchantmentSeed = this.random.nextInt();
        }

        this.setScore(param0.getInt("Score"));
        this.foodData.readAdditionalSaveData(param0);
        this.abilities.loadSaveData(param0);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double)this.abilities.getWalkingSpeed());
        if (param0.contains("EnderItems", 9)) {
            this.enderChestInventory.fromTag(param0.getList("EnderItems", 10));
        }

        if (param0.contains("ShoulderEntityLeft", 10)) {
            this.setShoulderEntityLeft(param0.getCompound("ShoulderEntityLeft"));
        }

        if (param0.contains("ShoulderEntityRight", 10)) {
            this.setShoulderEntityRight(param0.getCompound("ShoulderEntityRight"));
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        param0.put("Inventory", this.inventory.save(new ListTag()));
        param0.putInt("SelectedItemSlot", this.inventory.selected);
        param0.putShort("SleepTimer", (short)this.sleepCounter);
        param0.putFloat("XpP", this.experienceProgress);
        param0.putInt("XpLevel", this.experienceLevel);
        param0.putInt("XpTotal", this.totalExperience);
        param0.putInt("XpSeed", this.enchantmentSeed);
        param0.putInt("Score", this.getScore());
        this.foodData.addAdditionalSaveData(param0);
        this.abilities.addSaveData(param0);
        param0.put("EnderItems", this.enderChestInventory.createTag());
        if (!this.getShoulderEntityLeft().isEmpty()) {
            param0.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
        }

        if (!this.getShoulderEntityRight().isEmpty()) {
            param0.put("ShoulderEntityRight", this.getShoulderEntityRight());
        }

    }

    @Override
    public boolean isInvulnerableTo(DamageSource param0) {
        if (super.isInvulnerableTo(param0)) {
            return true;
        } else if (param0 == DamageSource.DROWN) {
            return !this.level.getGameRules().getBoolean(GameRules.RULE_DROWNING_DAMAGE);
        } else if (param0.isFall()) {
            return !this.level.getGameRules().getBoolean(GameRules.RULE_FALL_DAMAGE);
        } else if (param0.isFire()) {
            return !this.level.getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE);
        } else if (param0 == DamageSource.FREEZE) {
            return !this.level.getGameRules().getBoolean(GameRules.RULE_FREEZE_DAMAGE);
        } else {
            return false;
        }
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else if (this.abilities.invulnerable && !param0.isBypassInvul()) {
            return false;
        } else {
            this.noActionTime = 0;
            if (this.isDeadOrDying()) {
                return false;
            } else {
                this.removeEntitiesOnShoulder();
                if (param0.scalesWithDifficulty()) {
                    if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                        param1 = 0.0F;
                    }

                    if (this.level.getDifficulty() == Difficulty.EASY) {
                        param1 = Math.min(param1 / 2.0F + 1.0F, param1);
                    }

                    if (this.level.getDifficulty() == Difficulty.HARD) {
                        param1 = param1 * 3.0F / 2.0F;
                    }
                }

                return param1 == 0.0F ? false : super.hurt(param0, param1);
            }
        }
    }

    @Override
    protected void blockUsingShield(LivingEntity param0) {
        super.blockUsingShield(param0);
        if (param0.getMainHandItem().getItem() instanceof AxeItem) {
            this.disableShield(true);
        }

    }

    @Override
    public boolean canBeTargeted() {
        return !this.getAbilities().invulnerable && super.canBeTargeted();
    }

    public boolean canHarmPlayer(Player param0) {
        Team var0 = this.getTeam();
        Team var1 = param0.getTeam();
        if (var0 == null) {
            return true;
        } else {
            return !var0.isAlliedTo(var1) ? true : var0.isAllowFriendlyFire();
        }
    }

    @Override
    protected void hurtArmor(DamageSource param0, float param1) {
        this.inventory.hurtArmor(param0, param1);
    }

    @Override
    protected void hurtCurrentlyUsedShield(float param0) {
        if (this.useItem.is(Items.SHIELD)) {
            if (!this.level.isClientSide) {
                this.awardStat(Stats.ITEM_USED.get(this.useItem.getItem()));
            }

            if (param0 >= 3.0F) {
                int var0 = 1 + Mth.floor(param0);
                InteractionHand var1 = this.getUsedItemHand();
                this.useItem.hurtAndBreak(var0, this, param1 -> param1.broadcastBreakEvent(var1));
                if (this.useItem.isEmpty()) {
                    if (var1 == InteractionHand.MAIN_HAND) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    }

                    this.useItem = ItemStack.EMPTY;
                    this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F);
                }
            }

        }
    }

    @Override
    protected void actuallyHurt(DamageSource param0, float param1) {
        if (!this.isInvulnerableTo(param0)) {
            param1 = this.getDamageAfterArmorAbsorb(param0, param1);
            param1 = this.getDamageAfterMagicAbsorb(param0, param1);
            float var8 = Math.max(param1 - this.getAbsorptionAmount(), 0.0F);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (param1 - var8));
            float var1 = param1 - var8;
            if (var1 > 0.0F && var1 < 3.4028235E37F) {
                this.awardStat(Stats.DAMAGE_ABSORBED, Math.round(var1 * 10.0F));
            }

            if (var8 != 0.0F) {
                this.causeFoodExhaustion(param0.getFoodExhaustion());
                float var2 = this.getHealth();
                this.setHealth(this.getHealth() - var8);
                this.getCombatTracker().recordDamage(param0, var2, var8);
                if (var8 < 3.4028235E37F) {
                    this.awardStat(Stats.DAMAGE_TAKEN, Math.round(var8 * 10.0F));
                }

            }
        }
    }

    @Override
    protected boolean onSoulSpeedBlock() {
        return !this.abilities.flying && super.onSoulSpeedBlock();
    }

    public void openTextEdit(SignBlockEntity param0) {
    }

    public void openMinecartCommandBlock(BaseCommandBlock param0) {
    }

    public void openCommandBlock(CommandBlockEntity param0) {
    }

    public void openStructureBlock(StructureBlockEntity param0) {
    }

    public void openJigsawBlock(JigsawBlockEntity param0) {
    }

    public void openHorseInventory(AbstractHorse param0, Container param1) {
    }

    public OptionalInt openMenu(@Nullable MenuProvider param0) {
        return OptionalInt.empty();
    }

    public void sendMerchantOffers(int param0, MerchantOffers param1, int param2, int param3, boolean param4, boolean param5) {
    }

    public void openItemGui(ItemStack param0, InteractionHand param1) {
    }

    public InteractionResult interactOn(Entity param0, InteractionHand param1) {
        if (this.isSpectator()) {
            if (param0 instanceof MenuProvider) {
                this.openMenu((MenuProvider)param0);
            }

            return InteractionResult.PASS;
        } else {
            ItemStack var0 = this.getItemInHand(param1);
            ItemStack var1 = var0.copy();
            InteractionResult var2 = param0.interact(this, param1);
            if (var2.consumesAction()) {
                if (this.abilities.instabuild && var0 == this.getItemInHand(param1) && var0.getCount() < var1.getCount()) {
                    var0.setCount(var1.getCount());
                }

                return var2;
            } else {
                if (!var0.isEmpty() && param0 instanceof LivingEntity) {
                    if (this.abilities.instabuild) {
                        var0 = var1;
                    }

                    InteractionResult var3 = var0.interactLivingEntity(this, (LivingEntity)param0, param1);
                    if (var3.consumesAction()) {
                        if (var0.isEmpty() && !this.abilities.instabuild) {
                            this.setItemInHand(param1, ItemStack.EMPTY);
                        }

                        return var3;
                    }
                }

                return InteractionResult.PASS;
            }
        }
    }

    @Override
    public double getMyRidingOffset() {
        return -0.35;
    }

    @Override
    public void removeVehicle() {
        super.removeVehicle();
        this.boardingCooldown = 0;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isSleeping();
    }

    @Override
    public boolean isAffectedByFluids() {
        return !this.abilities.flying;
    }

    @Override
    protected Vec3 maybeBackOffFromEdge(Vec3 param0, MoverType param1) {
        if (!this.abilities.flying && (param1 == MoverType.SELF || param1 == MoverType.PLAYER) && this.isStayingOnGroundSurface() && this.isAboveGround()) {
            double var0 = param0.x;
            double var1 = param0.z;
            double var2 = 0.05;

            while(var0 != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(var0, (double)(-this.maxUpStep), 0.0))) {
                if (var0 < 0.05 && var0 >= -0.05) {
                    var0 = 0.0;
                } else if (var0 > 0.0) {
                    var0 -= 0.05;
                } else {
                    var0 += 0.05;
                }
            }

            while(var1 != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(0.0, (double)(-this.maxUpStep), var1))) {
                if (var1 < 0.05 && var1 >= -0.05) {
                    var1 = 0.0;
                } else if (var1 > 0.0) {
                    var1 -= 0.05;
                } else {
                    var1 += 0.05;
                }
            }

            while(var0 != 0.0 && var1 != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(var0, (double)(-this.maxUpStep), var1))) {
                if (var0 < 0.05 && var0 >= -0.05) {
                    var0 = 0.0;
                } else if (var0 > 0.0) {
                    var0 -= 0.05;
                } else {
                    var0 += 0.05;
                }

                if (var1 < 0.05 && var1 >= -0.05) {
                    var1 = 0.0;
                } else if (var1 > 0.0) {
                    var1 -= 0.05;
                } else {
                    var1 += 0.05;
                }
            }

            param0 = new Vec3(var0, param0.y, var1);
        }

        return param0;
    }

    private boolean isAboveGround() {
        return this.onGround
            || this.fallDistance < this.maxUpStep
                && !this.level.noCollision(this, this.getBoundingBox().move(0.0, (double)(this.fallDistance - this.maxUpStep), 0.0));
    }

    public void attack(Entity param0) {
        if (param0.isAttackable()) {
            if (!param0.skipAttackInteraction(this)) {
                float var0 = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float var1;
                if (param0 instanceof LivingEntity) {
                    var1 = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)param0).getMobType());
                } else {
                    var1 = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), MobType.UNDEFINED);
                }

                float var3 = this.getAttackStrengthScale(0.5F);
                var0 *= 0.2F + var3 * var3 * 0.8F;
                var1 *= var3;
                this.resetAttackStrengthTicker();
                if (var0 > 0.0F || var1 > 0.0F) {
                    boolean var4 = var3 > 0.9F;
                    boolean var5 = false;
                    int var6 = 0;
                    var6 += EnchantmentHelper.getKnockbackBonus(this);
                    if (this.isSprinting() && var4) {
                        this.level
                            .playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, this.getSoundSource(), 1.0F, 1.0F);
                        ++var6;
                        var5 = true;
                    }

                    boolean var7 = var4
                        && this.fallDistance > 0.0F
                        && !this.onGround
                        && !this.onClimbable()
                        && !this.isInWater()
                        && !this.hasEffect(MobEffects.BLINDNESS)
                        && !this.isPassenger()
                        && param0 instanceof LivingEntity;
                    var7 = var7 && !this.isSprinting();
                    if (var7) {
                        var0 *= 1.5F;
                    }

                    var0 += var1;
                    boolean var8 = false;
                    double var9 = (double)(this.walkDist - this.walkDistO);
                    if (var4 && !var7 && !var5 && this.onGround && var9 < (double)this.getSpeed()) {
                        ItemStack var10 = this.getItemInHand(InteractionHand.MAIN_HAND);
                        if (var10.getItem() instanceof SwordItem) {
                            var8 = true;
                        }
                    }

                    float var11 = 0.0F;
                    boolean var12 = false;
                    int var13 = EnchantmentHelper.getFireAspect(this);
                    if (param0 instanceof LivingEntity) {
                        var11 = ((LivingEntity)param0).getHealth();
                        if (var13 > 0 && !param0.isOnFire()) {
                            var12 = true;
                            param0.setSecondsOnFire(1);
                        }
                    }

                    Vec3 var14 = param0.getDeltaMovement();
                    boolean var15 = param0.hurt(DamageSource.playerAttack(this), var0);
                    if (var15) {
                        if (var6 > 0) {
                            if (param0 instanceof LivingEntity) {
                                ((LivingEntity)param0)
                                    .knockback(
                                        (float)var6 * 0.5F,
                                        (double)Mth.sin(this.yRot * (float) (Math.PI / 180.0)),
                                        (double)(-Mth.cos(this.yRot * (float) (Math.PI / 180.0)))
                                    );
                            } else {
                                param0.push(
                                    (double)(-Mth.sin(this.yRot * (float) (Math.PI / 180.0)) * (float)var6 * 0.5F),
                                    0.1,
                                    (double)(Mth.cos(this.yRot * (float) (Math.PI / 180.0)) * (float)var6 * 0.5F)
                                );
                            }

                            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                            this.setSprinting(false);
                        }

                        if (var8) {
                            float var16 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(this) * var0;

                            for(LivingEntity var18 : this.level.getEntitiesOfClass(LivingEntity.class, param0.getBoundingBox().inflate(1.0, 0.25, 1.0))) {
                                if (var18 != this
                                    && var18 != param0
                                    && !this.isAlliedTo(var18)
                                    && (!(var18 instanceof ArmorStand) || !((ArmorStand)var18).isMarker())
                                    && this.distanceToSqr(var18) < 9.0) {
                                    var18.knockback(
                                        0.4F, (double)Mth.sin(this.yRot * (float) (Math.PI / 180.0)), (double)(-Mth.cos(this.yRot * (float) (Math.PI / 180.0)))
                                    );
                                    var18.hurt(DamageSource.playerAttack(this), var16);
                                }
                            }

                            this.level
                                .playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0F, 1.0F);
                            this.sweepAttack();
                        }

                        if (param0 instanceof ServerPlayer && param0.hurtMarked) {
                            ((ServerPlayer)param0).connection.send(new ClientboundSetEntityMotionPacket(param0));
                            param0.hurtMarked = false;
                            param0.setDeltaMovement(var14);
                        }

                        if (var7) {
                            this.level
                                .playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0F, 1.0F);
                            this.crit(param0);
                        }

                        if (!var7 && !var8) {
                            if (var4) {
                                this.level
                                    .playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
                            } else {
                                this.level
                                    .playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0F, 1.0F);
                            }
                        }

                        if (var1 > 0.0F) {
                            this.magicCrit(param0);
                        }

                        this.setLastHurtMob(param0);
                        if (param0 instanceof LivingEntity) {
                            EnchantmentHelper.doPostHurtEffects((LivingEntity)param0, this);
                        }

                        EnchantmentHelper.doPostDamageEffects(this, param0);
                        ItemStack var19 = this.getMainHandItem();
                        Entity var20 = param0;
                        if (param0 instanceof EnderDragonPart) {
                            var20 = ((EnderDragonPart)param0).parentMob;
                        }

                        if (!this.level.isClientSide && !var19.isEmpty() && var20 instanceof LivingEntity) {
                            var19.hurtEnemy((LivingEntity)var20, this);
                            if (var19.isEmpty()) {
                                this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (param0 instanceof LivingEntity) {
                            float var21 = var11 - ((LivingEntity)param0).getHealth();
                            this.awardStat(Stats.DAMAGE_DEALT, Math.round(var21 * 10.0F));
                            if (var13 > 0) {
                                param0.setSecondsOnFire(var13 * 4);
                            }

                            if (this.level instanceof ServerLevel && var21 > 2.0F) {
                                int var22 = (int)((double)var21 * 0.5);
                                ((ServerLevel)this.level)
                                    .sendParticles(ParticleTypes.DAMAGE_INDICATOR, param0.getX(), param0.getY(0.5), param0.getZ(), var22, 0.1, 0.0, 0.1, 0.2);
                            }
                        }

                        this.causeFoodExhaustion(0.1F);
                    } else {
                        this.level
                            .playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
                        if (var12) {
                            param0.clearFire();
                        }
                    }
                }

            }
        }
    }

    @Override
    protected void doAutoAttackOnTouch(LivingEntity param0) {
        this.attack(param0);
    }

    public void disableShield(boolean param0) {
        float var0 = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
        if (param0) {
            var0 += 0.75F;
        }

        if (this.random.nextFloat() < var0) {
            this.getCooldowns().addCooldown(Items.SHIELD, 100);
            this.stopUsingItem();
            this.level.broadcastEntityEvent(this, (byte)30);
        }

    }

    public void crit(Entity param0) {
    }

    public void magicCrit(Entity param0) {
    }

    public void sweepAttack() {
        double var0 = (double)(-Mth.sin(this.yRot * (float) (Math.PI / 180.0)));
        double var1 = (double)Mth.cos(this.yRot * (float) (Math.PI / 180.0));
        if (this.level instanceof ServerLevel) {
            ((ServerLevel)this.level)
                .sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + var0, this.getY(0.5), this.getZ() + var1, 0, var0, 0.0, var1, 0.0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void respawn() {
    }

    @Override
    public void remove(Entity.RemovalReason param0) {
        super.remove(param0);
        this.inventoryMenu.removed(this);
        if (this.containerMenu != null) {
            this.containerMenu.removed(this);
        }

    }

    public boolean isLocalPlayer() {
        return false;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Abilities getAbilities() {
        return this.abilities;
    }

    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos param0) {
        this.startSleeping(param0);
        this.sleepCounter = 0;
        return Either.right(Unit.INSTANCE);
    }

    public void stopSleepInBed(boolean param0, boolean param1) {
        super.stopSleeping();
        if (this.level instanceof ServerLevel && param1) {
            ((ServerLevel)this.level).updateSleepingPlayerList();
        }

        this.sleepCounter = param0 ? 0 : 100;
    }

    @Override
    public void stopSleeping() {
        this.stopSleepInBed(true, true);
    }

    public static Optional<Vec3> findRespawnPositionAndUseSpawnBlock(ServerLevel param0, BlockPos param1, float param2, boolean param3, boolean param4) {
        BlockState var0 = param0.getBlockState(param1);
        Block var1 = var0.getBlock();
        if (var1 instanceof RespawnAnchorBlock && var0.getValue(RespawnAnchorBlock.CHARGE) > 0 && RespawnAnchorBlock.canSetSpawn(param0)) {
            Optional<Vec3> var2 = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, param0, param1);
            if (!param4 && var2.isPresent()) {
                param0.setBlock(param1, var0.setValue(RespawnAnchorBlock.CHARGE, Integer.valueOf(var0.getValue(RespawnAnchorBlock.CHARGE) - 1)), 3);
            }

            return var2;
        } else if (var1 instanceof BedBlock && BedBlock.canSetSpawn(param0)) {
            return BedBlock.findStandUpPosition(EntityType.PLAYER, param0, param1, param2);
        } else if (!param3) {
            return Optional.empty();
        } else {
            boolean var3 = var1.isPossibleToRespawnInThis();
            boolean var4 = param0.getBlockState(param1.above()).getBlock().isPossibleToRespawnInThis();
            return var3 && var4
                ? Optional.of(new Vec3((double)param1.getX() + 0.5, (double)param1.getY() + 0.1, (double)param1.getZ() + 0.5))
                : Optional.empty();
        }
    }

    public boolean isSleepingLongEnough() {
        return this.isSleeping() && this.sleepCounter >= 100;
    }

    public int getSleepTimer() {
        return this.sleepCounter;
    }

    public void displayClientMessage(Component param0, boolean param1) {
    }

    public void awardStat(ResourceLocation param0) {
        this.awardStat(Stats.CUSTOM.get(param0));
    }

    public void awardStat(ResourceLocation param0, int param1) {
        this.awardStat(Stats.CUSTOM.get(param0), param1);
    }

    public void awardStat(Stat<?> param0) {
        this.awardStat(param0, 1);
    }

    public void awardStat(Stat<?> param0, int param1) {
    }

    public void resetStat(Stat<?> param0) {
    }

    public int awardRecipes(Collection<Recipe<?>> param0) {
        return 0;
    }

    public void awardRecipesByKey(ResourceLocation[] param0) {
    }

    public int resetRecipes(Collection<Recipe<?>> param0) {
        return 0;
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        this.awardStat(Stats.JUMP);
        if (this.isSprinting()) {
            this.causeFoodExhaustion(0.2F);
        } else {
            this.causeFoodExhaustion(0.05F);
        }

    }

    @Override
    public void travel(Vec3 param0) {
        double var0 = this.getX();
        double var1 = this.getY();
        double var2 = this.getZ();
        if (this.isSwimming() && !this.isPassenger()) {
            double var3 = this.getLookAngle().y;
            double var4 = var3 < -0.2 ? 0.085 : 0.06;
            if (var3 <= 0.0
                || this.jumping
                || !this.level.getBlockState(new BlockPos(this.getX(), this.getY() + 1.0 - 0.1, this.getZ())).getFluidState().isEmpty()) {
                Vec3 var5 = this.getDeltaMovement();
                this.setDeltaMovement(var5.add(0.0, (var3 - var5.y) * var4, 0.0));
            }
        }

        if (this.abilities.flying && !this.isPassenger()) {
            double var6 = this.getDeltaMovement().y;
            float var7 = this.flyingSpeed;
            this.flyingSpeed = this.abilities.getFlyingSpeed() * (float)(this.isSprinting() ? 2 : 1);
            super.travel(param0);
            Vec3 var8 = this.getDeltaMovement();
            this.setDeltaMovement(var8.x, var6 * 0.6, var8.z);
            this.flyingSpeed = var7;
            this.fallDistance = 0.0F;
            this.setSharedFlag(7, false);
        } else {
            super.travel(param0);
        }

        this.checkMovementStatistics(this.getX() - var0, this.getY() - var1, this.getZ() - var2);
    }

    @Override
    public void updateSwimming() {
        if (this.abilities.flying) {
            this.setSwimming(false);
        } else {
            super.updateSwimming();
        }

    }

    protected boolean freeAt(BlockPos param0) {
        return !this.level.getBlockState(param0).isSuffocating(this.level, param0);
    }

    @Override
    public float getSpeed() {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    public void checkMovementStatistics(double param0, double param1, double param2) {
        if (!this.isPassenger()) {
            if (this.isSwimming()) {
                int var0 = Math.round(Mth.sqrt(param0 * param0 + param1 * param1 + param2 * param2) * 100.0F);
                if (var0 > 0) {
                    this.awardStat(Stats.SWIM_ONE_CM, var0);
                    this.causeFoodExhaustion(0.01F * (float)var0 * 0.01F);
                }
            } else if (this.isEyeInFluid(FluidTags.WATER)) {
                int var1 = Math.round(Mth.sqrt(param0 * param0 + param1 * param1 + param2 * param2) * 100.0F);
                if (var1 > 0) {
                    this.awardStat(Stats.WALK_UNDER_WATER_ONE_CM, var1);
                    this.causeFoodExhaustion(0.01F * (float)var1 * 0.01F);
                }
            } else if (this.isInWater()) {
                int var2 = Math.round(Mth.sqrt(param0 * param0 + param2 * param2) * 100.0F);
                if (var2 > 0) {
                    this.awardStat(Stats.WALK_ON_WATER_ONE_CM, var2);
                    this.causeFoodExhaustion(0.01F * (float)var2 * 0.01F);
                }
            } else if (this.onClimbable()) {
                if (param1 > 0.0) {
                    this.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round(param1 * 100.0));
                }
            } else if (this.onGround) {
                int var3 = Math.round(Mth.sqrt(param0 * param0 + param2 * param2) * 100.0F);
                if (var3 > 0) {
                    if (this.isSprinting()) {
                        this.awardStat(Stats.SPRINT_ONE_CM, var3);
                        this.causeFoodExhaustion(0.1F * (float)var3 * 0.01F);
                    } else if (this.isCrouching()) {
                        this.awardStat(Stats.CROUCH_ONE_CM, var3);
                        this.causeFoodExhaustion(0.0F * (float)var3 * 0.01F);
                    } else {
                        this.awardStat(Stats.WALK_ONE_CM, var3);
                        this.causeFoodExhaustion(0.0F * (float)var3 * 0.01F);
                    }
                }
            } else if (this.isFallFlying()) {
                int var4 = Math.round(Mth.sqrt(param0 * param0 + param1 * param1 + param2 * param2) * 100.0F);
                this.awardStat(Stats.AVIATE_ONE_CM, var4);
            } else {
                int var5 = Math.round(Mth.sqrt(param0 * param0 + param2 * param2) * 100.0F);
                if (var5 > 25) {
                    this.awardStat(Stats.FLY_ONE_CM, var5);
                }
            }

        }
    }

    private void checkRidingStatistics(double param0, double param1, double param2) {
        if (this.isPassenger()) {
            int var0 = Math.round(Mth.sqrt(param0 * param0 + param1 * param1 + param2 * param2) * 100.0F);
            if (var0 > 0) {
                Entity var1 = this.getVehicle();
                if (var1 instanceof AbstractMinecart) {
                    this.awardStat(Stats.MINECART_ONE_CM, var0);
                } else if (var1 instanceof Boat) {
                    this.awardStat(Stats.BOAT_ONE_CM, var0);
                } else if (var1 instanceof Pig) {
                    this.awardStat(Stats.PIG_ONE_CM, var0);
                } else if (var1 instanceof AbstractHorse) {
                    this.awardStat(Stats.HORSE_ONE_CM, var0);
                } else if (var1 instanceof Strider) {
                    this.awardStat(Stats.STRIDER_ONE_CM, var0);
                }
            }
        }

    }

    @Override
    public boolean causeFallDamage(float param0, float param1, DamageSource param2) {
        if (this.abilities.mayfly) {
            return false;
        } else {
            if (param0 >= 2.0F) {
                this.awardStat(Stats.FALL_ONE_CM, (int)Math.round((double)param0 * 100.0));
            }

            return super.causeFallDamage(param0, param1, param2);
        }
    }

    public boolean tryToStartFallFlying() {
        if (!this.onGround && !this.isFallFlying() && !this.isInWater() && !this.hasEffect(MobEffects.LEVITATION)) {
            ItemStack var0 = this.getItemBySlot(EquipmentSlot.CHEST);
            if (var0.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(var0)) {
                this.startFallFlying();
                return true;
            }
        }

        return false;
    }

    public void startFallFlying() {
        this.setSharedFlag(7, true);
    }

    public void stopFallFlying() {
        this.setSharedFlag(7, true);
        this.setSharedFlag(7, false);
    }

    @Override
    protected void doWaterSplashEffect() {
        if (!this.isSpectator()) {
            super.doWaterSplashEffect();
        }

    }

    @Override
    protected SoundEvent getFallDamageSound(int param0) {
        return param0 > 4 ? SoundEvents.PLAYER_BIG_FALL : SoundEvents.PLAYER_SMALL_FALL;
    }

    @Override
    public void killed(ServerLevel param0, LivingEntity param1) {
        this.awardStat(Stats.ENTITY_KILLED.get(param1.getType()));
    }

    @Override
    public void makeStuckInBlock(BlockState param0, Vec3 param1) {
        if (!this.abilities.flying) {
            super.makeStuckInBlock(param0, param1);
        }

    }

    public void giveExperiencePoints(int param0) {
        this.increaseScore(param0);
        this.experienceProgress += (float)param0 / (float)this.getXpNeededForNextLevel();
        this.totalExperience = Mth.clamp(this.totalExperience + param0, 0, Integer.MAX_VALUE);

        while(this.experienceProgress < 0.0F) {
            float var0 = this.experienceProgress * (float)this.getXpNeededForNextLevel();
            if (this.experienceLevel > 0) {
                this.giveExperienceLevels(-1);
                this.experienceProgress = 1.0F + var0 / (float)this.getXpNeededForNextLevel();
            } else {
                this.giveExperienceLevels(-1);
                this.experienceProgress = 0.0F;
            }
        }

        while(this.experienceProgress >= 1.0F) {
            this.experienceProgress = (this.experienceProgress - 1.0F) * (float)this.getXpNeededForNextLevel();
            this.giveExperienceLevels(1);
            this.experienceProgress /= (float)this.getXpNeededForNextLevel();
        }

    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed;
    }

    public void onEnchantmentPerformed(ItemStack param0, int param1) {
        this.experienceLevel -= param1;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0F;
            this.totalExperience = 0;
        }

        this.enchantmentSeed = this.random.nextInt();
    }

    public void giveExperienceLevels(int param0) {
        this.experienceLevel += param0;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0F;
            this.totalExperience = 0;
        }

        if (param0 > 0 && this.experienceLevel % 5 == 0 && (float)this.lastLevelUpTime < (float)this.tickCount - 100.0F) {
            float var0 = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), var0 * 0.75F, 1.0F);
            this.lastLevelUpTime = this.tickCount;
        }

    }

    public int getXpNeededForNextLevel() {
        if (this.experienceLevel >= 30) {
            return 112 + (this.experienceLevel - 30) * 9;
        } else {
            return this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2;
        }
    }

    public void causeFoodExhaustion(float param0) {
        if (!this.abilities.invulnerable) {
            if (!this.level.isClientSide) {
                this.foodData.addExhaustion(param0);
            }

        }
    }

    public FoodData getFoodData() {
        return this.foodData;
    }

    public boolean canEat(boolean param0) {
        return this.abilities.invulnerable || param0 || this.foodData.needsFood();
    }

    public boolean isHurt() {
        return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
    }

    public boolean mayBuild() {
        return this.abilities.mayBuild;
    }

    public boolean mayUseItemAt(BlockPos param0, Direction param1, ItemStack param2) {
        if (this.abilities.mayBuild) {
            return true;
        } else {
            BlockPos var0 = param0.relative(param1.getOpposite());
            BlockInWorld var1 = new BlockInWorld(this.level, var0, false);
            return param2.hasAdventureModePlaceTagForBlock(this.level.getTagManager(), var1);
        }
    }

    @Override
    protected int getExperienceReward(Player param0) {
        if (!this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !this.isSpectator()) {
            int var0 = this.experienceLevel * 7;
            return var0 > 100 ? 100 : var0;
        } else {
            return 0;
        }
    }

    @Override
    protected boolean isAlwaysExperienceDropper() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    protected boolean isMovementNoisy() {
        return !this.abilities.flying && (!this.onGround || !this.isDiscrete());
    }

    public void onUpdateAbilities() {
    }

    @Override
    public Component getName() {
        return new TextComponent(this.gameProfile.getName());
    }

    public PlayerEnderChestContainer getEnderChestInventory() {
        return this.enderChestInventory;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot param0) {
        if (param0 == EquipmentSlot.MAINHAND) {
            return this.inventory.getSelected();
        } else if (param0 == EquipmentSlot.OFFHAND) {
            return this.inventory.offhand.get(0);
        } else {
            return param0.getType() == EquipmentSlot.Type.ARMOR ? this.inventory.armor.get(param0.getIndex()) : ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot param0, ItemStack param1) {
        if (param0 == EquipmentSlot.MAINHAND) {
            this.playEquipSound(param1);
            this.inventory.items.set(this.inventory.selected, param1);
        } else if (param0 == EquipmentSlot.OFFHAND) {
            this.playEquipSound(param1);
            this.inventory.offhand.set(0, param1);
        } else if (param0.getType() == EquipmentSlot.Type.ARMOR) {
            this.playEquipSound(param1);
            this.inventory.armor.set(param0.getIndex(), param1);
        }

    }

    public boolean addItem(ItemStack param0) {
        this.playEquipSound(param0);
        return this.inventory.add(param0);
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return Lists.newArrayList(this.getMainHandItem(), this.getOffhandItem());
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.inventory.armor;
    }

    public boolean setEntityOnShoulder(CompoundTag param0) {
        if (this.isPassenger() || !this.onGround || this.isInWater()) {
            return false;
        } else if (this.getShoulderEntityLeft().isEmpty()) {
            this.setShoulderEntityLeft(param0);
            this.timeEntitySatOnShoulder = this.level.getGameTime();
            return true;
        } else if (this.getShoulderEntityRight().isEmpty()) {
            this.setShoulderEntityRight(param0);
            this.timeEntitySatOnShoulder = this.level.getGameTime();
            return true;
        } else {
            return false;
        }
    }

    protected void removeEntitiesOnShoulder() {
        if (this.timeEntitySatOnShoulder + 20L < this.level.getGameTime()) {
            this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
            this.setShoulderEntityLeft(new CompoundTag());
            this.respawnEntityOnShoulder(this.getShoulderEntityRight());
            this.setShoulderEntityRight(new CompoundTag());
        }

    }

    private void respawnEntityOnShoulder(CompoundTag param0) {
        if (!this.level.isClientSide && !param0.isEmpty()) {
            EntityType.create(param0, this.level).ifPresent(param0x -> {
                if (param0x instanceof TamableAnimal) {
                    ((TamableAnimal)param0x).setOwnerUUID(this.uuid);
                }

                param0x.setPos(this.getX(), this.getY() + 0.7F, this.getZ());
                ((ServerLevel)this.level).addWithUUID(param0x);
            });
        }

    }

    @Override
    public abstract boolean isSpectator();

    @Override
    public boolean isSwimming() {
        return !this.abilities.flying && !this.isSpectator() && super.isSwimming();
    }

    public abstract boolean isCreative();

    @Override
    public boolean isPushedByFluid() {
        return !this.abilities.flying;
    }

    public Scoreboard getScoreboard() {
        return this.level.getScoreboard();
    }

    @Override
    public Component getDisplayName() {
        MutableComponent var0 = PlayerTeam.formatNameForTeam(this.getTeam(), this.getName());
        return this.decorateDisplayNameComponent(var0);
    }

    private MutableComponent decorateDisplayNameComponent(MutableComponent param0) {
        String var0 = this.getGameProfile().getName();
        return param0.withStyle(
            param1 -> param1.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + var0 + " "))
                    .withHoverEvent(this.createHoverEvent())
                    .withInsertion(var0)
        );
    }

    @Override
    public String getScoreboardName() {
        return this.getGameProfile().getName();
    }

    @Override
    public float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        switch(param0) {
            case SWIMMING:
            case FALL_FLYING:
            case SPIN_ATTACK:
                return 0.4F;
            case CROUCHING:
                return 1.27F;
            default:
                return 1.62F;
        }
    }

    @Override
    public void setAbsorptionAmount(float param0) {
        if (param0 < 0.0F) {
            param0 = 0.0F;
        }

        this.getEntityData().set(DATA_PLAYER_ABSORPTION_ID, param0);
    }

    @Override
    public float getAbsorptionAmount() {
        return this.getEntityData().get(DATA_PLAYER_ABSORPTION_ID);
    }

    public static UUID createPlayerUUID(GameProfile param0) {
        UUID var0 = param0.getId();
        if (var0 == null) {
            var0 = createPlayerUUID(param0.getName());
        }

        return var0;
    }

    public static UUID createPlayerUUID(String param0) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + param0).getBytes(StandardCharsets.UTF_8));
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isModelPartShown(PlayerModelPart param0) {
        return (this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) & param0.getMask()) == param0.getMask();
    }

    @Override
    public SlotAccess getSlot(int param0) {
        if (param0 >= 0 && param0 < this.inventory.items.size()) {
            return SlotAccess.forContainer(this.inventory, param0);
        } else {
            int var0 = param0 - 200;
            return var0 >= 0 && var0 < this.enderChestInventory.getContainerSize()
                ? SlotAccess.forContainer(this.enderChestInventory, var0)
                : super.getSlot(param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    @OnlyIn(Dist.CLIENT)
    public void setReducedDebugInfo(boolean param0) {
        this.reducedDebugInfo = param0;
    }

    @Override
    public void setRemainingFireTicks(int param0) {
        super.setRemainingFireTicks(this.abilities.invulnerable ? Math.min(param0, 1) : param0);
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.entityData.get(DATA_PLAYER_MAIN_HAND) == 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public void setMainArm(HumanoidArm param0) {
        this.entityData.set(DATA_PLAYER_MAIN_HAND, (byte)(param0 == HumanoidArm.LEFT ? 0 : 1));
    }

    public CompoundTag getShoulderEntityLeft() {
        return this.entityData.get(DATA_SHOULDER_LEFT);
    }

    protected void setShoulderEntityLeft(CompoundTag param0) {
        this.entityData.set(DATA_SHOULDER_LEFT, param0);
    }

    public CompoundTag getShoulderEntityRight() {
        return this.entityData.get(DATA_SHOULDER_RIGHT);
    }

    protected void setShoulderEntityRight(CompoundTag param0) {
        this.entityData.set(DATA_SHOULDER_RIGHT, param0);
    }

    public float getCurrentItemAttackStrengthDelay() {
        return (float)(1.0 / this.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0);
    }

    public float getAttackStrengthScale(float param0) {
        return Mth.clamp(((float)this.attackStrengthTicker + param0) / this.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
    }

    public void resetAttackStrengthTicker() {
        this.attackStrengthTicker = 0;
    }

    public ItemCooldowns getCooldowns() {
        return this.cooldowns;
    }

    @Override
    protected float getBlockSpeedFactor() {
        return !this.abilities.flying && !this.isFallFlying() ? super.getBlockSpeedFactor() : 1.0F;
    }

    public float getLuck() {
        return (float)this.getAttributeValue(Attributes.LUCK);
    }

    public boolean canUseGameMasterBlocks() {
        return this.abilities.instabuild && this.getPermissionLevel() >= 2;
    }

    @Override
    public boolean canTakeItem(ItemStack param0) {
        EquipmentSlot var0 = Mob.getEquipmentSlotForItem(param0);
        return this.getItemBySlot(var0).isEmpty();
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        return POSES.getOrDefault(param0, STANDING_DIMENSIONS);
    }

    @Override
    public ImmutableList<Pose> getDismountPoses() {
        return ImmutableList.of(Pose.STANDING, Pose.CROUCHING, Pose.SWIMMING);
    }

    @Override
    public ItemStack getProjectile(ItemStack param0) {
        if (!(param0.getItem() instanceof ProjectileWeaponItem)) {
            return ItemStack.EMPTY;
        } else {
            Predicate<ItemStack> var0 = ((ProjectileWeaponItem)param0.getItem()).getSupportedHeldProjectiles();
            ItemStack var1 = ProjectileWeaponItem.getHeldProjectile(this, var0);
            if (!var1.isEmpty()) {
                return var1;
            } else {
                var0 = ((ProjectileWeaponItem)param0.getItem()).getAllSupportedProjectiles();

                for(int var2 = 0; var2 < this.inventory.getContainerSize(); ++var2) {
                    ItemStack var3 = this.inventory.getItem(var2);
                    if (var0.test(var3)) {
                        return var3;
                    }
                }

                return this.abilities.instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
            }
        }
    }

    @Override
    public ItemStack eat(Level param0, ItemStack param1) {
        this.getFoodData().eat(param1.getItem(), param1);
        this.awardStat(Stats.ITEM_USED.get(param1.getItem()));
        param0.playSound(
            null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, param0.random.nextFloat() * 0.1F + 0.9F
        );
        if (this instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)this, param1);
        }

        return super.eat(param0, param1);
    }

    @Override
    protected boolean shouldRemoveSoulSpeed(BlockState param0) {
        return this.abilities.flying || super.shouldRemoveSoulSpeed(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getRopeHoldPosition(float param0) {
        double var0 = 0.22 * (this.getMainArm() == HumanoidArm.RIGHT ? -1.0 : 1.0);
        float var1 = Mth.lerp(param0 * 0.5F, this.xRot, this.xRotO) * (float) (Math.PI / 180.0);
        float var2 = Mth.lerp(param0, this.yBodyRotO, this.yBodyRot) * (float) (Math.PI / 180.0);
        if (this.isFallFlying() || this.isAutoSpinAttack()) {
            Vec3 var3 = this.getViewVector(param0);
            Vec3 var4 = this.getDeltaMovement();
            double var5 = Entity.getHorizontalDistanceSqr(var4);
            double var6 = Entity.getHorizontalDistanceSqr(var3);
            float var9;
            if (var5 > 0.0 && var6 > 0.0) {
                double var7 = (var4.x * var3.x + var4.z * var3.z) / Math.sqrt(var5 * var6);
                double var8 = var4.x * var3.z - var4.z * var3.x;
                var9 = (float)(Math.signum(var8) * Math.acos(var7));
            } else {
                var9 = 0.0F;
            }

            return this.getPosition(param0).add(new Vec3(var0, -0.11, 0.85).zRot(-var9).xRot(-var1).yRot(-var2));
        } else if (this.isVisuallySwimming()) {
            return this.getPosition(param0).add(new Vec3(var0, 0.2, -0.15).xRot(-var1).yRot(-var2));
        } else {
            double var11 = this.getBoundingBox().getYsize() - 1.0;
            double var12 = this.isCrouching() ? -0.2 : 0.07;
            return this.getPosition(param0).add(new Vec3(var0, var11, var12).yRot(-var2));
        }
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isScoping() {
        return this.isUsingItem() && this.getUseItem().is(Items.SPYGLASS);
    }

    public static enum BedSleepingProblem {
        NOT_POSSIBLE_HERE,
        NOT_POSSIBLE_NOW(new TranslatableComponent("block.minecraft.bed.no_sleep")),
        TOO_FAR_AWAY(new TranslatableComponent("block.minecraft.bed.too_far_away")),
        OBSTRUCTED(new TranslatableComponent("block.minecraft.bed.obstructed")),
        OTHER_PROBLEM,
        NOT_SAFE(new TranslatableComponent("block.minecraft.bed.not_safe"));

        @Nullable
        private final Component message;

        private BedSleepingProblem() {
            this.message = null;
        }

        private BedSleepingProblem(Component param0) {
            this.message = param0;
        }

        @Nullable
        public Component getMessage() {
            return this.message;
        }
    }
}

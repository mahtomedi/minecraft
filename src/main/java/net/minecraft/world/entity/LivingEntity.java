package net.minecraft.world.entity;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.Dynamic;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEquippedItemPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.world.entity.ai.attributes.ModifiableAttributeMap;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

public abstract class LivingEntity extends Entity {
    private static final UUID SPEED_MODIFIER_SPRINTING_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final AttributeModifier SPEED_MODIFIER_SPRINTING = new AttributeModifier(
            SPEED_MODIFIER_SPRINTING_UUID, "Sprinting speed boost", 0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL
        )
        .setSerialize(false);
    protected static final EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_EFFECT_COLOR_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STINGER_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> SLEEPING_POS_ID = SynchedEntityData.defineId(
        LivingEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS
    );
    protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2F, 0.2F);
    private BaseAttributeMap attributes;
    private final CombatTracker combatTracker = new CombatTracker(this);
    private final Map<MobEffect, MobEffectInstance> activeEffects = Maps.newHashMap();
    private final NonNullList<ItemStack> lastHandItemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> lastArmorItemStacks = NonNullList.withSize(4, ItemStack.EMPTY);
    public boolean swinging;
    public InteractionHand swingingArm;
    public int swingTime;
    public int removeArrowTime;
    public int removeStingerTime;
    public int hurtTime;
    public int hurtDuration;
    public float hurtDir;
    public int deathTime;
    public float oAttackAnim;
    public float attackAnim;
    protected int attackStrengthTicker;
    public float animationSpeedOld;
    public float animationSpeed;
    public float animationPosition;
    public final int invulnerableDuration = 20;
    public final float timeOffs;
    public final float rotA;
    public float yBodyRot;
    public float yBodyRotO;
    public float yHeadRot;
    public float yHeadRotO;
    public float flyingSpeed = 0.02F;
    protected Player lastHurtByPlayer;
    protected int lastHurtByPlayerTime;
    protected boolean dead;
    protected int noActionTime;
    protected float oRun;
    protected float run;
    protected float animStep;
    protected float animStepO;
    protected float rotOffs;
    protected int deathScore;
    protected float lastHurt;
    protected boolean jumping;
    public float xxa;
    public float yya;
    public float zza;
    protected int lerpSteps;
    protected double lerpX;
    protected double lerpY;
    protected double lerpZ;
    protected double lerpYRot;
    protected double lerpXRot;
    protected double lyHeadRot;
    protected int lerpHeadSteps;
    private boolean effectsDirty = true;
    @Nullable
    private LivingEntity lastHurtByMob;
    private int lastHurtByMobTimestamp;
    private LivingEntity lastHurtMob;
    private int lastHurtMobTimestamp;
    private float speed;
    private int noJumpDelay;
    private float absorptionAmount;
    protected ItemStack useItem = ItemStack.EMPTY;
    protected int useItemRemaining;
    protected int fallFlyTicks;
    private BlockPos lastPos;
    private DamageSource lastDamageSource;
    private long lastDamageStamp;
    protected int autoSpinAttackTicks;
    private float swimAmount;
    private float swimAmountO;
    protected Brain<?> brain;

    protected LivingEntity(EntityType<? extends LivingEntity> param0, Level param1) {
        super(param0, param1);
        this.registerAttributes();
        this.setHealth(this.getMaxHealth());
        this.blocksBuilding = true;
        this.rotA = (float)((Math.random() + 1.0) * 0.01F);
        this.reapplyPosition();
        this.timeOffs = (float)Math.random() * 12398.0F;
        this.yRot = (float)(Math.random() * (float) (Math.PI * 2));
        this.yHeadRot = this.yRot;
        this.maxUpStep = 0.6F;
        this.brain = this.makeBrain(new Dynamic<>(NbtOps.INSTANCE, new CompoundTag()));
    }

    public Brain<?> getBrain() {
        return this.brain;
    }

    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return new Brain<>(ImmutableList.of(), ImmutableList.of(), param0);
    }

    @Override
    public void kill() {
        this.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
    }

    public boolean canAttackType(EntityType<?> param0) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_LIVING_ENTITY_FLAGS, (byte)0);
        this.entityData.define(DATA_EFFECT_COLOR_ID, 0);
        this.entityData.define(DATA_EFFECT_AMBIENCE_ID, false);
        this.entityData.define(DATA_ARROW_COUNT_ID, 0);
        this.entityData.define(DATA_STINGER_COUNT_ID, 0);
        this.entityData.define(DATA_HEALTH_ID, 1.0F);
        this.entityData.define(SLEEPING_POS_ID, Optional.empty());
    }

    protected void registerAttributes() {
        this.getAttributes().registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ARMOR);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
    }

    @Override
    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
        if (!this.isInWater()) {
            this.updateInWaterState();
        }

        if (!this.level.isClientSide && this.fallDistance > 3.0F && param1) {
            float var0 = (float)Mth.ceil(this.fallDistance - 3.0F);
            if (!param2.isAir()) {
                double var1 = Math.min((double)(0.2F + var0 / 15.0F), 2.5);
                int var2 = (int)(150.0 * var1);
                ((ServerLevel)this.level)
                    .sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, param2), this.getX(), this.getY(), this.getZ(), var2, 0.0, 0.0, 0.0, 0.15F);
            }
        }

        super.checkFallDamage(param0, param1, param2, param3);
    }

    public boolean canBreatheUnderwater() {
        return this.getMobType() == MobType.UNDEAD;
    }

    @OnlyIn(Dist.CLIENT)
    public float getSwimAmount(float param0) {
        return Mth.lerp(param0, this.swimAmountO, this.swimAmount);
    }

    @Override
    public void baseTick() {
        this.oAttackAnim = this.attackAnim;
        if (this.firstTick) {
            this.getSleepingPos().ifPresent(this::setPosToBed);
        }

        super.baseTick();
        this.level.getProfiler().push("livingEntityBaseTick");
        boolean var0 = this instanceof Player;
        if (this.isAlive()) {
            if (this.isInWall()) {
                this.hurt(DamageSource.IN_WALL, 1.0F);
            } else if (var0 && !this.level.getWorldBorder().isWithinBounds(this.getBoundingBox())) {
                double var1 = this.level.getWorldBorder().getDistanceToBorder(this) + this.level.getWorldBorder().getDamageSafeZone();
                if (var1 < 0.0) {
                    double var2 = this.level.getWorldBorder().getDamagePerBlock();
                    if (var2 > 0.0) {
                        this.hurt(DamageSource.IN_WALL, (float)Math.max(1, Mth.floor(-var1 * var2)));
                    }
                }
            }
        }

        if (this.fireImmune() || this.level.isClientSide) {
            this.clearFire();
        }

        boolean var3 = var0 && ((Player)this).abilities.invulnerable;
        if (this.isAlive()) {
            if (this.isUnderLiquid(FluidTags.WATER)
                && this.level.getBlockState(new BlockPos(this.getX(), this.getEyeY(), this.getZ())).getBlock() != Blocks.BUBBLE_COLUMN) {
                if (!this.canBreatheUnderwater() && !MobEffectUtil.hasWaterBreathing(this) && !var3) {
                    this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
                    if (this.getAirSupply() == -20) {
                        this.setAirSupply(0);
                        Vec3 var4 = this.getDeltaMovement();

                        for(int var5 = 0; var5 < 8; ++var5) {
                            float var6 = this.random.nextFloat() - this.random.nextFloat();
                            float var7 = this.random.nextFloat() - this.random.nextFloat();
                            float var8 = this.random.nextFloat() - this.random.nextFloat();
                            this.level
                                .addParticle(
                                    ParticleTypes.BUBBLE,
                                    this.getX() + (double)var6,
                                    this.getY() + (double)var7,
                                    this.getZ() + (double)var8,
                                    var4.x,
                                    var4.y,
                                    var4.z
                                );
                        }

                        this.hurt(DamageSource.DROWN, 2.0F);
                    }
                }

                if (!this.level.isClientSide && this.isPassenger() && this.getVehicle() != null && !this.getVehicle().rideableUnderWater()) {
                    this.stopRiding();
                }
            } else if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
            }

            if (!this.level.isClientSide) {
                BlockPos var9 = new BlockPos(this);
                if (!Objects.equal(this.lastPos, var9)) {
                    this.lastPos = var9;
                    this.onChangedBlock(var9);
                }
            }
        }

        if (this.isAlive() && this.isInWaterRainOrBubble()) {
            this.clearFire();
        }

        if (this.hurtTime > 0) {
            --this.hurtTime;
        }

        if (this.invulnerableTime > 0 && !(this instanceof ServerPlayer)) {
            --this.invulnerableTime;
        }

        if (this.getHealth() <= 0.0F) {
            this.tickDeath();
        }

        if (this.lastHurtByPlayerTime > 0) {
            --this.lastHurtByPlayerTime;
        } else {
            this.lastHurtByPlayer = null;
        }

        if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
            this.lastHurtMob = null;
        }

        if (this.lastHurtByMob != null) {
            if (!this.lastHurtByMob.isAlive()) {
                this.setLastHurtByMob(null);
            } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
                this.setLastHurtByMob(null);
            }
        }

        this.tickEffects();
        this.animStepO = this.animStep;
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
        this.level.getProfiler().pop();
    }

    protected void onChangedBlock(BlockPos param0) {
        int var0 = EnchantmentHelper.getEnchantmentLevel(Enchantments.FROST_WALKER, this);
        if (var0 > 0) {
            FrostWalkerEnchantment.onEntityMoved(this, this.level, param0, var0);
        }

    }

    public boolean isBaby() {
        return false;
    }

    public float getScale() {
        return this.isBaby() ? 0.5F : 1.0F;
    }

    @Override
    public boolean rideableUnderWater() {
        return false;
    }

    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 20) {
            this.remove();

            for(int var0 = 0; var0 < 20; ++var0) {
                double var1 = this.random.nextGaussian() * 0.02;
                double var2 = this.random.nextGaussian() * 0.02;
                double var3 = this.random.nextGaussian() * 0.02;
                this.level.addParticle(ParticleTypes.POOF, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), var1, var2, var3);
            }
        }

    }

    protected boolean shouldDropExperience() {
        return !this.isBaby();
    }

    protected int decreaseAirSupply(int param0) {
        int var0 = EnchantmentHelper.getRespiration(this);
        return var0 > 0 && this.random.nextInt(var0 + 1) > 0 ? param0 : param0 - 1;
    }

    protected int increaseAirSupply(int param0) {
        return Math.min(param0 + 4, this.getMaxAirSupply());
    }

    protected int getExperienceReward(Player param0) {
        return 0;
    }

    protected boolean isAlwaysExperienceDropper() {
        return false;
    }

    public Random getRandom() {
        return this.random;
    }

    @Nullable
    public LivingEntity getLastHurtByMob() {
        return this.lastHurtByMob;
    }

    public int getLastHurtByMobTimestamp() {
        return this.lastHurtByMobTimestamp;
    }

    public void setLastHurtByMob(@Nullable LivingEntity param0) {
        this.lastHurtByMob = param0;
        this.lastHurtByMobTimestamp = this.tickCount;
    }

    @Nullable
    public LivingEntity getLastHurtMob() {
        return this.lastHurtMob;
    }

    public int getLastHurtMobTimestamp() {
        return this.lastHurtMobTimestamp;
    }

    public void setLastHurtMob(Entity param0) {
        if (param0 instanceof LivingEntity) {
            this.lastHurtMob = (LivingEntity)param0;
        } else {
            this.lastHurtMob = null;
        }

        this.lastHurtMobTimestamp = this.tickCount;
    }

    public int getNoActionTime() {
        return this.noActionTime;
    }

    public void setNoActionTime(int param0) {
        this.noActionTime = param0;
    }

    protected void playEquipSound(ItemStack param0) {
        if (!param0.isEmpty()) {
            SoundEvent var0 = SoundEvents.ARMOR_EQUIP_GENERIC;
            Item var1 = param0.getItem();
            if (var1 instanceof ArmorItem) {
                var0 = ((ArmorItem)var1).getMaterial().getEquipSound();
            } else if (var1 == Items.ELYTRA) {
                var0 = SoundEvents.ARMOR_EQUIP_ELYTRA;
            }

            this.playSound(var0, 1.0F, 1.0F);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putFloat("Health", this.getHealth());
        param0.putShort("HurtTime", (short)this.hurtTime);
        param0.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
        param0.putShort("DeathTime", (short)this.deathTime);
        param0.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        param0.put("Attributes", SharedMonsterAttributes.saveAttributes(this.getAttributes()));
        if (!this.activeEffects.isEmpty()) {
            ListTag var0 = new ListTag();

            for(MobEffectInstance var1 : this.activeEffects.values()) {
                var0.add(var1.save(new CompoundTag()));
            }

            param0.put("ActiveEffects", var0);
        }

        param0.putBoolean("FallFlying", this.isFallFlying());
        this.getSleepingPos().ifPresent(param1 -> {
            param0.putInt("SleepingX", param1.getX());
            param0.putInt("SleepingY", param1.getY());
            param0.putInt("SleepingZ", param1.getZ());
        });
        param0.put("Brain", this.brain.serialize(NbtOps.INSTANCE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        this.setAbsorptionAmount(param0.getFloat("AbsorptionAmount"));
        if (param0.contains("Attributes", 9) && this.level != null && !this.level.isClientSide) {
            SharedMonsterAttributes.loadAttributes(this.getAttributes(), param0.getList("Attributes", 10));
        }

        if (param0.contains("ActiveEffects", 9)) {
            ListTag var0 = param0.getList("ActiveEffects", 10);

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                CompoundTag var2 = var0.getCompound(var1);
                MobEffectInstance var3 = MobEffectInstance.load(var2);
                if (var3 != null) {
                    this.activeEffects.put(var3.getEffect(), var3);
                }
            }
        }

        if (param0.contains("Health", 99)) {
            this.setHealth(param0.getFloat("Health"));
        }

        this.hurtTime = param0.getShort("HurtTime");
        this.deathTime = param0.getShort("DeathTime");
        this.lastHurtByMobTimestamp = param0.getInt("HurtByTimestamp");
        if (param0.contains("Team", 8)) {
            String var4 = param0.getString("Team");
            PlayerTeam var5 = this.level.getScoreboard().getPlayerTeam(var4);
            boolean var6 = var5 != null && this.level.getScoreboard().addPlayerToTeam(this.getStringUUID(), var5);
            if (!var6) {
                LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", var4);
            }
        }

        if (param0.getBoolean("FallFlying")) {
            this.setSharedFlag(7, true);
        }

        if (param0.contains("SleepingX", 99) && param0.contains("SleepingY", 99) && param0.contains("SleepingZ", 99)) {
            BlockPos var7 = new BlockPos(param0.getInt("SleepingX"), param0.getInt("SleepingY"), param0.getInt("SleepingZ"));
            this.setSleepingPos(var7);
            this.entityData.set(DATA_POSE, Pose.SLEEPING);
            if (!this.firstTick) {
                this.setPosToBed(var7);
            }
        }

        if (param0.contains("Brain", 10)) {
            this.brain = this.makeBrain(new Dynamic<>(NbtOps.INSTANCE, param0.get("Brain")));
        }

    }

    protected void tickEffects() {
        Iterator<MobEffect> var0 = this.activeEffects.keySet().iterator();

        try {
            while(var0.hasNext()) {
                MobEffect var1 = var0.next();
                MobEffectInstance var2 = this.activeEffects.get(var1);
                if (!var2.tick(this, () -> this.onEffectUpdated(var2, true))) {
                    if (!this.level.isClientSide) {
                        var0.remove();
                        this.onEffectRemoved(var2);
                    }
                } else if (var2.getDuration() % 600 == 0) {
                    this.onEffectUpdated(var2, false);
                }
            }
        } catch (ConcurrentModificationException var11) {
        }

        if (this.effectsDirty) {
            if (!this.level.isClientSide) {
                this.updateInvisibilityStatus();
            }

            this.effectsDirty = false;
        }

        int var3 = this.entityData.get(DATA_EFFECT_COLOR_ID);
        boolean var4 = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
        if (var3 > 0) {
            boolean var5;
            if (this.isInvisible()) {
                var5 = this.random.nextInt(15) == 0;
            } else {
                var5 = this.random.nextBoolean();
            }

            if (var4) {
                var5 &= this.random.nextInt(5) == 0;
            }

            if (var5 && var3 > 0) {
                double var7 = (double)(var3 >> 16 & 0xFF) / 255.0;
                double var8 = (double)(var3 >> 8 & 0xFF) / 255.0;
                double var9 = (double)(var3 >> 0 & 0xFF) / 255.0;
                this.level
                    .addParticle(
                        var4 ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT,
                        this.getRandomX(0.5),
                        this.getRandomY(),
                        this.getRandomZ(0.5),
                        var7,
                        var8,
                        var9
                    );
            }
        }

    }

    protected void updateInvisibilityStatus() {
        if (this.activeEffects.isEmpty()) {
            this.removeEffectParticles();
            this.setInvisible(false);
        } else {
            Collection<MobEffectInstance> var0 = this.activeEffects.values();
            this.entityData.set(DATA_EFFECT_AMBIENCE_ID, areAllEffectsAmbient(var0));
            this.entityData.set(DATA_EFFECT_COLOR_ID, PotionUtils.getColor(var0));
            this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
        }

    }

    public double getVisibilityPercent(@Nullable Entity param0) {
        double var0 = 1.0;
        if (this.isDiscrete()) {
            var0 *= 0.8;
        }

        if (this.isInvisible()) {
            float var1 = this.getArmorCoverPercentage();
            if (var1 < 0.1F) {
                var1 = 0.1F;
            }

            var0 *= 0.7 * (double)var1;
        }

        if (param0 != null) {
            ItemStack var2 = this.getItemBySlot(EquipmentSlot.HEAD);
            Item var3 = var2.getItem();
            EntityType<?> var4 = param0.getType();
            if (var4 == EntityType.SKELETON && var3 == Items.SKELETON_SKULL
                || var4 == EntityType.ZOMBIE && var3 == Items.ZOMBIE_HEAD
                || var4 == EntityType.CREEPER && var3 == Items.CREEPER_HEAD) {
                var0 *= 0.5;
            }
        }

        return var0;
    }

    public boolean canAttack(LivingEntity param0) {
        return true;
    }

    public boolean canAttack(LivingEntity param0, TargetingConditions param1) {
        return param1.test(this, param0);
    }

    public static boolean areAllEffectsAmbient(Collection<MobEffectInstance> param0) {
        for(MobEffectInstance var0 : param0) {
            if (!var0.isAmbient()) {
                return false;
            }
        }

        return true;
    }

    protected void removeEffectParticles() {
        this.entityData.set(DATA_EFFECT_AMBIENCE_ID, false);
        this.entityData.set(DATA_EFFECT_COLOR_ID, 0);
    }

    public boolean removeAllEffects() {
        if (this.level.isClientSide) {
            return false;
        } else {
            Iterator<MobEffectInstance> var0 = this.activeEffects.values().iterator();

            boolean var1;
            for(var1 = false; var0.hasNext(); var1 = true) {
                this.onEffectRemoved(var0.next());
                var0.remove();
            }

            return var1;
        }
    }

    public Collection<MobEffectInstance> getActiveEffects() {
        return this.activeEffects.values();
    }

    public Map<MobEffect, MobEffectInstance> getActiveEffectsMap() {
        return this.activeEffects;
    }

    public boolean hasEffect(MobEffect param0) {
        return this.activeEffects.containsKey(param0);
    }

    @Nullable
    public MobEffectInstance getEffect(MobEffect param0) {
        return this.activeEffects.get(param0);
    }

    public boolean addEffect(MobEffectInstance param0) {
        if (!this.canBeAffected(param0)) {
            return false;
        } else {
            MobEffectInstance var0 = this.activeEffects.get(param0.getEffect());
            if (var0 == null) {
                this.activeEffects.put(param0.getEffect(), param0);
                this.onEffectAdded(param0);
                return true;
            } else if (var0.update(param0)) {
                this.onEffectUpdated(var0, true);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean canBeAffected(MobEffectInstance param0) {
        if (this.getMobType() == MobType.UNDEAD) {
            MobEffect var0 = param0.getEffect();
            if (var0 == MobEffects.REGENERATION || var0 == MobEffects.POISON) {
                return false;
            }
        }

        return true;
    }

    public boolean isInvertedHealAndHarm() {
        return this.getMobType() == MobType.UNDEAD;
    }

    @Nullable
    public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect param0) {
        return this.activeEffects.remove(param0);
    }

    public boolean removeEffect(MobEffect param0) {
        MobEffectInstance var0 = this.removeEffectNoUpdate(param0);
        if (var0 != null) {
            this.onEffectRemoved(var0);
            return true;
        } else {
            return false;
        }
    }

    protected void onEffectAdded(MobEffectInstance param0) {
        this.effectsDirty = true;
        if (!this.level.isClientSide) {
            param0.getEffect().addAttributeModifiers(this, this.getAttributes(), param0.getAmplifier());
        }

    }

    protected void onEffectUpdated(MobEffectInstance param0, boolean param1) {
        this.effectsDirty = true;
        if (param1 && !this.level.isClientSide) {
            MobEffect var0 = param0.getEffect();
            var0.removeAttributeModifiers(this, this.getAttributes(), param0.getAmplifier());
            var0.addAttributeModifiers(this, this.getAttributes(), param0.getAmplifier());
        }

    }

    protected void onEffectRemoved(MobEffectInstance param0) {
        this.effectsDirty = true;
        if (!this.level.isClientSide) {
            param0.getEffect().removeAttributeModifiers(this, this.getAttributes(), param0.getAmplifier());
        }

    }

    public void heal(float param0) {
        float var0 = this.getHealth();
        if (var0 > 0.0F) {
            this.setHealth(var0 + param0);
        }

    }

    public float getHealth() {
        return this.entityData.get(DATA_HEALTH_ID);
    }

    public void setHealth(float param0) {
        this.entityData.set(DATA_HEALTH_ID, Mth.clamp(param0, 0.0F, this.getMaxHealth()));
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else if (this.level.isClientSide) {
            return false;
        } else if (this.getHealth() <= 0.0F) {
            return false;
        } else if (param0.isFire() && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        } else {
            if (this.isSleeping() && !this.level.isClientSide) {
                this.stopSleeping();
            }

            this.noActionTime = 0;
            float var0 = param1;
            if ((param0 == DamageSource.ANVIL || param0 == DamageSource.FALLING_BLOCK) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                this.getItemBySlot(EquipmentSlot.HEAD)
                    .hurtAndBreak(
                        (int)(param1 * 4.0F + this.random.nextFloat() * param1 * 2.0F), this, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.HEAD)
                    );
                param1 *= 0.75F;
            }

            boolean var1 = false;
            float var2 = 0.0F;
            if (param1 > 0.0F && this.isDamageSourceBlocked(param0)) {
                this.hurtCurrentlyUsedShield(param1);
                var2 = param1;
                param1 = 0.0F;
                if (!param0.isProjectile()) {
                    Entity var3 = param0.getDirectEntity();
                    if (var3 instanceof LivingEntity) {
                        this.blockUsingShield((LivingEntity)var3);
                    }
                }

                var1 = true;
            }

            this.animationSpeed = 1.5F;
            boolean var4 = true;
            if ((float)this.invulnerableTime > 10.0F) {
                if (param1 <= this.lastHurt) {
                    return false;
                }

                this.actuallyHurt(param0, param1 - this.lastHurt);
                this.lastHurt = param1;
                var4 = false;
            } else {
                this.lastHurt = param1;
                this.invulnerableTime = 20;
                this.actuallyHurt(param0, param1);
                this.hurtDuration = 10;
                this.hurtTime = this.hurtDuration;
            }

            this.hurtDir = 0.0F;
            Entity var5 = param0.getEntity();
            if (var5 != null) {
                if (var5 instanceof LivingEntity) {
                    this.setLastHurtByMob((LivingEntity)var5);
                }

                if (var5 instanceof Player) {
                    this.lastHurtByPlayerTime = 100;
                    this.lastHurtByPlayer = (Player)var5;
                } else if (var5 instanceof Wolf) {
                    Wolf var6 = (Wolf)var5;
                    if (var6.isTame()) {
                        this.lastHurtByPlayerTime = 100;
                        LivingEntity var7 = var6.getOwner();
                        if (var7 != null && var7.getType() == EntityType.PLAYER) {
                            this.lastHurtByPlayer = (Player)var7;
                        } else {
                            this.lastHurtByPlayer = null;
                        }
                    }
                }
            }

            if (var4) {
                if (var1) {
                    this.level.broadcastEntityEvent(this, (byte)29);
                } else if (param0 instanceof EntityDamageSource && ((EntityDamageSource)param0).isThorns()) {
                    this.level.broadcastEntityEvent(this, (byte)33);
                } else {
                    byte var8;
                    if (param0 == DamageSource.DROWN) {
                        var8 = 36;
                    } else if (param0.isFire()) {
                        var8 = 37;
                    } else if (param0 == DamageSource.SWEET_BERRY_BUSH) {
                        var8 = 44;
                    } else {
                        var8 = 2;
                    }

                    this.level.broadcastEntityEvent(this, var8);
                }

                if (param0 != DamageSource.DROWN && (!var1 || param1 > 0.0F)) {
                    this.markHurt();
                }

                if (var5 != null) {
                    double var12 = var5.getX() - this.getX();

                    double var13;
                    for(var13 = var5.getZ() - this.getZ(); var12 * var12 + var13 * var13 < 1.0E-4; var13 = (Math.random() - Math.random()) * 0.01) {
                        var12 = (Math.random() - Math.random()) * 0.01;
                    }

                    this.hurtDir = (float)(Mth.atan2(var13, var12) * 180.0F / (float)Math.PI - (double)this.yRot);
                    this.knockback(var5, 0.4F, var12, var13);
                } else {
                    this.hurtDir = (float)((int)(Math.random() * 2.0) * 180);
                }
            }

            if (this.getHealth() <= 0.0F) {
                if (!this.checkTotemDeathProtection(param0)) {
                    SoundEvent var14 = this.getDeathSound();
                    if (var4 && var14 != null) {
                        this.playSound(var14, this.getSoundVolume(), this.getVoicePitch());
                    }

                    this.die(param0);
                }
            } else if (var4) {
                this.playHurtSound(param0);
            }

            boolean var15 = !var1 || param1 > 0.0F;
            if (var15) {
                this.lastDamageSource = param0;
                this.lastDamageStamp = this.level.getGameTime();
            }

            if (this instanceof ServerPlayer) {
                CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer)this, param0, var0, param1, var1);
                if (var2 > 0.0F && var2 < 3.4028235E37F) {
                    ((ServerPlayer)this).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(var2 * 10.0F));
                }
            }

            if (var5 instanceof ServerPlayer) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)var5, this, param0, var0, param1, var1);
            }

            return var15;
        }
    }

    protected void blockUsingShield(LivingEntity param0) {
        param0.blockedByShield(this);
    }

    protected void blockedByShield(LivingEntity param0) {
        param0.knockback(this, 0.5F, param0.getX() - this.getX(), param0.getZ() - this.getZ());
    }

    private boolean checkTotemDeathProtection(DamageSource param0) {
        if (param0.isBypassInvul()) {
            return false;
        } else {
            ItemStack var0 = null;

            for(InteractionHand var1 : InteractionHand.values()) {
                ItemStack var2 = this.getItemInHand(var1);
                if (var2.getItem() == Items.TOTEM_OF_UNDYING) {
                    var0 = var2.copy();
                    var2.shrink(1);
                    break;
                }
            }

            if (var0 != null) {
                if (this instanceof ServerPlayer) {
                    ServerPlayer var3 = (ServerPlayer)this;
                    var3.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                    CriteriaTriggers.USED_TOTEM.trigger(var3, var0);
                }

                this.setHealth(1.0F);
                this.removeAllEffects();
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                this.level.broadcastEntityEvent(this, (byte)35);
            }

            return var0 != null;
        }
    }

    @Nullable
    public DamageSource getLastDamageSource() {
        if (this.level.getGameTime() - this.lastDamageStamp > 40L) {
            this.lastDamageSource = null;
        }

        return this.lastDamageSource;
    }

    protected void playHurtSound(DamageSource param0) {
        SoundEvent var0 = this.getHurtSound(param0);
        if (var0 != null) {
            this.playSound(var0, this.getSoundVolume(), this.getVoicePitch());
        }

    }

    private boolean isDamageSourceBlocked(DamageSource param0) {
        Entity var0 = param0.getDirectEntity();
        boolean var1 = false;
        if (var0 instanceof AbstractArrow) {
            AbstractArrow var2 = (AbstractArrow)var0;
            if (var2.getPierceLevel() > 0) {
                var1 = true;
            }
        }

        if (!param0.isBypassArmor() && this.isBlocking() && !var1) {
            Vec3 var3 = param0.getSourcePosition();
            if (var3 != null) {
                Vec3 var4 = this.getViewVector(1.0F);
                Vec3 var5 = var3.vectorTo(this.position()).normalize();
                var5 = new Vec3(var5.x, 0.0, var5.z);
                if (var5.dot(var4) < 0.0) {
                    return true;
                }
            }
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private void breakItem(ItemStack param0) {
        if (!param0.isEmpty()) {
            if (!this.isSilent()) {
                this.level
                    .playLocalSound(
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        SoundEvents.ITEM_BREAK,
                        this.getSoundSource(),
                        0.8F,
                        0.8F + this.level.random.nextFloat() * 0.4F,
                        false
                    );
            }

            this.spawnItemParticles(param0, 5);
        }

    }

    public void die(DamageSource param0) {
        if (!this.removed && !this.dead) {
            Entity var0 = param0.getEntity();
            LivingEntity var1 = this.getKillCredit();
            if (this.deathScore >= 0 && var1 != null) {
                var1.awardKillScore(this, this.deathScore, param0);
            }

            if (var0 != null) {
                var0.killed(this);
            }

            if (this.isSleeping()) {
                this.stopSleeping();
            }

            this.dead = true;
            this.getCombatTracker().recheckStatus();
            if (!this.level.isClientSide) {
                this.dropAllDeathLoot(param0);
                this.createWitherRose(var1);
            }

            this.level.broadcastEntityEvent(this, (byte)3);
            this.setPose(Pose.DYING);
        }
    }

    protected void createWitherRose(@Nullable LivingEntity param0) {
        if (!this.level.isClientSide) {
            boolean var0 = false;
            if (param0 instanceof WitherBoss) {
                if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    BlockPos var1 = new BlockPos(this);
                    BlockState var2 = Blocks.WITHER_ROSE.defaultBlockState();
                    if (this.level.getBlockState(var1).isAir() && var2.canSurvive(this.level, var1)) {
                        this.level.setBlock(var1, var2, 3);
                        var0 = true;
                    }
                }

                if (!var0) {
                    ItemEntity var3 = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
                    this.level.addFreshEntity(var3);
                }
            }

        }
    }

    protected void dropAllDeathLoot(DamageSource param0) {
        Entity var0 = param0.getEntity();
        int var1;
        if (var0 instanceof Player) {
            var1 = EnchantmentHelper.getMobLooting((LivingEntity)var0);
        } else {
            var1 = 0;
        }

        boolean var3 = this.lastHurtByPlayerTime > 0;
        if (this.shouldDropExperience() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.dropFromLootTable(param0, var3);
            this.dropCustomDeathLoot(param0, var1, var3);
        }

        this.dropEquipment();
        this.dropExperience();
    }

    protected void dropEquipment() {
    }

    protected void dropExperience() {
        if (!this.level.isClientSide
            && (
                this.isAlwaysExperienceDropper()
                    || this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
            )) {
            int var0 = this.getExperienceReward(this.lastHurtByPlayer);

            while(var0 > 0) {
                int var1 = ExperienceOrb.getExperienceValue(var0);
                var0 -= var1;
                this.level.addFreshEntity(new ExperienceOrb(this.level, this.getX(), this.getY(), this.getZ(), var1));
            }
        }

    }

    protected void dropCustomDeathLoot(DamageSource param0, int param1, boolean param2) {
    }

    public ResourceLocation getLootTable() {
        return this.getType().getDefaultLootTable();
    }

    protected void dropFromLootTable(DamageSource param0, boolean param1) {
        ResourceLocation var0 = this.getLootTable();
        LootTable var1 = this.level.getServer().getLootTables().get(var0);
        LootContext.Builder var2 = this.createLootContext(param1, param0);
        var1.getRandomItems(var2.create(LootContextParamSets.ENTITY), this::spawnAtLocation);
    }

    protected LootContext.Builder createLootContext(boolean param0, DamageSource param1) {
        LootContext.Builder var0 = new LootContext.Builder((ServerLevel)this.level)
            .withRandom(this.random)
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.BLOCK_POS, new BlockPos(this))
            .withParameter(LootContextParams.DAMAGE_SOURCE, param1)
            .withOptionalParameter(LootContextParams.KILLER_ENTITY, param1.getEntity())
            .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, param1.getDirectEntity());
        if (param0 && this.lastHurtByPlayer != null) {
            var0 = var0.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
        }

        return var0;
    }

    public void knockback(Entity param0, float param1, double param2, double param3) {
        if (!(this.random.nextDouble() < this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getValue())) {
            this.hasImpulse = true;
            Vec3 var0 = this.getDeltaMovement();
            Vec3 var1 = new Vec3(param2, 0.0, param3).normalize().scale((double)param1);
            this.setDeltaMovement(var0.x / 2.0 - var1.x, this.onGround ? Math.min(0.4, var0.y / 2.0 + (double)param1) : var0.y, var0.z / 2.0 - var1.z);
        }
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.GENERIC_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    protected SoundEvent getFallDamageSound(int param0) {
        return param0 > 4 ? SoundEvents.GENERIC_BIG_FALL : SoundEvents.GENERIC_SMALL_FALL;
    }

    protected SoundEvent getDrinkingSound(ItemStack param0) {
        return param0.getDrinkingSound();
    }

    public SoundEvent getEatingSound(ItemStack param0) {
        return param0.getEatingSound();
    }

    public boolean onLadder() {
        if (this.isSpectator()) {
            return false;
        } else {
            BlockState var0 = this.getFeetBlockState();
            Block var1 = var0.getBlock();
            if (var1 != Blocks.LADDER && var1 != Blocks.VINE && var1 != Blocks.SCAFFOLDING) {
                return var1 instanceof TrapDoorBlock && this.trapdoorUsableAsLadder(new BlockPos(this), var0);
            } else {
                return true;
            }
        }
    }

    public BlockState getFeetBlockState() {
        return this.level.getBlockState(new BlockPos(this));
    }

    private boolean trapdoorUsableAsLadder(BlockPos param0, BlockState param1) {
        if (param1.getValue(TrapDoorBlock.OPEN)) {
            BlockState var0 = this.level.getBlockState(param0.below());
            if (var0.getBlock() == Blocks.LADDER && var0.getValue(LadderBlock.FACING) == param1.getValue(TrapDoorBlock.FACING)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isAlive() {
        return !this.removed && this.getHealth() > 0.0F;
    }

    @Override
    public boolean causeFallDamage(float param0, float param1) {
        boolean var0 = super.causeFallDamage(param0, param1);
        int var1 = this.calculateFallDamage(param0, param1);
        if (var1 > 0) {
            this.playSound(this.getFallDamageSound(var1), 1.0F, 1.0F);
            this.playBlockFallSound();
            this.hurt(DamageSource.FALL, (float)var1);
            return true;
        } else {
            return var0;
        }
    }

    protected int calculateFallDamage(float param0, float param1) {
        MobEffectInstance var0 = this.getEffect(MobEffects.JUMP);
        float var1 = var0 == null ? 0.0F : (float)(var0.getAmplifier() + 1);
        return Mth.ceil((param0 - 3.0F - var1) * param1);
    }

    protected void playBlockFallSound() {
        if (!this.isSilent()) {
            int var0 = Mth.floor(this.getX());
            int var1 = Mth.floor(this.getY() - 0.2F);
            int var2 = Mth.floor(this.getZ());
            BlockState var3 = this.level.getBlockState(new BlockPos(var0, var1, var2));
            if (!var3.isAir()) {
                SoundType var4 = var3.getSoundType();
                this.playSound(var4.getFallSound(), var4.getVolume() * 0.5F, var4.getPitch() * 0.75F);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateHurt() {
        this.hurtDuration = 10;
        this.hurtTime = this.hurtDuration;
        this.hurtDir = 0.0F;
    }

    public int getArmorValue() {
        AttributeInstance var0 = this.getAttribute(SharedMonsterAttributes.ARMOR);
        return Mth.floor(var0.getValue());
    }

    protected void hurtArmor(float param0) {
    }

    protected void hurtCurrentlyUsedShield(float param0) {
    }

    protected float getDamageAfterArmorAbsorb(DamageSource param0, float param1) {
        if (!param0.isBypassArmor()) {
            this.hurtArmor(param1);
            param1 = CombatRules.getDamageAfterAbsorb(
                param1, (float)this.getArmorValue(), (float)this.getAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getValue()
            );
        }

        return param1;
    }

    protected float getDamageAfterMagicAbsorb(DamageSource param0, float param1) {
        if (param0.isBypassMagic()) {
            return param1;
        } else {
            if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && param0 != DamageSource.OUT_OF_WORLD) {
                int var0 = (this.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                int var1 = 25 - var0;
                float var2 = param1 * (float)var1;
                float var3 = param1;
                param1 = Math.max(var2 / 25.0F, 0.0F);
                float var4 = var3 - param1;
                if (var4 > 0.0F && var4 < 3.4028235E37F) {
                    if (this instanceof ServerPlayer) {
                        ((ServerPlayer)this).awardStat(Stats.DAMAGE_RESISTED, Math.round(var4 * 10.0F));
                    } else if (param0.getEntity() instanceof ServerPlayer) {
                        ((ServerPlayer)param0.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(var4 * 10.0F));
                    }
                }
            }

            if (param1 <= 0.0F) {
                return 0.0F;
            } else {
                int var5 = EnchantmentHelper.getDamageProtection(this.getArmorSlots(), param0);
                if (var5 > 0) {
                    param1 = CombatRules.getDamageAfterMagicAbsorb(param1, (float)var5);
                }

                return param1;
            }
        }
    }

    protected void actuallyHurt(DamageSource param0, float param1) {
        if (!this.isInvulnerableTo(param0)) {
            param1 = this.getDamageAfterArmorAbsorb(param0, param1);
            param1 = this.getDamageAfterMagicAbsorb(param0, param1);
            float var8 = Math.max(param1 - this.getAbsorptionAmount(), 0.0F);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (param1 - var8));
            float var1 = param1 - var8;
            if (var1 > 0.0F && var1 < 3.4028235E37F && param0.getEntity() instanceof ServerPlayer) {
                ((ServerPlayer)param0.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(var1 * 10.0F));
            }

            if (var8 != 0.0F) {
                float var2 = this.getHealth();
                this.setHealth(var2 - var8);
                this.getCombatTracker().recordDamage(param0, var2, var8);
                this.setAbsorptionAmount(this.getAbsorptionAmount() - var8);
            }
        }
    }

    public CombatTracker getCombatTracker() {
        return this.combatTracker;
    }

    @Nullable
    public LivingEntity getKillCredit() {
        if (this.combatTracker.getKiller() != null) {
            return this.combatTracker.getKiller();
        } else if (this.lastHurtByPlayer != null) {
            return this.lastHurtByPlayer;
        } else {
            return this.lastHurtByMob != null ? this.lastHurtByMob : null;
        }
    }

    public final float getMaxHealth() {
        return (float)this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getValue();
    }

    public final int getArrowCount() {
        return this.entityData.get(DATA_ARROW_COUNT_ID);
    }

    public final void setArrowCount(int param0) {
        this.entityData.set(DATA_ARROW_COUNT_ID, param0);
    }

    public final int getStingerCount() {
        return this.entityData.get(DATA_STINGER_COUNT_ID);
    }

    public final void setStingerCount(int param0) {
        this.entityData.set(DATA_STINGER_COUNT_ID, param0);
    }

    private int getCurrentSwingDuration() {
        if (MobEffectUtil.hasDigSpeed(this)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(this));
        } else {
            return this.hasEffect(MobEffects.DIG_SLOWDOWN) ? 6 + (1 + this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
        }
    }

    public void swing(InteractionHand param0) {
        this.swing(param0, false);
    }

    public void swing(InteractionHand param0, boolean param1) {
        if (!this.swinging || this.swingTime >= this.getCurrentSwingDuration() / 2 || this.swingTime < 0) {
            this.swingTime = -1;
            this.swinging = true;
            this.swingingArm = param0;
            if (this.level instanceof ServerLevel) {
                ClientboundAnimatePacket var0 = new ClientboundAnimatePacket(this, param0 == InteractionHand.MAIN_HAND ? 0 : 3);
                ServerChunkCache var1 = ((ServerLevel)this.level).getChunkSource();
                if (param1) {
                    var1.broadcastAndSend(this, var0);
                } else {
                    var1.broadcast(this, var0);
                }
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        switch(param0) {
            case 2:
            case 33:
            case 36:
            case 37:
            case 44:
                boolean var0 = param0 == 33;
                boolean var1 = param0 == 36;
                boolean var2 = param0 == 37;
                boolean var3 = param0 == 44;
                this.animationSpeed = 1.5F;
                this.invulnerableTime = 20;
                this.hurtDuration = 10;
                this.hurtTime = this.hurtDuration;
                this.hurtDir = 0.0F;
                if (var0) {
                    this.playSound(SoundEvents.THORNS_HIT, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                }

                DamageSource var4;
                if (var2) {
                    var4 = DamageSource.ON_FIRE;
                } else if (var1) {
                    var4 = DamageSource.DROWN;
                } else if (var3) {
                    var4 = DamageSource.SWEET_BERRY_BUSH;
                } else {
                    var4 = DamageSource.GENERIC;
                }

                SoundEvent var8 = this.getHurtSound(var4);
                if (var8 != null) {
                    this.playSound(var8, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                }

                this.hurt(DamageSource.GENERIC, 0.0F);
                break;
            case 3:
                SoundEvent var9 = this.getDeathSound();
                if (var9 != null) {
                    this.playSound(var9, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                }

                if (!(this instanceof Player)) {
                    this.setHealth(0.0F);
                    this.die(DamageSource.GENERIC);
                }
                break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 31:
            case 32:
            case 34:
            case 35:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 45:
            case 53:
            default:
                super.handleEntityEvent(param0);
                break;
            case 29:
                this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + this.level.random.nextFloat() * 0.4F);
                break;
            case 30:
                this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F);
                break;
            case 46:
                int var10 = 128;

                for(int var11 = 0; var11 < 128; ++var11) {
                    double var12 = (double)var11 / 127.0;
                    float var13 = (this.random.nextFloat() - 0.5F) * 0.2F;
                    float var14 = (this.random.nextFloat() - 0.5F) * 0.2F;
                    float var15 = (this.random.nextFloat() - 0.5F) * 0.2F;
                    double var16 = Mth.lerp(var12, this.xo, this.getX()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
                    double var17 = Mth.lerp(var12, this.yo, this.getY()) + this.random.nextDouble() * (double)this.getBbHeight();
                    double var18 = Mth.lerp(var12, this.zo, this.getZ()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
                    this.level.addParticle(ParticleTypes.PORTAL, var16, var17, var18, (double)var13, (double)var14, (double)var15);
                }
                break;
            case 47:
                this.breakItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
                break;
            case 48:
                this.breakItem(this.getItemBySlot(EquipmentSlot.OFFHAND));
                break;
            case 49:
                this.breakItem(this.getItemBySlot(EquipmentSlot.HEAD));
                break;
            case 50:
                this.breakItem(this.getItemBySlot(EquipmentSlot.CHEST));
                break;
            case 51:
                this.breakItem(this.getItemBySlot(EquipmentSlot.LEGS));
                break;
            case 52:
                this.breakItem(this.getItemBySlot(EquipmentSlot.FEET));
                break;
            case 54:
                HoneyBlock.showJumpParticles(this);
        }

    }

    @Override
    protected void outOfWorld() {
        this.hurt(DamageSource.OUT_OF_WORLD, 4.0F);
    }

    protected void updateSwingTime() {
        int var0 = this.getCurrentSwingDuration();
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= var0) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else {
            this.swingTime = 0;
        }

        this.attackAnim = (float)this.swingTime / (float)var0;
    }

    public AttributeInstance getAttribute(Attribute param0) {
        return this.getAttributes().getInstance(param0);
    }

    public BaseAttributeMap getAttributes() {
        if (this.attributes == null) {
            this.attributes = new ModifiableAttributeMap();
        }

        return this.attributes;
    }

    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    public ItemStack getMainHandItem() {
        return this.getItemBySlot(EquipmentSlot.MAINHAND);
    }

    public ItemStack getOffhandItem() {
        return this.getItemBySlot(EquipmentSlot.OFFHAND);
    }

    public ItemStack getItemInHand(InteractionHand param0) {
        if (param0 == InteractionHand.MAIN_HAND) {
            return this.getItemBySlot(EquipmentSlot.MAINHAND);
        } else if (param0 == InteractionHand.OFF_HAND) {
            return this.getItemBySlot(EquipmentSlot.OFFHAND);
        } else {
            throw new IllegalArgumentException("Invalid hand " + param0);
        }
    }

    public void setItemInHand(InteractionHand param0, ItemStack param1) {
        if (param0 == InteractionHand.MAIN_HAND) {
            this.setItemSlot(EquipmentSlot.MAINHAND, param1);
        } else {
            if (param0 != InteractionHand.OFF_HAND) {
                throw new IllegalArgumentException("Invalid hand " + param0);
            }

            this.setItemSlot(EquipmentSlot.OFFHAND, param1);
        }

    }

    public boolean hasItemInSlot(EquipmentSlot param0) {
        return !this.getItemBySlot(param0).isEmpty();
    }

    @Override
    public abstract Iterable<ItemStack> getArmorSlots();

    public abstract ItemStack getItemBySlot(EquipmentSlot var1);

    @Override
    public abstract void setItemSlot(EquipmentSlot var1, ItemStack var2);

    public float getArmorCoverPercentage() {
        Iterable<ItemStack> var0 = this.getArmorSlots();
        int var1 = 0;
        int var2 = 0;

        for(ItemStack var3 : var0) {
            if (!var3.isEmpty()) {
                ++var2;
            }

            ++var1;
        }

        return var1 > 0 ? (float)var2 / (float)var1 : 0.0F;
    }

    @Override
    public void setSprinting(boolean param0) {
        super.setSprinting(param0);
        AttributeInstance var0 = this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (var0.getModifier(SPEED_MODIFIER_SPRINTING_UUID) != null) {
            var0.removeModifier(SPEED_MODIFIER_SPRINTING);
        }

        if (param0) {
            var0.addModifier(SPEED_MODIFIER_SPRINTING);
        }

    }

    protected float getSoundVolume() {
        return 1.0F;
    }

    protected float getVoicePitch() {
        return this.isBaby()
            ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F
            : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
    }

    protected boolean isImmobile() {
        return this.getHealth() <= 0.0F;
    }

    @Override
    public void push(Entity param0) {
        if (!this.isSleeping()) {
            super.push(param0);
        }

    }

    private void findStandUpPosition(Entity param0) {
        if (this.level.getBlockState(new BlockPos(param0)).getBlock().is(BlockTags.PORTALS)) {
            this.setPos(param0.getX(), param0.getY(1.0) + 0.001, param0.getZ());
        } else if (!(param0 instanceof Boat) && !(param0 instanceof AbstractHorse)) {
            double var19 = param0.getX();
            double var20 = param0.getY(1.0);
            double var21 = param0.getZ();
            Direction var22 = param0.getMotionDirection();
            if (var22 != null && var22.getAxis() != Direction.Axis.Y) {
                Direction var23 = var22.getClockWise();
                int[][] var24 = new int[][]{{0, 1}, {0, -1}, {-1, 1}, {-1, -1}, {1, 1}, {1, -1}, {-1, 0}, {1, 0}, {0, 1}};
                double var25 = Math.floor(this.getX()) + 0.5;
                double var26 = Math.floor(this.getZ()) + 0.5;
                double var27 = this.getBoundingBox().maxX - this.getBoundingBox().minX;
                double var28 = this.getBoundingBox().maxZ - this.getBoundingBox().minZ;
                AABB var29 = new AABB(
                    var25 - var27 / 2.0,
                    param0.getBoundingBox().minY,
                    var26 - var28 / 2.0,
                    var25 + var27 / 2.0,
                    Math.floor(param0.getBoundingBox().minY) + (double)this.getBbHeight(),
                    var26 + var28 / 2.0
                );

                for(int[] var30 : var24) {
                    double var31 = (double)(var22.getStepX() * var30[0] + var23.getStepX() * var30[1]);
                    double var32 = (double)(var22.getStepZ() * var30[0] + var23.getStepZ() * var30[1]);
                    double var33 = var25 + var31;
                    double var34 = var26 + var32;
                    AABB var35 = var29.move(var31, 0.0, var32);
                    if (this.level.noCollision(this, var35)) {
                        BlockPos var36 = new BlockPos(var33, this.getY(), var34);
                        if (this.level.getBlockState(var36).entityCanStandOn(this.level, var36, this)) {
                            this.teleportTo(var33, this.getY() + 1.0, var34);
                            return;
                        }

                        BlockPos var37 = new BlockPos(var33, this.getY() - 1.0, var34);
                        if (this.level.getBlockState(var37).entityCanStandOn(this.level, var37, this) || this.level.getFluidState(var37).is(FluidTags.WATER)) {
                            var19 = var33;
                            var20 = this.getY() + 1.0;
                            var21 = var34;
                        }
                    } else {
                        BlockPos var38 = new BlockPos(var33, this.getY() + 1.0, var34);
                        if (this.level.noCollision(this, var35.move(0.0, 1.0, 0.0))
                            && this.level.getBlockState(var38).entityCanStandOn(this.level, var38, this)) {
                            var19 = var33;
                            var20 = this.getY() + 2.0;
                            var21 = var34;
                        }
                    }
                }
            }

            this.teleportTo(var19, var20, var21);
        } else {
            double var0 = (double)(this.getBbWidth() / 2.0F + param0.getBbWidth() / 2.0F) + 0.4;
            AABB var1 = param0.getBoundingBox();
            float var4;
            double var2;
            int var3;
            if (param0 instanceof Boat) {
                var2 = var1.maxY;
                var3 = 2;
                var4 = 0.0F;
            } else {
                var2 = var1.minY;
                var3 = 3;
                var4 = (float) (Math.PI / 2) * (float)(this.getMainArm() == HumanoidArm.RIGHT ? -1 : 1);
            }

            float var8 = -this.yRot * (float) (Math.PI / 180.0) - (float) Math.PI + var4;
            float var9 = -Mth.sin(var8);
            float var10 = -Mth.cos(var8);
            double var11 = Math.abs(var9) > Math.abs(var10) ? var0 / (double)Math.abs(var9) : var0 / (double)Math.abs(var10);
            AABB var12 = this.getBoundingBox().move(-this.getX(), -this.getY(), -this.getZ());
            ImmutableSet<Entity> var13 = ImmutableSet.of(this, param0);
            double var14 = this.getX() + (double)var9 * var11;
            double var15 = this.getZ() + (double)var10 * var11;
            double var16 = 0.001;

            for(int var17 = 0; var17 < var3; ++var17) {
                double var18 = var2 + var16;
                if (this.level.noCollision(this, var12.move(var14, var18, var15), var13)) {
                    this.setPos(var14, var18, var15);
                    return;
                }

                ++var16;
            }

            this.setPos(param0.getX(), param0.getY(1.0) + 0.001, param0.getZ());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldShowName() {
        return this.isCustomNameVisible();
    }

    protected float getJumpPower() {
        return 0.42F * this.getBlockJumpFactor();
    }

    protected void jumpFromGround() {
        float var0 = this.getJumpPower();
        if (this.hasEffect(MobEffects.JUMP)) {
            var0 += 0.1F * (float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1);
        }

        Vec3 var1 = this.getDeltaMovement();
        this.setDeltaMovement(var1.x, (double)var0, var1.z);
        if (this.isSprinting()) {
            float var2 = this.yRot * (float) (Math.PI / 180.0);
            this.setDeltaMovement(this.getDeltaMovement().add((double)(-Mth.sin(var2) * 0.2F), 0.0, (double)(Mth.cos(var2) * 0.2F)));
        }

        this.hasImpulse = true;
    }

    @OnlyIn(Dist.CLIENT)
    protected void goDownInWater() {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04F, 0.0));
    }

    protected void jumpInLiquid(Tag<Fluid> param0) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04F, 0.0));
    }

    protected float getWaterSlowDown() {
        return 0.8F;
    }

    public void travel(Vec3 param0) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            double var0 = 0.08;
            boolean var1 = this.getDeltaMovement().y <= 0.0;
            if (var1 && this.hasEffect(MobEffects.SLOW_FALLING)) {
                var0 = 0.01;
                this.fallDistance = 0.0F;
            }

            if (!this.isInWater() || this instanceof Player && ((Player)this).abilities.flying) {
                if (!this.isInLava() || this instanceof Player && ((Player)this).abilities.flying) {
                    if (this.isFallFlying()) {
                        Vec3 var13 = this.getDeltaMovement();
                        if (var13.y > -0.5) {
                            this.fallDistance = 1.0F;
                        }

                        Vec3 var14 = this.getLookAngle();
                        float var15 = this.xRot * (float) (Math.PI / 180.0);
                        double var16 = Math.sqrt(var14.x * var14.x + var14.z * var14.z);
                        double var17 = Math.sqrt(getHorizontalDistanceSqr(var13));
                        double var18 = var14.length();
                        float var19 = Mth.cos(var15);
                        var19 = (float)((double)var19 * (double)var19 * Math.min(1.0, var18 / 0.4));
                        var13 = this.getDeltaMovement().add(0.0, var0 * (-1.0 + (double)var19 * 0.75), 0.0);
                        if (var13.y < 0.0 && var16 > 0.0) {
                            double var20 = var13.y * -0.1 * (double)var19;
                            var13 = var13.add(var14.x * var20 / var16, var20, var14.z * var20 / var16);
                        }

                        if (var15 < 0.0F && var16 > 0.0) {
                            double var21 = var17 * (double)(-Mth.sin(var15)) * 0.04;
                            var13 = var13.add(-var14.x * var21 / var16, var21 * 3.2, -var14.z * var21 / var16);
                        }

                        if (var16 > 0.0) {
                            var13 = var13.add((var14.x / var16 * var17 - var13.x) * 0.1, 0.0, (var14.z / var16 * var17 - var13.z) * 0.1);
                        }

                        this.setDeltaMovement(var13.multiply(0.99F, 0.98F, 0.99F));
                        this.move(MoverType.SELF, this.getDeltaMovement());
                        if (this.horizontalCollision && !this.level.isClientSide) {
                            double var22 = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
                            double var23 = var17 - var22;
                            float var24 = (float)(var23 * 10.0 - 3.0);
                            if (var24 > 0.0F) {
                                this.playSound(this.getFallDamageSound((int)var24), 1.0F, 1.0F);
                                this.hurt(DamageSource.FLY_INTO_WALL, var24);
                            }
                        }

                        if (this.onGround && !this.level.isClientSide) {
                            this.setSharedFlag(7, false);
                        }
                    } else {
                        BlockPos var25 = this.getBlockPosBelowThatAffectsMyMovement();
                        float var26 = this.level.getBlockState(var25).getBlock().getFriction();
                        float var27 = this.onGround ? var26 * 0.91F : 0.91F;
                        this.moveRelative(this.getFrictionInfluencedSpeed(var26), param0);
                        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
                        this.move(MoverType.SELF, this.getDeltaMovement());
                        Vec3 var28 = this.getDeltaMovement();
                        if ((this.horizontalCollision || this.jumping) && this.onLadder()) {
                            var28 = new Vec3(var28.x, 0.2, var28.z);
                        }

                        double var29 = var28.y;
                        if (this.hasEffect(MobEffects.LEVITATION)) {
                            var29 += (0.05 * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - var28.y) * 0.2;
                            this.fallDistance = 0.0F;
                        } else if (this.level.isClientSide && !this.level.hasChunkAt(var25)) {
                            if (this.getY() > 0.0) {
                                var29 = -0.1;
                            } else {
                                var29 = 0.0;
                            }
                        } else if (!this.isNoGravity()) {
                            var29 -= var0;
                        }

                        this.setDeltaMovement(var28.x * (double)var27, var29 * 0.98F, var28.z * (double)var27);
                    }
                } else {
                    double var11 = this.getY();
                    this.moveRelative(0.02F, param0);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
                    if (!this.isNoGravity()) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -var0 / 4.0, 0.0));
                    }

                    Vec3 var12 = this.getDeltaMovement();
                    if (this.horizontalCollision && this.isFree(var12.x, var12.y + 0.6F - this.getY() + var11, var12.z)) {
                        this.setDeltaMovement(var12.x, 0.3F, var12.z);
                    }
                }
            } else {
                double var2 = this.getY();
                float var3 = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
                float var4 = 0.02F;
                float var5 = (float)EnchantmentHelper.getDepthStrider(this);
                if (var5 > 3.0F) {
                    var5 = 3.0F;
                }

                if (!this.onGround) {
                    var5 *= 0.5F;
                }

                if (var5 > 0.0F) {
                    var3 += (0.54600006F - var3) * var5 / 3.0F;
                    var4 += (this.getSpeed() - var4) * var5 / 3.0F;
                }

                if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                    var3 = 0.96F;
                }

                this.moveRelative(var4, param0);
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vec3 var6 = this.getDeltaMovement();
                if (this.horizontalCollision && this.onLadder()) {
                    var6 = new Vec3(var6.x, 0.2, var6.z);
                }

                this.setDeltaMovement(var6.multiply((double)var3, 0.8F, (double)var3));
                if (!this.isNoGravity() && !this.isSprinting()) {
                    Vec3 var7 = this.getDeltaMovement();
                    double var8;
                    if (var1 && Math.abs(var7.y - 0.005) >= 0.003 && Math.abs(var7.y - var0 / 16.0) < 0.003) {
                        var8 = -0.003;
                    } else {
                        var8 = var7.y - var0 / 16.0;
                    }

                    this.setDeltaMovement(var7.x, var8, var7.z);
                }

                Vec3 var10 = this.getDeltaMovement();
                if (this.horizontalCollision && this.isFree(var10.x, var10.y + 0.6F - this.getY() + var2, var10.z)) {
                    this.setDeltaMovement(var10.x, 0.3F, var10.z);
                }
            }
        }

        this.animationSpeedOld = this.animationSpeed;
        double var30 = this.getX() - this.xo;
        double var31 = this.getZ() - this.zo;
        double var32 = this instanceof FlyingAnimal ? this.getY() - this.yo : 0.0;
        float var33 = Mth.sqrt(var30 * var30 + var32 * var32 + var31 * var31) * 4.0F;
        if (var33 > 1.0F) {
            var33 = 1.0F;
        }

        this.animationSpeed += (var33 - this.animationSpeed) * 0.4F;
        this.animationPosition += this.animationSpeed;
    }

    private Vec3 handleOnClimbable(Vec3 param0) {
        if (this.onLadder()) {
            this.fallDistance = 0.0F;
            float var0 = 0.15F;
            double var1 = Mth.clamp(param0.x, -0.15F, 0.15F);
            double var2 = Mth.clamp(param0.z, -0.15F, 0.15F);
            double var3 = Math.max(param0.y, -0.15F);
            if (var3 < 0.0 && this.getFeetBlockState().getBlock() != Blocks.SCAFFOLDING && this.isSuppressingSlidingDownLadder() && this instanceof Player) {
                var3 = 0.0;
            }

            param0 = new Vec3(var1, var3, var2);
        }

        return param0;
    }

    private float getFrictionInfluencedSpeed(float param0) {
        return this.onGround ? this.getSpeed() * (0.21600002F / (param0 * param0 * param0)) : this.flyingSpeed;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float param0) {
        this.speed = param0;
    }

    public boolean doHurtTarget(Entity param0) {
        this.setLastHurtMob(param0);
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.updatingUsingItem();
        this.updateSwimAmount();
        if (!this.level.isClientSide) {
            int var0 = this.getArrowCount();
            if (var0 > 0) {
                if (this.removeArrowTime <= 0) {
                    this.removeArrowTime = 20 * (30 - var0);
                }

                --this.removeArrowTime;
                if (this.removeArrowTime <= 0) {
                    this.setArrowCount(var0 - 1);
                }
            }

            int var1 = this.getStingerCount();
            if (var1 > 0) {
                if (this.removeStingerTime <= 0) {
                    this.removeStingerTime = 20 * (30 - var1);
                }

                --this.removeStingerTime;
                if (this.removeStingerTime <= 0) {
                    this.setStingerCount(var1 - 1);
                }
            }

            for(EquipmentSlot var2 : EquipmentSlot.values()) {
                ItemStack var3;
                switch(var2.getType()) {
                    case HAND:
                        var3 = this.lastHandItemStacks.get(var2.getIndex());
                        break;
                    case ARMOR:
                        var3 = this.lastArmorItemStacks.get(var2.getIndex());
                        break;
                    default:
                        continue;
                }

                ItemStack var6 = this.getItemBySlot(var2);
                if (!ItemStack.matches(var6, var3)) {
                    ((ServerLevel)this.level).getChunkSource().broadcast(this, new ClientboundSetEquippedItemPacket(this.getId(), var2, var6));
                    if (!var3.isEmpty()) {
                        this.getAttributes().removeAttributeModifiers(var3.getAttributeModifiers(var2));
                    }

                    if (!var6.isEmpty()) {
                        this.getAttributes().addAttributeModifiers(var6.getAttributeModifiers(var2));
                    }

                    switch(var2.getType()) {
                        case HAND:
                            this.lastHandItemStacks.set(var2.getIndex(), var6.copy());
                            break;
                        case ARMOR:
                            this.lastArmorItemStacks.set(var2.getIndex(), var6.copy());
                    }
                }
            }

            if (this.tickCount % 20 == 0) {
                this.getCombatTracker().recheckStatus();
            }

            if (!this.glowing) {
                boolean var7 = this.hasEffect(MobEffects.GLOWING);
                if (this.getSharedFlag(6) != var7) {
                    this.setSharedFlag(6, var7);
                }
            }

            if (this.isSleeping() && !this.checkBedExists()) {
                this.stopSleeping();
            }
        }

        this.aiStep();
        double var8 = this.getX() - this.xo;
        double var9 = this.getZ() - this.zo;
        float var10 = (float)(var8 * var8 + var9 * var9);
        float var11 = this.yBodyRot;
        float var12 = 0.0F;
        this.oRun = this.run;
        float var13 = 0.0F;
        if (var10 > 0.0025000002F) {
            var13 = 1.0F;
            var12 = (float)Math.sqrt((double)var10) * 3.0F;
            float var14 = (float)Mth.atan2(var9, var8) * (180.0F / (float)Math.PI) - 90.0F;
            float var15 = Mth.abs(Mth.wrapDegrees(this.yRot) - var14);
            if (95.0F < var15 && var15 < 265.0F) {
                var11 = var14 - 180.0F;
            } else {
                var11 = var14;
            }
        }

        if (this.attackAnim > 0.0F) {
            var11 = this.yRot;
        }

        if (!this.onGround) {
            var13 = 0.0F;
        }

        this.run += (var13 - this.run) * 0.3F;
        this.level.getProfiler().push("headTurn");
        var12 = this.tickHeadTurn(var11, var12);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("rangeChecks");

        while(this.yRot - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while(this.yRot - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        while(this.yBodyRot - this.yBodyRotO < -180.0F) {
            this.yBodyRotO -= 360.0F;
        }

        while(this.yBodyRot - this.yBodyRotO >= 180.0F) {
            this.yBodyRotO += 360.0F;
        }

        while(this.xRot - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }

        while(this.xRot - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while(this.yHeadRot - this.yHeadRotO < -180.0F) {
            this.yHeadRotO -= 360.0F;
        }

        while(this.yHeadRot - this.yHeadRotO >= 180.0F) {
            this.yHeadRotO += 360.0F;
        }

        this.level.getProfiler().pop();
        this.animStep += var12;
        if (this.isFallFlying()) {
            ++this.fallFlyTicks;
        } else {
            this.fallFlyTicks = 0;
        }

        if (this.isSleeping()) {
            this.xRot = 0.0F;
        }

    }

    protected float tickHeadTurn(float param0, float param1) {
        float var0 = Mth.wrapDegrees(param0 - this.yBodyRot);
        this.yBodyRot += var0 * 0.3F;
        float var1 = Mth.wrapDegrees(this.yRot - this.yBodyRot);
        boolean var2 = var1 < -90.0F || var1 >= 90.0F;
        if (var1 < -75.0F) {
            var1 = -75.0F;
        }

        if (var1 >= 75.0F) {
            var1 = 75.0F;
        }

        this.yBodyRot = this.yRot - var1;
        if (var1 * var1 > 2500.0F) {
            this.yBodyRot += var1 * 0.2F;
        }

        if (var2) {
            param1 *= -1.0F;
        }

        return param1;
    }

    public void aiStep() {
        if (this.noJumpDelay > 0) {
            --this.noJumpDelay;
        }

        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.setPacketCoordinates(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpSteps > 0) {
            double var0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
            double var1 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
            double var2 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
            double var3 = Mth.wrapDegrees(this.lerpYRot - (double)this.yRot);
            this.yRot = (float)((double)this.yRot + var3 / (double)this.lerpSteps);
            this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
            --this.lerpSteps;
            this.setPos(var0, var1, var2);
            this.setRot(this.yRot, this.xRot);
        } else if (!this.isEffectiveAi()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }

        if (this.lerpHeadSteps > 0) {
            this.yHeadRot = (float)((double)this.yHeadRot + Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
            --this.lerpHeadSteps;
        }

        Vec3 var4 = this.getDeltaMovement();
        double var5 = var4.x;
        double var6 = var4.y;
        double var7 = var4.z;
        if (Math.abs(var4.x) < 0.003) {
            var5 = 0.0;
        }

        if (Math.abs(var4.y) < 0.003) {
            var6 = 0.0;
        }

        if (Math.abs(var4.z) < 0.003) {
            var7 = 0.0;
        }

        this.setDeltaMovement(var5, var6, var7);
        this.level.getProfiler().push("ai");
        if (this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        } else if (this.isEffectiveAi()) {
            this.level.getProfiler().push("newAi");
            this.serverAiStep();
            this.level.getProfiler().pop();
        }

        this.level.getProfiler().pop();
        this.level.getProfiler().push("jump");
        if (this.jumping) {
            if (!(this.waterHeight > 0.0) || this.onGround && !(this.waterHeight > 0.4)) {
                if (this.isInLava()) {
                    this.jumpInLiquid(FluidTags.LAVA);
                } else if ((this.onGround || this.waterHeight > 0.0 && this.waterHeight <= 0.4) && this.noJumpDelay == 0) {
                    this.jumpFromGround();
                    this.noJumpDelay = 10;
                }
            } else {
                this.jumpInLiquid(FluidTags.WATER);
            }
        } else {
            this.noJumpDelay = 0;
        }

        this.level.getProfiler().pop();
        this.level.getProfiler().push("travel");
        this.xxa *= 0.98F;
        this.zza *= 0.98F;
        this.updateFallFlying();
        AABB var8 = this.getBoundingBox();
        this.travel(new Vec3((double)this.xxa, (double)this.yya, (double)this.zza));
        this.level.getProfiler().pop();
        this.level.getProfiler().push("push");
        if (this.autoSpinAttackTicks > 0) {
            --this.autoSpinAttackTicks;
            this.checkAutoSpinAttack(var8, this.getBoundingBox());
        }

        this.pushEntities();
        this.level.getProfiler().pop();
    }

    private void updateFallFlying() {
        boolean var0 = this.getSharedFlag(7);
        if (var0 && !this.onGround && !this.isPassenger()) {
            ItemStack var1 = this.getItemBySlot(EquipmentSlot.CHEST);
            if (var1.getItem() == Items.ELYTRA && ElytraItem.isFlyEnabled(var1)) {
                var0 = true;
                if (!this.level.isClientSide && (this.fallFlyTicks + 1) % 20 == 0) {
                    var1.hurtAndBreak(1, this, param0 -> param0.broadcastBreakEvent(EquipmentSlot.CHEST));
                }
            } else {
                var0 = false;
            }
        } else {
            var0 = false;
        }

        if (!this.level.isClientSide) {
            this.setSharedFlag(7, var0);
        }

    }

    protected void serverAiStep() {
    }

    protected void pushEntities() {
        List<Entity> var0 = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this));
        if (!var0.isEmpty()) {
            int var1 = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (var1 > 0 && var0.size() > var1 - 1 && this.random.nextInt(4) == 0) {
                int var2 = 0;

                for(int var3 = 0; var3 < var0.size(); ++var3) {
                    if (!var0.get(var3).isPassenger()) {
                        ++var2;
                    }
                }

                if (var2 > var1 - 1) {
                    this.hurt(DamageSource.CRAMMING, 6.0F);
                }
            }

            for(int var4 = 0; var4 < var0.size(); ++var4) {
                Entity var5 = var0.get(var4);
                this.doPush(var5);
            }
        }

    }

    protected void checkAutoSpinAttack(AABB param0, AABB param1) {
        AABB var0 = param0.minmax(param1);
        List<Entity> var1 = this.level.getEntities(this, var0);
        if (!var1.isEmpty()) {
            for(int var2 = 0; var2 < var1.size(); ++var2) {
                Entity var3 = var1.get(var2);
                if (var3 instanceof LivingEntity) {
                    this.doAutoAttackOnTouch((LivingEntity)var3);
                    this.autoSpinAttackTicks = 0;
                    this.setDeltaMovement(this.getDeltaMovement().scale(-0.2));
                    break;
                }
            }
        } else if (this.horizontalCollision) {
            this.autoSpinAttackTicks = 0;
        }

        if (!this.level.isClientSide && this.autoSpinAttackTicks <= 0) {
            this.setLivingEntityFlag(4, false);
        }

    }

    protected void doPush(Entity param0) {
        param0.push(this);
    }

    protected void doAutoAttackOnTouch(LivingEntity param0) {
    }

    public void startAutoSpinAttack(int param0) {
        this.autoSpinAttackTicks = param0;
        if (!this.level.isClientSide) {
            this.setLivingEntityFlag(4, true);
        }

    }

    public boolean isAutoSpinAttack() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 4) != 0;
    }

    @Override
    public void stopRiding() {
        Entity var0 = this.getVehicle();
        super.stopRiding();
        if (var0 != null && var0 != this.getVehicle() && !this.level.isClientSide) {
            this.findStandUpPosition(var0);
        }

    }

    @Override
    public void rideTick() {
        super.rideTick();
        this.oRun = this.run;
        this.run = 0.0F;
        this.fallDistance = 0.0F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpTo(double param0, double param1, double param2, float param3, float param4, int param5, boolean param6) {
        this.lerpX = param0;
        this.lerpY = param1;
        this.lerpZ = param2;
        this.lerpYRot = (double)param3;
        this.lerpXRot = (double)param4;
        this.lerpSteps = param5;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpHeadTo(float param0, int param1) {
        this.lyHeadRot = (double)param0;
        this.lerpHeadSteps = param1;
    }

    public void setJumping(boolean param0) {
        this.jumping = param0;
    }

    public void take(Entity param0, int param1) {
        if (!param0.removed && !this.level.isClientSide && (param0 instanceof ItemEntity || param0 instanceof AbstractArrow || param0 instanceof ExperienceOrb)
            )
         {
            ((ServerLevel)this.level).getChunkSource().broadcast(param0, new ClientboundTakeItemEntityPacket(param0.getId(), this.getId(), param1));
        }

    }

    public boolean canSee(Entity param0) {
        Vec3 var0 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 var1 = new Vec3(param0.getX(), param0.getEyeY(), param0.getZ());
        return this.level.clip(new ClipContext(var0, var1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public float getViewYRot(float param0) {
        return param0 == 1.0F ? this.yHeadRot : Mth.lerp(param0, this.yHeadRotO, this.yHeadRot);
    }

    @OnlyIn(Dist.CLIENT)
    public float getAttackAnim(float param0) {
        float var0 = this.attackAnim - this.oAttackAnim;
        if (var0 < 0.0F) {
            ++var0;
        }

        return this.oAttackAnim + var0 * param0;
    }

    public boolean isEffectiveAi() {
        return !this.level.isClientSide;
    }

    @Override
    public boolean isPickable() {
        return !this.removed;
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.onLadder();
    }

    @Override
    protected void markHurt() {
        this.hurtMarked = this.random.nextDouble() >= this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getValue();
    }

    @Override
    public float getYHeadRot() {
        return this.yHeadRot;
    }

    @Override
    public void setYHeadRot(float param0) {
        this.yHeadRot = param0;
    }

    @Override
    public void setYBodyRot(float param0) {
        this.yBodyRot = param0;
    }

    public float getAbsorptionAmount() {
        return this.absorptionAmount;
    }

    public void setAbsorptionAmount(float param0) {
        if (param0 < 0.0F) {
            param0 = 0.0F;
        }

        this.absorptionAmount = param0;
    }

    public void onEnterCombat() {
    }

    public void onLeaveCombat() {
    }

    protected void updateEffectVisibility() {
        this.effectsDirty = true;
    }

    public abstract HumanoidArm getMainArm();

    public boolean isUsingItem() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
    }

    public InteractionHand getUsedItemHand() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private void updatingUsingItem() {
        if (this.isUsingItem()) {
            if (ItemStack.isSameIgnoreDurability(this.getItemInHand(this.getUsedItemHand()), this.useItem)) {
                this.useItem.onUseTick(this.level, this, this.getUseItemRemainingTicks());
                if (this.shouldTriggerItemUseEffects()) {
                    this.triggerItemUseEffects(this.useItem, 5);
                }

                if (--this.useItemRemaining == 0 && !this.level.isClientSide && !this.useItem.useOnRelease()) {
                    this.completeUsingItem();
                }
            } else {
                this.stopUsingItem();
            }
        }

    }

    private boolean shouldTriggerItemUseEffects() {
        int var0 = this.getUseItemRemainingTicks();
        FoodProperties var1 = this.useItem.getItem().getFoodProperties();
        boolean var2 = var1 != null && var1.isFastFood();
        var2 |= var0 <= this.useItem.getUseDuration() - 7;
        return var2 && var0 % 4 == 0;
    }

    private void updateSwimAmount() {
        this.swimAmountO = this.swimAmount;
        if (this.isVisuallySwimming()) {
            this.swimAmount = Math.min(1.0F, this.swimAmount + 0.09F);
        } else {
            this.swimAmount = Math.max(0.0F, this.swimAmount - 0.09F);
        }

    }

    protected void setLivingEntityFlag(int param0, boolean param1) {
        int var0 = this.entityData.get(DATA_LIVING_ENTITY_FLAGS);
        if (param1) {
            var0 |= param0;
        } else {
            var0 &= ~param0;
        }

        this.entityData.set(DATA_LIVING_ENTITY_FLAGS, (byte)var0);
    }

    public void startUsingItem(InteractionHand param0) {
        ItemStack var0 = this.getItemInHand(param0);
        if (!var0.isEmpty() && !this.isUsingItem()) {
            this.useItem = var0;
            this.useItemRemaining = var0.getUseDuration();
            if (!this.level.isClientSide) {
                this.setLivingEntityFlag(1, true);
                this.setLivingEntityFlag(2, param0 == InteractionHand.OFF_HAND);
            }

        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        super.onSyncedDataUpdated(param0);
        if (SLEEPING_POS_ID.equals(param0)) {
            if (this.level.isClientSide) {
                this.getSleepingPos().ifPresent(this::setPosToBed);
            }
        } else if (DATA_LIVING_ENTITY_FLAGS.equals(param0) && this.level.isClientSide) {
            if (this.isUsingItem() && this.useItem.isEmpty()) {
                this.useItem = this.getItemInHand(this.getUsedItemHand());
                if (!this.useItem.isEmpty()) {
                    this.useItemRemaining = this.useItem.getUseDuration();
                }
            } else if (!this.isUsingItem() && !this.useItem.isEmpty()) {
                this.useItem = ItemStack.EMPTY;
                this.useItemRemaining = 0;
            }
        }

    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor param0, Vec3 param1) {
        super.lookAt(param0, param1);
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRot = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot;
    }

    protected void triggerItemUseEffects(ItemStack param0, int param1) {
        if (!param0.isEmpty() && this.isUsingItem()) {
            if (param0.getUseAnimation() == UseAnim.DRINK) {
                this.playSound(this.getDrinkingSound(param0), 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
            }

            if (param0.getUseAnimation() == UseAnim.EAT) {
                this.spawnItemParticles(param0, param1);
                this.playSound(
                    this.getEatingSound(param0), 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F
                );
            }

        }
    }

    private void spawnItemParticles(ItemStack param0, int param1) {
        for(int var0 = 0; var0 < param1; ++var0) {
            Vec3 var1 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            var1 = var1.xRot(-this.xRot * (float) (Math.PI / 180.0));
            var1 = var1.yRot(-this.yRot * (float) (Math.PI / 180.0));
            double var2 = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3 var3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.3, var2, 0.6);
            var3 = var3.xRot(-this.xRot * (float) (Math.PI / 180.0));
            var3 = var3.yRot(-this.yRot * (float) (Math.PI / 180.0));
            var3 = var3.add(this.getX(), this.getEyeY(), this.getZ());
            this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, param0), var3.x, var3.y, var3.z, var1.x, var1.y + 0.05, var1.z);
        }

    }

    protected void completeUsingItem() {
        if (!this.useItem.equals(this.getItemInHand(this.getUsedItemHand()))) {
            this.releaseUsingItem();
        } else {
            if (!this.useItem.isEmpty() && this.isUsingItem()) {
                this.triggerItemUseEffects(this.useItem, 16);
                this.setItemInHand(this.getUsedItemHand(), this.useItem.finishUsingItem(this.level, this));
                this.stopUsingItem();
            }

        }
    }

    public ItemStack getUseItem() {
        return this.useItem;
    }

    public int getUseItemRemainingTicks() {
        return this.useItemRemaining;
    }

    public int getTicksUsingItem() {
        return this.isUsingItem() ? this.useItem.getUseDuration() - this.getUseItemRemainingTicks() : 0;
    }

    public void releaseUsingItem() {
        if (!this.useItem.isEmpty()) {
            this.useItem.releaseUsing(this.level, this, this.getUseItemRemainingTicks());
            if (this.useItem.useOnRelease()) {
                this.updatingUsingItem();
            }
        }

        this.stopUsingItem();
    }

    public void stopUsingItem() {
        if (!this.level.isClientSide) {
            this.setLivingEntityFlag(1, false);
        }

        this.useItem = ItemStack.EMPTY;
        this.useItemRemaining = 0;
    }

    public boolean isBlocking() {
        if (this.isUsingItem() && !this.useItem.isEmpty()) {
            Item var0 = this.useItem.getItem();
            if (var0.getUseAnimation(this.useItem) != UseAnim.BLOCK) {
                return false;
            } else {
                return var0.getUseDuration(this.useItem) - this.useItemRemaining >= 5;
            }
        } else {
            return false;
        }
    }

    public boolean isSuppressingSlidingDownLadder() {
        return this.isShiftKeyDown();
    }

    public boolean isFallFlying() {
        return this.getSharedFlag(7);
    }

    @Override
    public boolean isVisuallySwimming() {
        return super.isVisuallySwimming() || !this.isFallFlying() && this.getPose() == Pose.FALL_FLYING;
    }

    @OnlyIn(Dist.CLIENT)
    public int getFallFlyingTicks() {
        return this.fallFlyTicks;
    }

    public boolean randomTeleport(double param0, double param1, double param2, boolean param3) {
        double var0 = this.getX();
        double var1 = this.getY();
        double var2 = this.getZ();
        double var3 = param1;
        boolean var4 = false;
        BlockPos var5 = new BlockPos(param0, param1, param2);
        Level var6 = this.level;
        if (var6.hasChunkAt(var5)) {
            boolean var7 = false;

            while(!var7 && var5.getY() > 0) {
                BlockPos var8 = var5.below();
                BlockState var9 = var6.getBlockState(var8);
                if (var9.getMaterial().blocksMotion()) {
                    var7 = true;
                } else {
                    --var3;
                    var5 = var8;
                }
            }

            if (var7) {
                this.teleportTo(param0, var3, param2);
                if (var6.noCollision(this) && !var6.containsAnyLiquid(this.getBoundingBox())) {
                    var4 = true;
                }
            }
        }

        if (!var4) {
            this.teleportTo(var0, var1, var2);
            return false;
        } else {
            if (param3) {
                var6.broadcastEntityEvent(this, (byte)46);
            }

            if (this instanceof PathfinderMob) {
                ((PathfinderMob)this).getNavigation().stop();
            }

            return true;
        }
    }

    public boolean isAffectedByPotions() {
        return true;
    }

    public boolean attackable() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void setRecordPlayingNearby(BlockPos param0, boolean param1) {
    }

    public boolean canTakeItem(ItemStack param0) {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddMobPacket(this);
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        return param0 == Pose.SLEEPING ? SLEEPING_DIMENSIONS : super.getDimensions(param0).scale(this.getScale());
    }

    public Optional<BlockPos> getSleepingPos() {
        return this.entityData.get(SLEEPING_POS_ID);
    }

    public void setSleepingPos(BlockPos param0) {
        this.entityData.set(SLEEPING_POS_ID, Optional.of(param0));
    }

    public void clearSleepingPos() {
        this.entityData.set(SLEEPING_POS_ID, Optional.empty());
    }

    public boolean isSleeping() {
        return this.getSleepingPos().isPresent();
    }

    public void startSleeping(BlockPos param0) {
        if (this.isPassenger()) {
            this.stopRiding();
        }

        BlockState var0 = this.level.getBlockState(param0);
        if (var0.getBlock() instanceof BedBlock) {
            this.level.setBlock(param0, var0.setValue(BedBlock.OCCUPIED, Boolean.valueOf(true)), 3);
        }

        this.setPose(Pose.SLEEPING);
        this.setPosToBed(param0);
        this.setSleepingPos(param0);
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = true;
    }

    private void setPosToBed(BlockPos param0) {
        this.setPos((double)param0.getX() + 0.5, (double)((float)param0.getY() + 0.6875F), (double)param0.getZ() + 0.5);
    }

    private boolean checkBedExists() {
        return this.getSleepingPos().map(param0 -> this.level.getBlockState(param0).getBlock() instanceof BedBlock).orElse(false);
    }

    public void stopSleeping() {
        this.getSleepingPos().filter(this.level::hasChunkAt).ifPresent(param0 -> {
            BlockState var0 = this.level.getBlockState(param0);
            if (var0.getBlock() instanceof BedBlock) {
                this.level.setBlock(param0, var0.setValue(BedBlock.OCCUPIED, Boolean.valueOf(false)), 3);
                Vec3 var1 = BedBlock.findStandUpPosition(this.getType(), this.level, param0, 0).orElseGet(() -> {
                    BlockPos var0x = param0.above();
                    return new Vec3((double)var0x.getX() + 0.5, (double)var0x.getY() + 0.1, (double)var0x.getZ() + 0.5);
                });
                this.setPos(var1.x, var1.y, var1.z);
            }

        });
        this.setPose(Pose.STANDING);
        this.clearSleepingPos();
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Direction getBedOrientation() {
        BlockPos var0 = this.getSleepingPos().orElse(null);
        return var0 != null ? BedBlock.getBedOrientation(this.level, var0) : null;
    }

    @Override
    public boolean isInWall() {
        return !this.isSleeping() && super.isInWall();
    }

    @Override
    protected final float getEyeHeight(Pose param0, EntityDimensions param1) {
        return param0 == Pose.SLEEPING ? 0.2F : this.getStandingEyeHeight(param0, param1);
    }

    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return super.getEyeHeight(param0, param1);
    }

    public ItemStack getProjectile(ItemStack param0) {
        return ItemStack.EMPTY;
    }

    public ItemStack eat(Level param0, ItemStack param1) {
        if (param1.isEdible()) {
            param0.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                this.getEatingSound(param1),
                SoundSource.NEUTRAL,
                1.0F,
                1.0F + (param0.random.nextFloat() - param0.random.nextFloat()) * 0.4F
            );
            this.addEatEffect(param1, param0, this);
            if (!(this instanceof Player) || !((Player)this).abilities.instabuild) {
                param1.shrink(1);
            }
        }

        return param1;
    }

    private void addEatEffect(ItemStack param0, Level param1, LivingEntity param2) {
        Item var0 = param0.getItem();
        if (var0.isEdible()) {
            for(Pair<MobEffectInstance, Float> var2 : var0.getFoodProperties().getEffects()) {
                if (!param1.isClientSide && var2.getLeft() != null && param1.random.nextFloat() < var2.getRight()) {
                    param2.addEffect(new MobEffectInstance(var2.getLeft()));
                }
            }
        }

    }

    private static byte entityEventForEquipmentBreak(EquipmentSlot param0) {
        switch(param0) {
            case MAINHAND:
                return 47;
            case OFFHAND:
                return 48;
            case HEAD:
                return 49;
            case CHEST:
                return 50;
            case FEET:
                return 52;
            case LEGS:
                return 51;
            default:
                return 47;
        }
    }

    public void broadcastBreakEvent(EquipmentSlot param0) {
        this.level.broadcastEntityEvent(this, entityEventForEquipmentBreak(param0));
    }

    public void broadcastBreakEvent(InteractionHand param0) {
        this.broadcastBreakEvent(param0 == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }
}

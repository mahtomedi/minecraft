package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractArrow extends Entity implements Projectile {
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(
        AbstractArrow.class, EntityDataSerializers.OPTIONAL_UUID
    );
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
    @Nullable
    private BlockState lastState;
    protected boolean inGround;
    protected int inGroundTime;
    public AbstractArrow.Pickup pickup = AbstractArrow.Pickup.DISALLOWED;
    public int shakeTime;
    public UUID ownerUUID;
    private int life;
    private int flightTime;
    private double baseDamage = 2.0;
    private int knockback;
    private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
    private IntOpenHashSet piercingIgnoreEntityIds;
    private List<Entity> piercedAndKilledEntities;

    protected AbstractArrow(EntityType<? extends AbstractArrow> param0, Level param1) {
        super(param0, param1);
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> param0, double param1, double param2, double param3, Level param4) {
        this(param0, param4);
        this.setPos(param1, param2, param3);
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> param0, LivingEntity param1, Level param2) {
        this(param0, param1.x, param1.y + (double)param1.getEyeHeight() - 0.1F, param1.z, param2);
        this.setOwner(param1);
        if (param1 instanceof Player) {
            this.pickup = AbstractArrow.Pickup.ALLOWED;
        }

    }

    public void setSoundEvent(SoundEvent param0) {
        this.soundEvent = param0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        double var0 = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(var0)) {
            var0 = 1.0;
        }

        var0 *= 64.0 * getViewScale();
        return param0 < var0 * var0;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ID_FLAGS, (byte)0);
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
        this.entityData.define(PIERCE_LEVEL, (byte)0);
    }

    public void shootFromRotation(Entity param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = -Mth.sin(param2 * (float) (Math.PI / 180.0)) * Mth.cos(param1 * (float) (Math.PI / 180.0));
        float var1 = -Mth.sin(param1 * (float) (Math.PI / 180.0));
        float var2 = Mth.cos(param2 * (float) (Math.PI / 180.0)) * Mth.cos(param1 * (float) (Math.PI / 180.0));
        this.shoot((double)var0, (double)var1, (double)var2, param4, param5);
        this.setDeltaMovement(
            this.getDeltaMovement().add(param0.getDeltaMovement().x, param0.onGround ? 0.0 : param0.getDeltaMovement().y, param0.getDeltaMovement().z)
        );
    }

    @Override
    public void shoot(double param0, double param1, double param2, float param3, float param4) {
        Vec3 var0 = new Vec3(param0, param1, param2)
            .normalize()
            .add(
                this.random.nextGaussian() * 0.0075F * (double)param4,
                this.random.nextGaussian() * 0.0075F * (double)param4,
                this.random.nextGaussian() * 0.0075F * (double)param4
            )
            .scale((double)param3);
        this.setDeltaMovement(var0);
        float var1 = Mth.sqrt(getHorizontalDistanceSqr(var0));
        this.yRot = (float)(Mth.atan2(var0.x, var0.z) * 180.0F / (float)Math.PI);
        this.xRot = (float)(Mth.atan2(var0.y, (double)var1) * 180.0F / (float)Math.PI);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
        this.life = 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpTo(double param0, double param1, double param2, float param3, float param4, int param5, boolean param6) {
        this.setPos(param0, param1, param2);
        this.setRot(param3, param4);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpMotion(double param0, double param1, double param2) {
        this.setDeltaMovement(param0, param1, param2);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float var0 = Mth.sqrt(param0 * param0 + param2 * param2);
            this.xRot = (float)(Mth.atan2(param1, (double)var0) * 180.0F / (float)Math.PI);
            this.yRot = (float)(Mth.atan2(param0, param2) * 180.0F / (float)Math.PI);
            this.xRotO = this.xRot;
            this.yRotO = this.yRot;
            this.moveTo(this.x, this.y, this.z, this.yRot, this.xRot);
            this.life = 0;
        }

    }

    @Override
    public void tick() {
        super.tick();
        boolean var0 = this.isNoPhysics();
        Vec3 var1 = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float var2 = Mth.sqrt(getHorizontalDistanceSqr(var1));
            this.yRot = (float)(Mth.atan2(var1.x, var1.z) * 180.0F / (float)Math.PI);
            this.xRot = (float)(Mth.atan2(var1.y, (double)var2) * 180.0F / (float)Math.PI);
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }

        BlockPos var3 = new BlockPos(this.x, this.y, this.z);
        BlockState var4 = this.level.getBlockState(var3);
        if (!var4.isAir() && !var0) {
            VoxelShape var5 = var4.getCollisionShape(this.level, var3);
            if (!var5.isEmpty()) {
                for(AABB var6 : var5.toAabbs()) {
                    if (var6.move(var3).contains(new Vec3(this.x, this.y, this.z))) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain()) {
            this.clearFire();
        }

        if (this.inGround && !var0) {
            if (this.lastState != var4 && this.level.noCollision(this.getBoundingBox().inflate(0.06))) {
                this.inGround = false;
                this.setDeltaMovement(
                    var1.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F))
                );
                this.life = 0;
                this.flightTime = 0;
            } else if (!this.level.isClientSide) {
                this.checkDespawn();
            }

            ++this.inGroundTime;
        } else {
            this.inGroundTime = 0;
            ++this.flightTime;
            Vec3 var7 = new Vec3(this.x, this.y, this.z);
            Vec3 var8 = var7.add(var1);
            HitResult var9 = this.level.clip(new ClipContext(var7, var8, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (var9.getType() != HitResult.Type.MISS) {
                var8 = var9.getLocation();
            }

            while(!this.removed) {
                EntityHitResult var10 = this.findHitEntity(var7, var8);
                if (var10 != null) {
                    var9 = var10;
                }

                if (var9 != null && var9.getType() == HitResult.Type.ENTITY) {
                    Entity var11 = ((EntityHitResult)var9).getEntity();
                    Entity var12 = this.getOwner();
                    if (var11 instanceof Player && var12 instanceof Player && !((Player)var12).canHarmPlayer((Player)var11)) {
                        var9 = null;
                        var10 = null;
                    }
                }

                if (var9 != null && !var0) {
                    this.onHit(var9);
                    this.hasImpulse = true;
                }

                if (var10 == null || this.getPierceLevel() <= 0) {
                    break;
                }

                var9 = null;
            }

            var1 = this.getDeltaMovement();
            double var13 = var1.x;
            double var14 = var1.y;
            double var15 = var1.z;
            if (this.isCritArrow()) {
                for(int var16 = 0; var16 < 4; ++var16) {
                    this.level
                        .addParticle(
                            ParticleTypes.CRIT,
                            this.x + var13 * (double)var16 / 4.0,
                            this.y + var14 * (double)var16 / 4.0,
                            this.z + var15 * (double)var16 / 4.0,
                            -var13,
                            -var14 + 0.2,
                            -var15
                        );
                }
            }

            this.x += var13;
            this.y += var14;
            this.z += var15;
            float var17 = Mth.sqrt(getHorizontalDistanceSqr(var1));
            if (var0) {
                this.yRot = (float)(Mth.atan2(-var13, -var15) * 180.0F / (float)Math.PI);
            } else {
                this.yRot = (float)(Mth.atan2(var13, var15) * 180.0F / (float)Math.PI);
            }

            this.xRot = (float)(Mth.atan2(var14, (double)var17) * 180.0F / (float)Math.PI);

            while(this.xRot - this.xRotO < -180.0F) {
                this.xRotO -= 360.0F;
            }

            while(this.xRot - this.xRotO >= 180.0F) {
                this.xRotO += 360.0F;
            }

            while(this.yRot - this.yRotO < -180.0F) {
                this.yRotO -= 360.0F;
            }

            while(this.yRot - this.yRotO >= 180.0F) {
                this.yRotO += 360.0F;
            }

            this.xRot = Mth.lerp(0.2F, this.xRotO, this.xRot);
            this.yRot = Mth.lerp(0.2F, this.yRotO, this.yRot);
            float var18 = 0.99F;
            float var19 = 0.05F;
            if (this.isInWater()) {
                for(int var20 = 0; var20 < 4; ++var20) {
                    float var21 = 0.25F;
                    this.level.addParticle(ParticleTypes.BUBBLE, this.x - var13 * 0.25, this.y - var14 * 0.25, this.z - var15 * 0.25, var13, var14, var15);
                }

                var18 = this.getWaterInertia();
            }

            this.setDeltaMovement(var1.scale((double)var18));
            if (!this.isNoGravity() && !var0) {
                Vec3 var22 = this.getDeltaMovement();
                this.setDeltaMovement(var22.x, var22.y - 0.05F, var22.z);
            }

            this.setPos(this.x, this.y, this.z);
            this.checkInsideBlocks();
        }
    }

    protected void checkDespawn() {
        ++this.life;
        if (this.life >= 1200) {
            this.remove();
        }

    }

    protected void onHit(HitResult param0) {
        HitResult.Type var0 = param0.getType();
        if (var0 == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)param0);
        } else if (var0 == HitResult.Type.BLOCK) {
            BlockHitResult var1 = (BlockHitResult)param0;
            BlockState var2 = this.level.getBlockState(var1.getBlockPos());
            this.lastState = var2;
            Vec3 var3 = var1.getLocation().subtract(this.x, this.y, this.z);
            this.setDeltaMovement(var3);
            Vec3 var4 = var3.normalize().scale(0.05F);
            this.x -= var4.x;
            this.y -= var4.y;
            this.z -= var4.z;
            this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            this.inGround = true;
            this.shakeTime = 7;
            this.setCritArrow(false);
            this.setPierceLevel((byte)0);
            this.setSoundEvent(SoundEvents.ARROW_HIT);
            this.setShotFromCrossbow(false);
            this.resetPiercedEntities();
            var2.onProjectileHit(this.level, var2, var1, this);
        }

    }

    private void resetPiercedEntities() {
        if (this.piercedAndKilledEntities != null) {
            this.piercedAndKilledEntities.clear();
        }

        if (this.piercingIgnoreEntityIds != null) {
            this.piercingIgnoreEntityIds.clear();
        }

    }

    protected void onHitEntity(EntityHitResult param0) {
        Entity var0 = param0.getEntity();
        float var1 = (float)this.getDeltaMovement().length();
        int var2 = Mth.ceil(Math.max((double)var1 * this.baseDamage, 0.0));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.remove();
                return;
            }

            this.piercingIgnoreEntityIds.add(var0.getId());
        }

        if (this.isCritArrow()) {
            var2 += this.random.nextInt(var2 / 2 + 2);
        }

        Entity var3 = this.getOwner();
        DamageSource var4;
        if (var3 == null) {
            var4 = DamageSource.arrow(this, this);
        } else {
            var4 = DamageSource.arrow(this, var3);
            if (var3 instanceof LivingEntity) {
                ((LivingEntity)var3).setLastHurtMob(var0);
            }
        }

        int var6 = var0.getRemainingFireTicks();
        if (this.isOnFire() && !(var0 instanceof EnderMan)) {
            var0.setSecondsOnFire(5);
        }

        if (var0.hurt(var4, (float)var2)) {
            if (var0 instanceof LivingEntity) {
                LivingEntity var7 = (LivingEntity)var0;
                if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
                    var7.setArrowCount(var7.getArrowCount() + 1);
                }

                if (this.knockback > 0) {
                    Vec3 var8 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double)this.knockback * 0.6);
                    if (var8.lengthSqr() > 0.0) {
                        var7.push(var8.x, 0.1, var8.z);
                    }
                }

                if (!this.level.isClientSide && var3 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(var7, var3);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)var3, var7);
                }

                this.doPostHurtEffects(var7);
                if (var3 != null && var7 != var3 && var7 instanceof Player && var3 instanceof ServerPlayer) {
                    ((ServerPlayer)var3).connection.send(new ClientboundGameEventPacket(6, 0.0F));
                }

                if (!var0.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(var7);
                }

                if (!this.level.isClientSide && var3 instanceof ServerPlayer) {
                    ServerPlayer var9 = (ServerPlayer)var3;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(var9, this.piercedAndKilledEntities, this.piercedAndKilledEntities.size());
                    } else if (!var0.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(var9, Arrays.asList(var0), 0);
                    }
                }
            }

            this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0 && !(var0 instanceof EnderMan)) {
                this.remove();
            }
        } else {
            var0.setRemainingFireTicks(var6);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.yRot += 180.0F;
            this.yRotO += 180.0F;
            this.flightTime = 0;
            if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.remove();
            }
        }

    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ARROW_HIT;
    }

    protected final SoundEvent getHitGroundSoundEvent() {
        return this.soundEvent;
    }

    protected void doPostHurtEffects(LivingEntity param0) {
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 param0, Vec3 param1) {
        return ProjectileUtil.getHitResult(
            this.level,
            this,
            param0,
            param1,
            this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0),
            param0x -> !param0x.isSpectator()
                    && param0x.isAlive()
                    && param0x.isPickable()
                    && (param0x != this.getOwner() || this.flightTime >= 5)
                    && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(param0x.getId()))
        );
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putShort("life", (short)this.life);
        if (this.lastState != null) {
            param0.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
        }

        param0.putByte("shake", (byte)this.shakeTime);
        param0.putByte("inGround", (byte)(this.inGround ? 1 : 0));
        param0.putByte("pickup", (byte)this.pickup.ordinal());
        param0.putDouble("damage", this.baseDamage);
        param0.putBoolean("crit", this.isCritArrow());
        param0.putByte("PierceLevel", this.getPierceLevel());
        if (this.ownerUUID != null) {
            param0.putUUID("OwnerUUID", this.ownerUUID);
        }

        param0.putString("SoundEvent", Registry.SOUND_EVENT.getKey(this.soundEvent).toString());
        param0.putBoolean("ShotFromCrossbow", this.shotFromCrossbow());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        this.life = param0.getShort("life");
        if (param0.contains("inBlockState", 10)) {
            this.lastState = NbtUtils.readBlockState(param0.getCompound("inBlockState"));
        }

        this.shakeTime = param0.getByte("shake") & 255;
        this.inGround = param0.getByte("inGround") == 1;
        if (param0.contains("damage", 99)) {
            this.baseDamage = param0.getDouble("damage");
        }

        if (param0.contains("pickup", 99)) {
            this.pickup = AbstractArrow.Pickup.byOrdinal(param0.getByte("pickup"));
        } else if (param0.contains("player", 99)) {
            this.pickup = param0.getBoolean("player") ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
        }

        this.setCritArrow(param0.getBoolean("crit"));
        this.setPierceLevel(param0.getByte("PierceLevel"));
        if (param0.hasUUID("OwnerUUID")) {
            this.ownerUUID = param0.getUUID("OwnerUUID");
        }

        if (param0.contains("SoundEvent", 8)) {
            this.soundEvent = Registry.SOUND_EVENT
                .getOptional(new ResourceLocation(param0.getString("SoundEvent")))
                .orElse(this.getDefaultHitGroundSoundEvent());
        }

        this.setShotFromCrossbow(param0.getBoolean("ShotFromCrossbow"));
    }

    public void setOwner(@Nullable Entity param0) {
        this.ownerUUID = param0 == null ? null : param0.getUUID();
        if (param0 instanceof Player) {
            this.pickup = ((Player)param0).abilities.instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        }

    }

    @Nullable
    public Entity getOwner() {
        return this.ownerUUID != null && this.level instanceof ServerLevel ? ((ServerLevel)this.level).getEntity(this.ownerUUID) : null;
    }

    @Override
    public void playerTouch(Player param0) {
        if (!this.level.isClientSide && (this.inGround || this.isNoPhysics()) && this.shakeTime <= 0) {
            boolean var0 = this.pickup == AbstractArrow.Pickup.ALLOWED
                || this.pickup == AbstractArrow.Pickup.CREATIVE_ONLY && param0.abilities.instabuild
                || this.isNoPhysics() && this.getOwner().getUUID() == param0.getUUID();
            if (this.pickup == AbstractArrow.Pickup.ALLOWED && !param0.inventory.add(this.getPickupItem())) {
                var0 = false;
            }

            if (var0) {
                param0.take(this, 1);
                this.remove();
            }

        }
    }

    protected abstract ItemStack getPickupItem();

    @Override
    protected boolean makeStepSound() {
        return false;
    }

    public void setBaseDamage(double param0) {
        this.baseDamage = param0;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }

    public void setKnockback(int param0) {
        this.knockback = param0;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected float getEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.0F;
    }

    public void setCritArrow(boolean param0) {
        this.setFlag(1, param0);
    }

    public void setPierceLevel(byte param0) {
        this.entityData.set(PIERCE_LEVEL, param0);
    }

    private void setFlag(int param0, boolean param1) {
        byte var0 = this.entityData.get(ID_FLAGS);
        if (param1) {
            this.entityData.set(ID_FLAGS, (byte)(var0 | param0));
        } else {
            this.entityData.set(ID_FLAGS, (byte)(var0 & ~param0));
        }

    }

    public boolean isCritArrow() {
        byte var0 = this.entityData.get(ID_FLAGS);
        return (var0 & 1) != 0;
    }

    public boolean shotFromCrossbow() {
        byte var0 = this.entityData.get(ID_FLAGS);
        return (var0 & 4) != 0;
    }

    public byte getPierceLevel() {
        return this.entityData.get(PIERCE_LEVEL);
    }

    public void setEnchantmentEffectsFromEntity(LivingEntity param0, float param1) {
        int var0 = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER_ARROWS, param0);
        int var1 = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH_ARROWS, param0);
        this.setBaseDamage((double)(param1 * 2.0F) + this.random.nextGaussian() * 0.25 + (double)((float)this.level.getDifficulty().getId() * 0.11F));
        if (var0 > 0) {
            this.setBaseDamage(this.getBaseDamage() + (double)var0 * 0.5 + 0.5);
        }

        if (var1 > 0) {
            this.setKnockback(var1);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAMING_ARROWS, param0) > 0) {
            this.setSecondsOnFire(100);
        }

    }

    protected float getWaterInertia() {
        return 0.6F;
    }

    public void setNoPhysics(boolean param0) {
        this.noPhysics = param0;
        this.setFlag(2, param0);
    }

    public boolean isNoPhysics() {
        if (!this.level.isClientSide) {
            return this.noPhysics;
        } else {
            return (this.entityData.get(ID_FLAGS) & 2) != 0;
        }
    }

    public void setShotFromCrossbow(boolean param0) {
        this.setFlag(4, param0);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        Entity var0 = this.getOwner();
        return new ClientboundAddEntityPacket(this, var0 == null ? 0 : var0.getId());
    }

    public static enum Pickup {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;

        public static AbstractArrow.Pickup byOrdinal(int param0) {
            if (param0 < 0 || param0 > values().length) {
                param0 = 0;
            }

            return values()[param0];
        }
    }
}

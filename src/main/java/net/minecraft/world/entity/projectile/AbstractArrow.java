package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractArrow extends Projectile {
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
    @Nullable
    private BlockState lastState;
    protected boolean inGround;
    protected int inGroundTime;
    public AbstractArrow.Pickup pickup = AbstractArrow.Pickup.DISALLOWED;
    public int shakeTime;
    private int life;
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
        this(param0, param1.getX(), param1.getEyeY() - 0.1F, param1.getZ(), param2);
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
        this.entityData.define(PIERCE_LEVEL, (byte)0);
    }

    @Override
    public void shoot(double param0, double param1, double param2, float param3, float param4) {
        super.shoot(param0, param1, param2, param3, param4);
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
        super.lerpMotion(param0, param1, param2);
        this.life = 0;
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

        BlockPos var3 = this.blockPosition();
        BlockState var4 = this.level.getBlockState(var3);
        if (!var4.isAir() && !var0) {
            VoxelShape var5 = var4.getCollisionShape(this.level, var3);
            if (!var5.isEmpty()) {
                Vec3 var6 = this.position();

                for(AABB var7 : var5.toAabbs()) {
                    if (var7.move(var3).contains(var6)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain() || var4.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.inGround && !var0) {
            if (this.lastState != var4 && this.shouldFall()) {
                this.startFalling();
            } else if (!this.level.isClientSide) {
                this.tickDespawn();
            }

            ++this.inGroundTime;
        } else {
            this.inGroundTime = 0;
            Vec3 var8 = this.position();
            Vec3 var9 = var8.add(var1);
            HitResult var10 = this.level.clip(new ClipContext(var8, var9, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (var10.getType() != HitResult.Type.MISS) {
                var9 = var10.getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult var11 = this.findHitEntity(var8, var9);
                if (var11 != null) {
                    var10 = var11;
                }

                if (var10 != null && var10.getType() == HitResult.Type.ENTITY) {
                    Entity var12 = ((EntityHitResult)var10).getEntity();
                    Entity var13 = this.getOwner();
                    if (var12 instanceof Player && var13 instanceof Player && !((Player)var13).canHarmPlayer((Player)var12)) {
                        var10 = null;
                        var11 = null;
                    }
                }

                if (var10 != null && !var0) {
                    this.onHit(var10);
                    this.hasImpulse = true;
                }

                if (var11 == null || this.getPierceLevel() <= 0) {
                    break;
                }

                var10 = null;
            }

            var1 = this.getDeltaMovement();
            double var14 = var1.x;
            double var15 = var1.y;
            double var16 = var1.z;
            if (this.isCritArrow()) {
                for(int var17 = 0; var17 < 4; ++var17) {
                    this.level
                        .addParticle(
                            ParticleTypes.CRIT,
                            this.getX() + var14 * (double)var17 / 4.0,
                            this.getY() + var15 * (double)var17 / 4.0,
                            this.getZ() + var16 * (double)var17 / 4.0,
                            -var14,
                            -var15 + 0.2,
                            -var16
                        );
                }
            }

            double var18 = this.getX() + var14;
            double var19 = this.getY() + var15;
            double var20 = this.getZ() + var16;
            float var21 = Mth.sqrt(getHorizontalDistanceSqr(var1));
            if (var0) {
                this.yRot = (float)(Mth.atan2(-var14, -var16) * 180.0F / (float)Math.PI);
            } else {
                this.yRot = (float)(Mth.atan2(var14, var16) * 180.0F / (float)Math.PI);
            }

            this.xRot = (float)(Mth.atan2(var15, (double)var21) * 180.0F / (float)Math.PI);
            this.xRot = lerpRotation(this.xRotO, this.xRot);
            this.yRot = lerpRotation(this.yRotO, this.yRot);
            float var22 = 0.99F;
            float var23 = 0.05F;
            if (this.isInWater()) {
                for(int var24 = 0; var24 < 4; ++var24) {
                    float var25 = 0.25F;
                    this.level.addParticle(ParticleTypes.BUBBLE, var18 - var14 * 0.25, var19 - var15 * 0.25, var20 - var16 * 0.25, var14, var15, var16);
                }

                var22 = this.getWaterInertia();
            }

            this.setDeltaMovement(var1.scale((double)var22));
            if (!this.isNoGravity() && !var0) {
                Vec3 var26 = this.getDeltaMovement();
                this.setDeltaMovement(var26.x, var26.y - 0.05F, var26.z);
            }

            this.setPos(var18, var19, var20);
            this.checkInsideBlocks();
        }
    }

    private boolean shouldFall() {
        return this.inGround && this.level.noCollision(new AABB(this.position(), this.position()).inflate(0.06));
    }

    private void startFalling() {
        this.inGround = false;
        Vec3 var0 = this.getDeltaMovement();
        this.setDeltaMovement(
            var0.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F))
        );
        this.life = 0;
    }

    @Override
    public void move(MoverType param0, Vec3 param1) {
        super.move(param0, param1);
        if (param0 != MoverType.SELF && this.shouldFall()) {
            this.startFalling();
        }

    }

    protected void tickDespawn() {
        ++this.life;
        if (this.life >= 1200) {
            this.discard();
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

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        Entity var0 = param0.getEntity();
        float var1 = (float)this.getDeltaMovement().length();
        int var2 = Mth.ceil(Mth.clamp((double)var1 * this.baseDamage, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(var0.getId());
        }

        if (this.isCritArrow()) {
            long var3 = (long)this.random.nextInt(var2 / 2 + 2);
            var2 = (int)Math.min(var3 + (long)var2, 2147483647L);
        }

        Entity var4 = this.getOwner();
        DamageSource var5;
        if (var4 == null) {
            var5 = DamageSource.arrow(this, this);
        } else {
            var5 = DamageSource.arrow(this, var4);
            if (var4 instanceof LivingEntity) {
                ((LivingEntity)var4).setLastHurtMob(var0);
            }
        }

        boolean var7 = var0.getType() == EntityType.ENDERMAN;
        int var8 = var0.getRemainingFireTicks();
        if (this.isOnFire() && !var7) {
            var0.setSecondsOnFire(5);
        }

        if (var0.hurt(var5, (float)var2)) {
            if (var7) {
                return;
            }

            if (var0 instanceof LivingEntity) {
                LivingEntity var9 = (LivingEntity)var0;
                if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
                    var9.setArrowCount(var9.getArrowCount() + 1);
                }

                if (this.knockback > 0) {
                    Vec3 var10 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double)this.knockback * 0.6);
                    if (var10.lengthSqr() > 0.0) {
                        var9.push(var10.x, 0.1, var10.z);
                    }
                }

                if (!this.level.isClientSide && var4 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(var9, var4);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)var4, var9);
                }

                this.doPostHurtEffects(var9);
                if (var4 != null && var9 != var4 && var9 instanceof Player && var4 instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer)var4).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!var0.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(var9);
                }

                if (!this.level.isClientSide && var4 instanceof ServerPlayer) {
                    ServerPlayer var11 = (ServerPlayer)var4;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(var11, this.piercedAndKilledEntities);
                    } else if (!var0.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(var11, Arrays.asList(var0));
                    }
                }
            }

            this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            var0.setRemainingFireTicks(var8);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.yRot += 180.0F;
            this.yRotO += 180.0F;
            if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            }
        }

    }

    @Override
    protected void onHitBlock(BlockHitResult param0) {
        this.lastState = this.level.getBlockState(param0.getBlockPos());
        super.onHitBlock(param0);
        Vec3 var0 = param0.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(var0);
        Vec3 var1 = var0.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - var1.x, this.getY() - var1.y, this.getZ() - var1.z);
        this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.inGround = true;
        this.shakeTime = 7;
        this.setCritArrow(false);
        this.setPierceLevel((byte)0);
        this.setSoundEvent(SoundEvents.ARROW_HIT);
        this.setShotFromCrossbow(false);
        this.resetPiercedEntities();
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
        return ProjectileUtil.getEntityHitResult(
            this.level, this, param0, param1, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity
        );
    }

    @Override
    protected boolean canHitEntity(Entity param0x) {
        return super.canHitEntity(param0x) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(param0x.getId()));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putShort("life", (short)this.life);
        if (this.lastState != null) {
            param0.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
        }

        param0.putByte("shake", (byte)this.shakeTime);
        param0.putBoolean("inGround", this.inGround);
        param0.putByte("pickup", (byte)this.pickup.ordinal());
        param0.putDouble("damage", this.baseDamage);
        param0.putBoolean("crit", this.isCritArrow());
        param0.putByte("PierceLevel", this.getPierceLevel());
        param0.putString("SoundEvent", Registry.SOUND_EVENT.getKey(this.soundEvent).toString());
        param0.putBoolean("ShotFromCrossbow", this.shotFromCrossbow());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.life = param0.getShort("life");
        if (param0.contains("inBlockState", 10)) {
            this.lastState = NbtUtils.readBlockState(param0.getCompound("inBlockState"));
        }

        this.shakeTime = param0.getByte("shake") & 255;
        this.inGround = param0.getBoolean("inGround");
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
        if (param0.contains("SoundEvent", 8)) {
            this.soundEvent = Registry.SOUND_EVENT
                .getOptional(new ResourceLocation(param0.getString("SoundEvent")))
                .orElse(this.getDefaultHitGroundSoundEvent());
        }

        this.setShotFromCrossbow(param0.getBoolean("ShotFromCrossbow"));
    }

    @Override
    public void setOwner(@Nullable Entity param0) {
        super.setOwner(param0);
        if (param0 instanceof Player) {
            this.pickup = ((Player)param0).getAbilities().instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        }

    }

    @Override
    public void playerTouch(Player param0) {
        if (!this.level.isClientSide && (this.inGround || this.isNoPhysics()) && this.shakeTime <= 0) {
            boolean var0 = this.pickup == AbstractArrow.Pickup.ALLOWED
                || this.pickup == AbstractArrow.Pickup.CREATIVE_ONLY && param0.getAbilities().instabuild
                || this.isNoPhysics() && this.getOwner().getUUID() == param0.getUUID();
            if (this.pickup == AbstractArrow.Pickup.ALLOWED && !param0.getInventory().add(this.getPickupItem())) {
                var0 = false;
            }

            if (var0) {
                param0.take(this, 1);
                this.discard();
            }

        }
    }

    protected abstract ItemStack getPickupItem();

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
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
        return 0.13F;
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

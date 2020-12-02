package net.minecraft.world.entity.projectile;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
    value = Dist.CLIENT,
    _interface = ItemSupplier.class
)
public class FireworkRocketEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(
        FireworkRocketEntity.class, EntityDataSerializers.ITEM_STACK
    );
    private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(
        FireworkRocketEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT
    );
    private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.BOOLEAN);
    private int life;
    private int lifetime;
    private LivingEntity attachedToEntity;

    public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> param0, Level param1) {
        super(param0, param1);
    }

    public FireworkRocketEntity(Level param0, double param1, double param2, double param3, ItemStack param4) {
        super(EntityType.FIREWORK_ROCKET, param0);
        this.life = 0;
        this.setPos(param1, param2, param3);
        int var0 = 1;
        if (!param4.isEmpty() && param4.hasTag()) {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, param4.copy());
            var0 += param4.getOrCreateTagElement("Fireworks").getByte("Flight");
        }

        this.setDeltaMovement(this.random.nextGaussian() * 0.001, 0.05, this.random.nextGaussian() * 0.001);
        this.lifetime = 10 * var0 + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public FireworkRocketEntity(Level param0, @Nullable Entity param1, double param2, double param3, double param4, ItemStack param5) {
        this(param0, param2, param3, param4, param5);
        this.setOwner(param1);
    }

    public FireworkRocketEntity(Level param0, ItemStack param1, LivingEntity param2) {
        this(param0, param2, param2.getX(), param2.getY(), param2.getZ(), param1);
        this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of(param2.getId()));
        this.attachedToEntity = param2;
    }

    public FireworkRocketEntity(Level param0, ItemStack param1, double param2, double param3, double param4, boolean param5) {
        this(param0, param2, param3, param4, param1);
        this.entityData.set(DATA_SHOT_AT_ANGLE, param5);
    }

    public FireworkRocketEntity(Level param0, ItemStack param1, Entity param2, double param3, double param4, double param5, boolean param6) {
        this(param0, param1, param3, param4, param5, param6);
        this.setOwner(param2);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_FIREWORKS_ITEM, ItemStack.EMPTY);
        this.entityData.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
        this.entityData.define(DATA_SHOT_AT_ANGLE, false);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        return param0 < 4096.0 && !this.isAttachedToEntity();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRender(double param0, double param1, double param2) {
        return super.shouldRender(param0, param1, param2) && !this.isAttachedToEntity();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isAttachedToEntity()) {
            if (this.attachedToEntity == null) {
                this.entityData.get(DATA_ATTACHED_TO_TARGET).ifPresent(param0 -> {
                    Entity var0x = this.level.getEntity(param0);
                    if (var0x instanceof LivingEntity) {
                        this.attachedToEntity = (LivingEntity)var0x;
                    }

                });
            }

            if (this.attachedToEntity != null) {
                if (this.attachedToEntity.isFallFlying()) {
                    Vec3 var0 = this.attachedToEntity.getLookAngle();
                    double var1 = 1.5;
                    double var2 = 0.1;
                    Vec3 var3 = this.attachedToEntity.getDeltaMovement();
                    this.attachedToEntity
                        .setDeltaMovement(
                            var3.add(
                                var0.x * 0.1 + (var0.x * 1.5 - var3.x) * 0.5,
                                var0.y * 0.1 + (var0.y * 1.5 - var3.y) * 0.5,
                                var0.z * 0.1 + (var0.z * 1.5 - var3.z) * 0.5
                            )
                        );
                }

                this.setPos(this.attachedToEntity.getX(), this.attachedToEntity.getY(), this.attachedToEntity.getZ());
                this.setDeltaMovement(this.attachedToEntity.getDeltaMovement());
            }
        } else {
            if (!this.isShotAtAngle()) {
                double var4 = this.horizontalCollision ? 1.0 : 1.15;
                this.setDeltaMovement(this.getDeltaMovement().multiply(var4, 1.0, var4).add(0.0, 0.04, 0.0));
            }

            Vec3 var5 = this.getDeltaMovement();
            this.move(MoverType.SELF, var5);
            this.setDeltaMovement(var5);
        }

        HitResult var6 = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (!this.noPhysics) {
            this.onHit(var6);
            this.hasImpulse = true;
        }

        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
        }

        ++this.life;
        if (this.level.isClientSide && this.life % 2 < 2) {
            this.level
                .addParticle(
                    ParticleTypes.FIREWORK,
                    this.getX(),
                    this.getY() - 0.3,
                    this.getZ(),
                    this.random.nextGaussian() * 0.05,
                    -this.getDeltaMovement().y * 0.5,
                    this.random.nextGaussian() * 0.05
                );
        }

        if (!this.level.isClientSide && this.life > this.lifetime) {
            this.explode();
        }

    }

    private void explode() {
        this.level.broadcastEntityEvent(this, (byte)17);
        this.gameEvent(this.getOwner(), GameEvent.EXPLODE);
        this.dealExplosionDamage();
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        if (!this.level.isClientSide) {
            this.explode();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult param0) {
        BlockPos var0 = new BlockPos(param0.getBlockPos());
        this.level.getBlockState(var0).entityInside(this.level, var0, this);
        if (!this.level.isClientSide() && this.hasExplosion()) {
            this.explode();
        }

        super.onHitBlock(param0);
    }

    private boolean hasExplosion() {
        ItemStack var0 = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        CompoundTag var1 = var0.isEmpty() ? null : var0.getTagElement("Fireworks");
        ListTag var2 = var1 != null ? var1.getList("Explosions", 10) : null;
        return var2 != null && !var2.isEmpty();
    }

    private void dealExplosionDamage() {
        float var0 = 0.0F;
        ItemStack var1 = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        CompoundTag var2 = var1.isEmpty() ? null : var1.getTagElement("Fireworks");
        ListTag var3 = var2 != null ? var2.getList("Explosions", 10) : null;
        if (var3 != null && !var3.isEmpty()) {
            var0 = 5.0F + (float)(var3.size() * 2);
        }

        if (var0 > 0.0F) {
            if (this.attachedToEntity != null) {
                this.attachedToEntity.hurt(DamageSource.fireworks(this, this.getOwner()), 5.0F + (float)(var3.size() * 2));
            }

            double var4 = 5.0;
            Vec3 var5 = this.position();

            for(LivingEntity var7 : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0))) {
                if (var7 != this.attachedToEntity && !(this.distanceToSqr(var7) > 25.0)) {
                    boolean var8 = false;

                    for(int var9 = 0; var9 < 2; ++var9) {
                        Vec3 var10 = new Vec3(var7.getX(), var7.getY(0.5 * (double)var9), var7.getZ());
                        HitResult var11 = this.level.clip(new ClipContext(var5, var10, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                        if (var11.getType() == HitResult.Type.MISS) {
                            var8 = true;
                            break;
                        }
                    }

                    if (var8) {
                        float var12 = var0 * (float)Math.sqrt((5.0 - (double)this.distanceTo(var7)) / 5.0);
                        var7.hurt(DamageSource.fireworks(this, this.getOwner()), var12);
                    }
                }
            }
        }

    }

    private boolean isAttachedToEntity() {
        return this.entityData.get(DATA_ATTACHED_TO_TARGET).isPresent();
    }

    public boolean isShotAtAngle() {
        return this.entityData.get(DATA_SHOT_AT_ANGLE);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 17 && this.level.isClientSide) {
            if (!this.hasExplosion()) {
                for(int var0 = 0; var0 < this.random.nextInt(3) + 2; ++var0) {
                    this.level
                        .addParticle(
                            ParticleTypes.POOF,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            this.random.nextGaussian() * 0.05,
                            0.005,
                            this.random.nextGaussian() * 0.05
                        );
                }
            } else {
                ItemStack var1 = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
                CompoundTag var2 = var1.isEmpty() ? null : var1.getTagElement("Fireworks");
                Vec3 var3 = this.getDeltaMovement();
                this.level.createFireworks(this.getX(), this.getY(), this.getZ(), var3.x, var3.y, var3.z, var2);
            }
        }

        super.handleEntityEvent(param0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Life", this.life);
        param0.putInt("LifeTime", this.lifetime);
        ItemStack var0 = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        if (!var0.isEmpty()) {
            param0.put("FireworksItem", var0.save(new CompoundTag()));
        }

        param0.putBoolean("ShotAtAngle", this.entityData.get(DATA_SHOT_AT_ANGLE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.life = param0.getInt("Life");
        this.lifetime = param0.getInt("LifeTime");
        ItemStack var0 = ItemStack.of(param0.getCompound("FireworksItem"));
        if (!var0.isEmpty()) {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, var0);
        }

        if (param0.contains("ShotAtAngle")) {
            this.entityData.set(DATA_SHOT_AT_ANGLE, param0.getBoolean("ShotAtAngle"));
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getItem() {
        ItemStack var0 = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        return var0.isEmpty() ? new ItemStack(Items.FIREWORK_ROCKET) : var0;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}

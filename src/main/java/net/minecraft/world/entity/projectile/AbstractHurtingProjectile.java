package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractHurtingProjectile extends Projectile {
    public double xPower;
    public double yPower;
    public double zPower;

    protected AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> param0, Level param1) {
        super(param0, param1);
    }

    public AbstractHurtingProjectile(
        EntityType<? extends AbstractHurtingProjectile> param0,
        double param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        Level param7
    ) {
        this(param0, param7);
        this.moveTo(param1, param2, param3, this.yRot, this.xRot);
        this.reapplyPosition();
        double var0 = (double)Mth.sqrt(param4 * param4 + param5 * param5 + param6 * param6);
        if (var0 != 0.0) {
            this.xPower = param4 / var0 * 0.1;
            this.yPower = param5 / var0 * 0.1;
            this.zPower = param6 / var0 * 0.1;
        }

    }

    public AbstractHurtingProjectile(
        EntityType<? extends AbstractHurtingProjectile> param0, LivingEntity param1, double param2, double param3, double param4, Level param5
    ) {
        this(param0, param1.getX(), param1.getY(), param1.getZ(), param2, param3, param4, param5);
        this.setOwner(param1);
        this.setRot(param1.yRot, param1.xRot);
    }

    @Override
    protected void defineSynchedData() {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        double var0 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(var0)) {
            var0 = 4.0;
        }

        var0 *= 64.0;
        return param0 < var0 * var0;
    }

    @Override
    public void tick() {
        Entity var0 = this.getOwner();
        if (this.level.isClientSide || (var0 == null || !var0.removed) && this.level.hasChunkAt(this.blockPosition())) {
            super.tick();
            if (this.shouldBurn()) {
                this.setSecondsOnFire(1);
            }

            HitResult var1 = ProjectileUtil.getHitResult(this, this::canHitEntity, ClipContext.Block.COLLIDER);
            if (var1.getType() != HitResult.Type.MISS) {
                this.onHit(var1);
            }

            Vec3 var2 = this.getDeltaMovement();
            double var3 = this.getX() + var2.x;
            double var4 = this.getY() + var2.y;
            double var5 = this.getZ() + var2.z;
            ProjectileUtil.rotateTowardsMovement(this, 0.2F);
            float var6 = this.getInertia();
            if (this.isInWater()) {
                for(int var7 = 0; var7 < 4; ++var7) {
                    float var8 = 0.25F;
                    this.level.addParticle(ParticleTypes.BUBBLE, var3 - var2.x * 0.25, var4 - var2.y * 0.25, var5 - var2.z * 0.25, var2.x, var2.y, var2.z);
                }

                var6 = 0.8F;
            }

            this.setDeltaMovement(var2.add(this.xPower, this.yPower, this.zPower).scale((double)var6));
            this.level.addParticle(this.getTrailParticle(), var3, var4 + 0.5, var5, 0.0, 0.0, 0.0);
            this.setPos(var3, var4, var5);
        } else {
            this.remove();
        }
    }

    @Override
    protected boolean canHitEntity(Entity param0) {
        return super.canHitEntity(param0) && !param0.noPhysics;
    }

    protected boolean shouldBurn() {
        return true;
    }

    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.SMOKE;
    }

    protected float getInertia() {
        return 0.95F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.put("power", this.newDoubleList(new double[]{this.xPower, this.yPower, this.zPower}));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("power", 9)) {
            ListTag var0 = param0.getList("power", 6);
            if (var0.size() == 3) {
                this.xPower = var0.getDouble(0);
                this.yPower = var0.getDouble(1);
                this.zPower = var0.getDouble(2);
            }
        }

    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            this.markHurt();
            Entity var0 = param0.getEntity();
            if (var0 != null) {
                Vec3 var1 = var0.getLookAngle();
                this.setDeltaMovement(var1);
                this.xPower = var1.x * 0.1;
                this.yPower = var1.y * 0.1;
                this.zPower = var1.z * 0.1;
                this.setOwner(var0);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        Entity var0 = this.getOwner();
        int var1 = var0 == null ? 0 : var0.getId();
        return new ClientboundAddEntityPacket(
            this.getId(),
            this.getUUID(),
            this.getX(),
            this.getY(),
            this.getZ(),
            this.xRot,
            this.yRot,
            this.getType(),
            var1,
            new Vec3(this.xPower, this.yPower, this.zPower)
        );
    }
}

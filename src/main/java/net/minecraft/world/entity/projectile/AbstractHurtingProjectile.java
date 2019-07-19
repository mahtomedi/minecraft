package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
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

public abstract class AbstractHurtingProjectile extends Entity {
    public LivingEntity owner;
    private int life;
    private int flightTime;
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
        this.setPos(param1, param2, param3);
        double var0 = (double)Mth.sqrt(param4 * param4 + param5 * param5 + param6 * param6);
        this.xPower = param4 / var0 * 0.1;
        this.yPower = param5 / var0 * 0.1;
        this.zPower = param6 / var0 * 0.1;
    }

    public AbstractHurtingProjectile(
        EntityType<? extends AbstractHurtingProjectile> param0, LivingEntity param1, double param2, double param3, double param4, Level param5
    ) {
        this(param0, param5);
        this.owner = param1;
        this.moveTo(param1.x, param1.y, param1.z, param1.yRot, param1.xRot);
        this.setPos(this.x, this.y, this.z);
        this.setDeltaMovement(Vec3.ZERO);
        param2 += this.random.nextGaussian() * 0.4;
        param3 += this.random.nextGaussian() * 0.4;
        param4 += this.random.nextGaussian() * 0.4;
        double var0 = (double)Mth.sqrt(param2 * param2 + param3 * param3 + param4 * param4);
        this.xPower = param2 / var0 * 0.1;
        this.yPower = param3 / var0 * 0.1;
        this.zPower = param4 / var0 * 0.1;
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
        if (this.level.isClientSide || (this.owner == null || !this.owner.removed) && this.level.hasChunkAt(new BlockPos(this))) {
            super.tick();
            if (this.shouldBurn()) {
                this.setSecondsOnFire(1);
            }

            ++this.flightTime;
            HitResult var0 = ProjectileUtil.forwardsRaycast(this, true, this.flightTime >= 25, this.owner, ClipContext.Block.COLLIDER);
            if (var0.getType() != HitResult.Type.MISS) {
                this.onHit(var0);
            }

            Vec3 var1 = this.getDeltaMovement();
            this.x += var1.x;
            this.y += var1.y;
            this.z += var1.z;
            ProjectileUtil.rotateTowardsMovement(this, 0.2F);
            float var2 = this.getInertia();
            if (this.isInWater()) {
                for(int var3 = 0; var3 < 4; ++var3) {
                    float var4 = 0.25F;
                    this.level
                        .addParticle(ParticleTypes.BUBBLE, this.x - var1.x * 0.25, this.y - var1.y * 0.25, this.z - var1.z * 0.25, var1.x, var1.y, var1.z);
                }

                var2 = 0.8F;
            }

            this.setDeltaMovement(var1.add(this.xPower, this.yPower, this.zPower).scale((double)var2));
            this.level.addParticle(this.getTrailParticle(), this.x, this.y + 0.5, this.z, 0.0, 0.0, 0.0);
            this.setPos(this.x, this.y, this.z);
        } else {
            this.remove();
        }
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

    protected abstract void onHit(HitResult var1);

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        Vec3 var0 = this.getDeltaMovement();
        param0.put("direction", this.newDoubleList(new double[]{var0.x, var0.y, var0.z}));
        param0.put("power", this.newDoubleList(new double[]{this.xPower, this.yPower, this.zPower}));
        param0.putInt("life", this.life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        if (param0.contains("power", 9)) {
            ListTag var0 = param0.getList("power", 6);
            if (var0.size() == 3) {
                this.xPower = var0.getDouble(0);
                this.yPower = var0.getDouble(1);
                this.zPower = var0.getDouble(2);
            }
        }

        this.life = param0.getInt("life");
        if (param0.contains("direction", 9) && param0.getList("direction", 6).size() == 3) {
            ListTag var1 = param0.getList("direction", 6);
            this.setDeltaMovement(var1.getDouble(0), var1.getDouble(1), var1.getDouble(2));
        } else {
            this.remove();
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
            if (param0.getEntity() != null) {
                Vec3 var0 = param0.getEntity().getLookAngle();
                this.setDeltaMovement(var0);
                this.xPower = var0.x * 0.1;
                this.yPower = var0.y * 0.1;
                this.zPower = var0.z * 0.1;
                if (param0.getEntity() instanceof LivingEntity) {
                    this.owner = (LivingEntity)param0.getEntity();
                }

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

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getLightColor() {
        return 15728880;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        int var0 = this.owner == null ? 0 : this.owner.getId();
        return new ClientboundAddEntityPacket(
            this.getId(), this.getUUID(), this.x, this.y, this.z, this.xRot, this.yRot, this.getType(), var0, new Vec3(this.xPower, this.yPower, this.zPower)
        );
    }
}

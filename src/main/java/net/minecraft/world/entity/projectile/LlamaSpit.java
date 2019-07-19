package net.minecraft.world.entity.projectile;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LlamaSpit extends Entity implements Projectile {
    public Llama owner;
    private CompoundTag ownerTag;

    public LlamaSpit(EntityType<? extends LlamaSpit> param0, Level param1) {
        super(param0, param1);
    }

    public LlamaSpit(Level param0, Llama param1) {
        this(EntityType.LLAMA_SPIT, param0);
        this.owner = param1;
        this.setPos(
            param1.x - (double)(param1.getBbWidth() + 1.0F) * 0.5 * (double)Mth.sin(param1.yBodyRot * (float) (Math.PI / 180.0)),
            param1.y + (double)param1.getEyeHeight() - 0.1F,
            param1.z + (double)(param1.getBbWidth() + 1.0F) * 0.5 * (double)Mth.cos(param1.yBodyRot * (float) (Math.PI / 180.0))
        );
    }

    @OnlyIn(Dist.CLIENT)
    public LlamaSpit(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        this(EntityType.LLAMA_SPIT, param0);
        this.setPos(param1, param2, param3);

        for(int var0 = 0; var0 < 7; ++var0) {
            double var1 = 0.4 + 0.1 * (double)var0;
            param0.addParticle(ParticleTypes.SPIT, param1, param2, param3, param4 * var1, param5, param6 * var1);
        }

        this.setDeltaMovement(param4, param5, param6);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ownerTag != null) {
            this.restoreOwnerFromSave();
        }

        Vec3 var0 = this.getDeltaMovement();
        HitResult var1 = ProjectileUtil.getHitResult(
            this,
            this.getBoundingBox().expandTowards(var0).inflate(1.0),
            param0 -> !param0.isSpectator() && param0 != this.owner,
            ClipContext.Block.OUTLINE,
            true
        );
        if (var1 != null) {
            this.onHit(var1);
        }

        this.x += var0.x;
        this.y += var0.y;
        this.z += var0.z;
        float var2 = Mth.sqrt(getHorizontalDistanceSqr(var0));
        this.yRot = (float)(Mth.atan2(var0.x, var0.z) * 180.0F / (float)Math.PI);
        this.xRot = (float)(Mth.atan2(var0.y, (double)var2) * 180.0F / (float)Math.PI);

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
        float var3 = 0.99F;
        float var4 = 0.06F;
        if (!this.level.containsMaterial(this.getBoundingBox(), Material.AIR)) {
            this.remove();
        } else if (this.isInWaterOrBubble()) {
            this.remove();
        } else {
            this.setDeltaMovement(var0.scale(0.99F));
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.06F, 0.0));
            }

            this.setPos(this.x, this.y, this.z);
        }
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
        }

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
        this.yRot = (float)(Mth.atan2(var0.x, param2) * 180.0F / (float)Math.PI);
        this.xRot = (float)(Mth.atan2(var0.y, (double)var1) * 180.0F / (float)Math.PI);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    public void onHit(HitResult param0) {
        HitResult.Type var0 = param0.getType();
        if (var0 == HitResult.Type.ENTITY && this.owner != null) {
            ((EntityHitResult)param0).getEntity().hurt(DamageSource.indirectMobAttack(this, this.owner).setProjectile(), 1.0F);
        } else if (var0 == HitResult.Type.BLOCK && !this.level.isClientSide) {
            this.remove();
        }

    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        if (param0.contains("Owner", 10)) {
            this.ownerTag = param0.getCompound("Owner");
        }

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        if (this.owner != null) {
            CompoundTag var0 = new CompoundTag();
            UUID var1 = this.owner.getUUID();
            var0.putUUID("OwnerUUID", var1);
            param0.put("Owner", var0);
        }

    }

    private void restoreOwnerFromSave() {
        if (this.ownerTag != null && this.ownerTag.hasUUID("OwnerUUID")) {
            UUID var0 = this.ownerTag.getUUID("OwnerUUID");

            for(Llama var2 : this.level.getEntitiesOfClass(Llama.class, this.getBoundingBox().inflate(15.0))) {
                if (var2.getUUID().equals(var0)) {
                    this.owner = var2;
                    break;
                }
            }
        }

        this.ownerTag = null;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

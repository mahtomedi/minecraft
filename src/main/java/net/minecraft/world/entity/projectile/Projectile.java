package net.minecraft.world.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Projectile extends Entity {
    private UUID ownerUUID;

    Projectile(EntityType<? extends Projectile> param0, Level param1) {
        super(param0, param1);
    }

    public void setOwner(@Nullable Entity param0) {
        this.ownerUUID = param0 == null ? null : param0.getUUID();
    }

    @Nullable
    public Entity getOwner() {
        return this.ownerUUID != null && this.level instanceof ServerLevel ? ((ServerLevel)this.level).getEntity(this.ownerUUID) : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        if (this.ownerUUID != null) {
            param0.putUUIDAsArray("OwnerUUID", this.ownerUUID);
        }

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        if (param0.hasUUIDArray("OwnerUUID")) {
            this.ownerUUID = param0.getUUIDFromArray("OwnerUUID");
        }

    }

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
    }

    public void shootFromRotation(Entity param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = -Mth.sin(param2 * (float) (Math.PI / 180.0)) * Mth.cos(param1 * (float) (Math.PI / 180.0));
        float var1 = -Mth.sin((param1 + param3) * (float) (Math.PI / 180.0));
        float var2 = Mth.cos(param2 * (float) (Math.PI / 180.0)) * Mth.cos(param1 * (float) (Math.PI / 180.0));
        this.shoot((double)var0, (double)var1, (double)var2, param4, param5);
        Vec3 var3 = param0.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(var3.x, param0.isOnGround() ? 0.0 : var3.y, var3.z));
    }

    protected void onHit(HitResult param0) {
        HitResult.Type var0 = param0.getType();
        if (var0 == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)param0);
        } else if (var0 == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult)param0);
        }

    }

    protected void onHitEntity(EntityHitResult param0) {
    }

    protected void onHitBlock(BlockHitResult param0) {
        BlockState var1 = this.level.getBlockState(param0.getBlockPos());
        var1.onProjectileHit(this.level, var1, param0, this);
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
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
        }

    }
}

package net.minecraft.world.entity.projectile;

import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ThrowableProjectile extends Entity implements Projectile {
    private int xBlock = -1;
    private int yBlock = -1;
    private int zBlock = -1;
    protected boolean inGround;
    public int shakeTime;
    protected LivingEntity owner;
    private UUID ownerId;
    private boolean leftOwner;

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> param0, Level param1) {
        super(param0, param1);
    }

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> param0, double param1, double param2, double param3, Level param4) {
        this(param0, param4);
        this.setPos(param1, param2, param3);
    }

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> param0, LivingEntity param1, Level param2) {
        this(param0, param1.getX(), param1.getEyeY() - 0.1F, param1.getZ(), param2);
        this.owner = param1;
        this.ownerId = param1.getUUID();
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

    public void shootFromRotation(Entity param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = -Mth.sin(param2 * (float) (Math.PI / 180.0)) * Mth.cos(param1 * (float) (Math.PI / 180.0));
        float var1 = -Mth.sin((param1 + param3) * (float) (Math.PI / 180.0));
        float var2 = Mth.cos(param2 * (float) (Math.PI / 180.0)) * Mth.cos(param1 * (float) (Math.PI / 180.0));
        this.shoot((double)var0, (double)var1, (double)var2, param4, param5);
        Vec3 var3 = param0.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(var3.x, param0.isOnGround() ? 0.0 : var3.y, var3.z));
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
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpMotion(double param0, double param1, double param2) {
        this.setDeltaMovement(param0, param1, param2);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float var0 = Mth.sqrt(param0 * param0 + param2 * param2);
            this.yRot = (float)(Mth.atan2(param0, param2) * 180.0F / (float)Math.PI);
            this.xRot = (float)(Mth.atan2(param1, (double)var0) * 180.0F / (float)Math.PI);
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.inGround) {
            this.inGround = false;
            this.setDeltaMovement(
                this.getDeltaMovement()
                    .multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F))
            );
        }

        AABB var0 = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
        if (this.owner == null) {
            this.leftOwner = true;
        } else if (!this.leftOwner) {
            boolean var1 = false;

            for(Entity var2 : this.level.getEntities(this, var0, param0 -> !param0.isSpectator() && param0.isPickable())) {
                if (this.isEntityOrVehicle(var2, this.owner)) {
                    var1 = true;
                    break;
                }
            }

            if (!var1) {
                this.leftOwner = true;
            }
        }

        Predicate<Entity> var3 = param0 -> !param0.isSpectator() && param0.isPickable() && (this.leftOwner || !this.isEntityOrVehicle(param0, this.owner));
        HitResult var4 = ProjectileUtil.getHitResult(this, var0, var3, ClipContext.Block.OUTLINE, true);
        if (var4.getType() != HitResult.Type.MISS) {
            if (var4.getType() == HitResult.Type.BLOCK && this.level.getBlockState(((BlockHitResult)var4).getBlockPos()).getBlock() == Blocks.NETHER_PORTAL) {
                this.handleInsidePortal(((BlockHitResult)var4).getBlockPos());
            } else {
                this.onHit(var4);
            }
        }

        Vec3 var5 = this.getDeltaMovement();
        double var6 = this.getX() + var5.x;
        double var7 = this.getY() + var5.y;
        double var8 = this.getZ() + var5.z;
        float var9 = Mth.sqrt(getHorizontalDistanceSqr(var5));
        this.yRot = (float)(Mth.atan2(var5.x, var5.z) * 180.0F / (float)Math.PI);
        this.xRot = (float)(Mth.atan2(var5.y, (double)var9) * 180.0F / (float)Math.PI);

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
        float var12;
        if (this.isInWater()) {
            for(int var10 = 0; var10 < 4; ++var10) {
                float var11 = 0.25F;
                this.level.addParticle(ParticleTypes.BUBBLE, var6 - var5.x * 0.25, var7 - var5.y * 0.25, var8 - var5.z * 0.25, var5.x, var5.y, var5.z);
            }

            var12 = 0.8F;
        } else {
            var12 = 0.99F;
        }

        this.setDeltaMovement(var5.scale((double)var12));
        if (!this.isNoGravity()) {
            Vec3 var14 = this.getDeltaMovement();
            this.setDeltaMovement(var14.x, var14.y - (double)this.getGravity(), var14.z);
        }

        this.setPos(var6, var7, var8);
    }

    private boolean isEntityOrVehicle(Entity param0, Entity param1) {
        return param0 == param1 || param0.getPassengers().contains(param1);
    }

    protected float getGravity() {
        return 0.03F;
    }

    protected abstract void onHit(HitResult var1);

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putInt("xTile", this.xBlock);
        param0.putInt("yTile", this.yBlock);
        param0.putInt("zTile", this.zBlock);
        param0.putByte("shake", (byte)this.shakeTime);
        param0.putBoolean("inGround", this.inGround);
        if (this.ownerId != null) {
            param0.put("owner", NbtUtils.createUUIDTag(this.ownerId));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        this.xBlock = param0.getInt("xTile");
        this.yBlock = param0.getInt("yTile");
        this.zBlock = param0.getInt("zTile");
        this.shakeTime = param0.getByte("shake") & 255;
        this.inGround = param0.getBoolean("inGround");
        this.owner = null;
        if (param0.contains("owner", 10)) {
            this.ownerId = NbtUtils.loadUUIDTag(param0.getCompound("owner"));
        }

    }

    @Nullable
    public LivingEntity getOwner() {
        if ((this.owner == null || this.owner.removed) && this.ownerId != null && this.level instanceof ServerLevel) {
            Entity var0 = ((ServerLevel)this.level).getEntity(this.ownerId);
            if (var0 instanceof LivingEntity) {
                this.owner = (LivingEntity)var0;
            } else {
                this.owner = null;
            }
        }

        return this.owner;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

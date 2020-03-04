package net.minecraft.world.entity.projectile;

import java.util.function.Predicate;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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

public abstract class ThrowableProjectile extends Projectile {
    private int xBlock = -1;
    private int yBlock = -1;
    private int zBlock = -1;
    protected boolean inGround;
    private int shakeTime;
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
        this.setOwner(param1);
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
        Entity var1 = this.getOwner();
        if (var1 == null) {
            this.leftOwner = true;
        } else if (!this.leftOwner) {
            boolean var2 = false;

            for(Entity var3 : this.level.getEntities(this, var0, param0 -> !param0.isSpectator() && param0.isPickable())) {
                if (this.isEntityOrVehicle(var3, var1)) {
                    var2 = true;
                    break;
                }
            }

            if (!var2) {
                this.leftOwner = true;
            }
        }

        Predicate<Entity> var4 = param1 -> !param1.isSpectator() && param1.isPickable() && (this.leftOwner || !this.isEntityOrVehicle(param1, var1));
        HitResult var5 = ProjectileUtil.getHitResult(this, var0, var4, ClipContext.Block.OUTLINE, true);
        if (var5.getType() != HitResult.Type.MISS) {
            if (var5.getType() == HitResult.Type.BLOCK && this.level.getBlockState(((BlockHitResult)var5).getBlockPos()).getBlock() == Blocks.NETHER_PORTAL) {
                this.handleInsidePortal(((BlockHitResult)var5).getBlockPos());
            } else {
                this.onHit(var5);
            }
        }

        Vec3 var6 = this.getDeltaMovement();
        double var7 = this.getX() + var6.x;
        double var8 = this.getY() + var6.y;
        double var9 = this.getZ() + var6.z;
        float var10 = Mth.sqrt(getHorizontalDistanceSqr(var6));
        this.yRot = (float)(Mth.atan2(var6.x, var6.z) * 180.0F / (float)Math.PI);
        this.xRot = (float)(Mth.atan2(var6.y, (double)var10) * 180.0F / (float)Math.PI);

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
        float var13;
        if (this.isInWater()) {
            for(int var11 = 0; var11 < 4; ++var11) {
                float var12 = 0.25F;
                this.level.addParticle(ParticleTypes.BUBBLE, var7 - var6.x * 0.25, var8 - var6.y * 0.25, var9 - var6.z * 0.25, var6.x, var6.y, var6.z);
            }

            var13 = 0.8F;
        } else {
            var13 = 0.99F;
        }

        this.setDeltaMovement(var6.scale((double)var13));
        if (!this.isNoGravity()) {
            Vec3 var15 = this.getDeltaMovement();
            this.setDeltaMovement(var15.x, var15.y - (double)this.getGravity(), var15.z);
        }

        this.setPos(var7, var8, var9);
    }

    private boolean isEntityOrVehicle(Entity param0, Entity param1) {
        return param0 == param1 || param0.getPassengers().contains(param1);
    }

    protected float getGravity() {
        return 0.03F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("xTile", this.xBlock);
        param0.putInt("yTile", this.yBlock);
        param0.putInt("zTile", this.zBlock);
        param0.putByte("shake", (byte)this.shakeTime);
        param0.putBoolean("inGround", this.inGround);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.xBlock = param0.getInt("xTile");
        this.yBlock = param0.getInt("yTile");
        this.zBlock = param0.getInt("zTile");
        this.shakeTime = param0.getByte("shake") & 255;
        this.inGround = param0.getBoolean("inGround");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

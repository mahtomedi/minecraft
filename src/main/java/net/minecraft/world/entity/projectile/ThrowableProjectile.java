package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ThrowableProjectile extends Projectile {
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
        HitResult var0 = ProjectileUtil.getHitResult(this, this::canHitEntity, ClipContext.Block.OUTLINE);
        if (var0.getType() != HitResult.Type.MISS) {
            if (var0.getType() == HitResult.Type.BLOCK && this.level.getBlockState(((BlockHitResult)var0).getBlockPos()).is(Blocks.NETHER_PORTAL)) {
                this.handleInsidePortal(((BlockHitResult)var0).getBlockPos());
            } else {
                this.onHit(var0);
            }
        }

        Vec3 var1 = this.getDeltaMovement();
        double var2 = this.getX() + var1.x;
        double var3 = this.getY() + var1.y;
        double var4 = this.getZ() + var1.z;
        this.updateRotation();
        float var7;
        if (this.isInWater()) {
            for(int var5 = 0; var5 < 4; ++var5) {
                float var6 = 0.25F;
                this.level.addParticle(ParticleTypes.BUBBLE, var2 - var1.x * 0.25, var3 - var1.y * 0.25, var4 - var1.z * 0.25, var1.x, var1.y, var1.z);
            }

            var7 = 0.8F;
        } else {
            var7 = 0.99F;
        }

        this.setDeltaMovement(var1.scale((double)var7));
        if (!this.isNoGravity()) {
            Vec3 var9 = this.getDeltaMovement();
            this.setDeltaMovement(var9.x, var9.y - (double)this.getGravity(), var9.z);
        }

        this.setPos(var2, var3, var4);
    }

    protected float getGravity() {
        return 0.03F;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

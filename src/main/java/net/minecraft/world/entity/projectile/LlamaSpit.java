package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LlamaSpit extends Projectile {
    public LlamaSpit(EntityType<? extends LlamaSpit> param0, Level param1) {
        super(param0, param1);
    }

    public LlamaSpit(Level param0, Llama param1) {
        this(EntityType.LLAMA_SPIT, param0);
        super.setOwner(param1);
        this.setPos(
            param1.getX() - (double)(param1.getBbWidth() + 1.0F) * 0.5 * (double)Mth.sin(param1.yBodyRot * (float) (Math.PI / 180.0)),
            param1.getEyeY() - 0.1F,
            param1.getZ() + (double)(param1.getBbWidth() + 1.0F) * 0.5 * (double)Mth.cos(param1.yBodyRot * (float) (Math.PI / 180.0))
        );
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 var0 = this.getDeltaMovement();
        HitResult var1 = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (var1 != null) {
            this.onHit(var1);
        }

        double var2 = this.getX() + var0.x;
        double var3 = this.getY() + var0.y;
        double var4 = this.getZ() + var0.z;
        this.updateRotation();
        float var5 = 0.99F;
        float var6 = 0.06F;
        if (this.level.getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
            this.discard();
        } else if (this.isInWaterOrBubble()) {
            this.discard();
        } else {
            this.setDeltaMovement(var0.scale(0.99F));
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.06F, 0.0));
            }

            this.setPos(var2, var3, var4);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        Entity var0 = this.getOwner();
        if (var0 instanceof LivingEntity) {
            param0.getEntity().hurt(DamageSource.indirectMobAttack(this, (LivingEntity)var0).setProjectile(), 1.0F);
        }

    }

    @Override
    protected void onHitBlock(BlockHitResult param0) {
        super.onHitBlock(param0);
        if (!this.level.isClientSide) {
            this.discard();
        }

    }

    @Override
    protected void defineSynchedData() {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket param0) {
        super.recreateFromPacket(param0);
        double var0 = param0.getXa();
        double var1 = param0.getYa();
        double var2 = param0.getZa();

        for(int var3 = 0; var3 < 7; ++var3) {
            double var4 = 0.4 + 0.1 * (double)var3;
            this.level.addParticle(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), var0 * var4, var1, var2 * var4);
        }

        this.setDeltaMovement(var0, var1, var2);
    }
}

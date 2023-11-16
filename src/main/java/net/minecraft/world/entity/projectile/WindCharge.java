package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WindCharge extends AbstractHurtingProjectile implements ItemSupplier {
    public WindCharge(EntityType<? extends WindCharge> param0, Level param1) {
        super(param0, param1);
    }

    public WindCharge(EntityType<? extends WindCharge> param0, Breeze param1, Level param2) {
        super(param0, param1.getX(), param1.getSnoutYPosition(), param1.getZ(), param2);
        this.setOwner(param1);
    }

    @Override
    protected AABB makeBoundingBox() {
        float var0 = this.getType().getDimensions().width / 2.0F;
        float var1 = this.getType().getDimensions().height;
        float var2 = 0.15F;
        return new AABB(
            this.position().x - (double)var0,
            this.position().y - 0.15F,
            this.position().z - (double)var0,
            this.position().x + (double)var0,
            this.position().y - 0.15F + (double)var1,
            this.position().z + (double)var0
        );
    }

    @Override
    protected float getEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.0F;
    }

    @Override
    public boolean canCollideWith(Entity param0) {
        return param0 instanceof WindCharge ? false : super.canCollideWith(param0);
    }

    @Override
    protected boolean canHitEntity(Entity param0) {
        return param0 instanceof WindCharge ? false : super.canHitEntity(param0);
    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        if (!this.level().isClientSide) {
            Entity var10000 = param0.getEntity();
            Entity var3 = this.getOwner();
            var10000.hurt(this.damageSources().mobProjectile(this, var3 instanceof LivingEntity var0 ? var0 : null), 1.0F);
            this.explode();
        }
    }

    private void explode() {
        this.level()
            .explode(
                this,
                null,
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                (float)(3.0 + this.random.nextDouble()),
                false,
                Level.ExplosionInteraction.BLOW,
                ParticleTypes.GUST,
                ParticleTypes.GUST_EMITTER,
                SoundEvents.WIND_BURST
            );
    }

    @Override
    protected void onHitBlock(BlockHitResult param0) {
        super.onHitBlock(param0);
        this.explode();
        this.discard();
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        if (!this.level().isClientSide) {
            this.discard();
        }

    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Nullable
    @Override
    protected ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    protected ClipContext.Block getClipType() {
        return ClipContext.Block.OUTLINE;
    }
}

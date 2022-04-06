package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class MinecartTNT extends AbstractMinecart {
    private static final byte EVENT_PRIME = 10;
    private int fuse = -1;

    public MinecartTNT(EntityType<? extends MinecartTNT> param0, Level param1) {
        super(param0, param1);
    }

    public MinecartTNT(Level param0, double param1, double param2, double param3) {
        super(EntityType.TNT_MINECART, param0, param1, param2, param3);
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.TNT;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.TNT.defaultBlockState();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.fuse > 0) {
            --this.fuse;
            this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.fuse == 0) {
            this.explode(this.getDeltaMovement().horizontalDistanceSqr());
        }

        if (this.horizontalCollision) {
            double var0 = this.getDeltaMovement().horizontalDistanceSqr();
            if (var0 >= 0.01F) {
                this.explode(var0);
            }
        }

    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        Entity var0 = param0.getDirectEntity();
        if (var0 instanceof AbstractArrow var1 && var1.isOnFire()) {
            this.explode(var1.getDeltaMovement().lengthSqr());
        }

        return super.hurt(param0, param1);
    }

    @Override
    public void destroy(DamageSource param0) {
        double var0 = this.getDeltaMovement().horizontalDistanceSqr();
        if (!param0.isFire() && !param0.isExplosion() && !(var0 >= 0.01F)) {
            super.destroy(param0);
        } else {
            if (this.fuse < 0) {
                this.primeFuse();
                this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
            }

        }
    }

    @Override
    protected Item getDropItem() {
        return Items.TNT_MINECART;
    }

    protected void explode(double param0) {
        if (!this.level.isClientSide) {
            double var0 = Math.sqrt(param0);
            if (var0 > 5.0) {
                var0 = 5.0;
            }

            this.level
                .explode(this, this.getX(), this.getY(), this.getZ(), (float)(4.0 + this.random.nextDouble() * 1.5 * var0), Explosion.BlockInteraction.BREAK);
            this.discard();
        }

    }

    @Override
    public boolean causeFallDamage(float param0, float param1, DamageSource param2) {
        if (param0 >= 3.0F) {
            float var0 = param0 / 10.0F;
            this.explode((double)(var0 * var0));
        }

        return super.causeFallDamage(param0, param1, param2);
    }

    @Override
    public void activateMinecart(int param0, int param1, int param2, boolean param3) {
        if (param3 && this.fuse < 0) {
            this.primeFuse();
        }

    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 10) {
            this.primeFuse();
        } else {
            super.handleEntityEvent(param0);
        }

    }

    public void primeFuse() {
        this.fuse = 80;
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte)10);
            if (!this.isSilent()) {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

    }

    public int getFuse() {
        return this.fuse;
    }

    public boolean isPrimed() {
        return this.fuse > -1;
    }

    @Override
    public float getBlockExplosionResistance(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, FluidState param4, float param5) {
        return !this.isPrimed() || !param3.is(BlockTags.RAILS) && !param1.getBlockState(param2.above()).is(BlockTags.RAILS)
            ? super.getBlockExplosionResistance(param0, param1, param2, param3, param4, param5)
            : 0.0F;
    }

    @Override
    public boolean shouldBlockExplode(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, float param4) {
        return !this.isPrimed() || !param3.is(BlockTags.RAILS) && !param1.getBlockState(param2.above()).is(BlockTags.RAILS)
            ? super.shouldBlockExplode(param0, param1, param2, param3, param4)
            : false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("TNTFuse", 99)) {
            this.fuse = param0.getInt("TNTFuse");
        }

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("TNTFuse", this.fuse);
    }
}

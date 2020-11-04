package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Snowball extends ThrowableItemProjectile {
    public Snowball(EntityType<? extends Snowball> param0, Level param1) {
        super(param0, param1);
    }

    public Snowball(Level param0, LivingEntity param1) {
        super(EntityType.SNOWBALL, param1, param0);
    }

    public Snowball(Level param0, double param1, double param2, double param3) {
        super(EntityType.SNOWBALL, param1, param2, param3, param0);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @OnlyIn(Dist.CLIENT)
    private ParticleOptions getParticle() {
        ItemStack var0 = this.getItemRaw();
        return (ParticleOptions)(var0.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, var0));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 3) {
            ParticleOptions var0 = this.getParticle();

            for(int var1 = 0; var1 < 8; ++var1) {
                this.level.addParticle(var0, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }

    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        Entity var0 = param0.getEntity();
        int var1 = var0 instanceof Blaze ? 3 : 0;
        var0.hurt(DamageSource.thrown(this, this.getOwner()), (float)var1);
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte)3);
            this.discard();
        }

    }
}

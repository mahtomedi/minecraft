package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ThrownEgg extends ThrowableItemProjectile {
    public ThrownEgg(EntityType<? extends ThrownEgg> param0, Level param1) {
        super(param0, param1);
    }

    public ThrownEgg(Level param0, LivingEntity param1) {
        super(EntityType.EGG, param1, param0);
    }

    public ThrownEgg(Level param0, double param1, double param2, double param3) {
        super(EntityType.EGG, param1, param2, param3, param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 3) {
            double var0 = 0.08;

            for(int var1 = 0; var1 < 8; ++var1) {
                this.level
                    .addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, this.getItem()),
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        ((double)this.random.nextFloat() - 0.5) * 0.08,
                        ((double)this.random.nextFloat() - 0.5) * 0.08,
                        ((double)this.random.nextFloat() - 0.5) * 0.08
                    );
            }
        }

    }

    @Override
    protected void onHit(HitResult param0) {
        if (param0.getType() == HitResult.Type.ENTITY) {
            ((EntityHitResult)param0).getEntity().hurt(DamageSource.thrown(this, this.getOwner()), 0.0F);
        }

        if (!this.level.isClientSide) {
            if (this.random.nextInt(8) == 0) {
                int var0 = 1;
                if (this.random.nextInt(32) == 0) {
                    var0 = 4;
                }

                for(int var1 = 0; var1 < var0; ++var1) {
                    Chicken var2 = EntityType.CHICKEN.create(this.level);
                    var2.setAge(-24000);
                    var2.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
                    this.level.addFreshEntity(var2);
                }
            }

            this.level.broadcastEntityEvent(this, (byte)3);
            this.remove();
        }

    }

    @Override
    protected Item getDefaultItem() {
        return Items.EGG;
    }
}

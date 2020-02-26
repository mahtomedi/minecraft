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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
        HitResult.Type var0 = param0.getType();
        if (var0 == HitResult.Type.ENTITY) {
            ((EntityHitResult)param0).getEntity().hurt(DamageSource.thrown(this, this.getOwner()), 0.0F);
        } else if (var0 == HitResult.Type.BLOCK) {
            BlockHitResult var1 = (BlockHitResult)param0;
            BlockState var2 = this.level.getBlockState(var1.getBlockPos());
            var2.onProjectileHit(this.level, var2, var1, this);
        }

        if (!this.level.isClientSide) {
            if (this.random.nextInt(8) == 0) {
                int var3 = 1;
                if (this.random.nextInt(32) == 0) {
                    var3 = 4;
                }

                for(int var4 = 0; var4 < var3; ++var4) {
                    Chicken var5 = EntityType.CHICKEN.create(this.level);
                    var5.setAge(-24000);
                    var5.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
                    this.level.addFreshEntity(var5);
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

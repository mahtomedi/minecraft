package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEnderpearl extends ThrowableItemProjectile {
    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> param0, Level param1) {
        super(param0, param1);
    }

    public ThrownEnderpearl(Level param0, LivingEntity param1) {
        super(EntityType.ENDER_PEARL, param1, param0);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        param0.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);

        for(int var0 = 0; var0 < 32; ++var0) {
            this.level
                .addParticle(
                    ParticleTypes.PORTAL,
                    this.getX(),
                    this.getY() + this.random.nextDouble() * 2.0,
                    this.getZ(),
                    this.random.nextGaussian(),
                    0.0,
                    this.random.nextGaussian()
                );
        }

        if (!this.level.isClientSide && !this.isRemoved()) {
            Entity var1 = this.getOwner();
            if (var1 instanceof ServerPlayer var2) {
                if (var2.connection.getConnection().isConnected() && var2.level == this.level && !var2.isSleeping()) {
                    if (this.random.nextFloat() < 0.05F && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        Endermite var3 = EntityType.ENDERMITE.create(this.level);
                        var3.moveTo(var1.getX(), var1.getY(), var1.getZ(), var1.getYRot(), var1.getXRot());
                        this.level.addFreshEntity(var3);
                    }

                    if (var1.isPassenger()) {
                        var2.dismountTo(this.getX(), this.getY(), this.getZ());
                    } else {
                        var1.teleportTo(this.getX(), this.getY(), this.getZ());
                    }

                    var1.fallDistance = 0.0F;
                    var1.hurt(DamageSource.FALL, 5.0F);
                }
            } else if (var1 != null) {
                var1.teleportTo(this.getX(), this.getY(), this.getZ());
                var1.fallDistance = 0.0F;
            }

            this.discard();
        }

    }

    @Override
    public void tick() {
        Entity var0 = this.getOwner();
        if (var0 instanceof Player && !var0.isAlive()) {
            this.discard();
        } else {
            super.tick();
        }

    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel param0) {
        Entity var0 = this.getOwner();
        if (var0 != null && var0.level.dimension() != param0.dimension()) {
            this.setOwner(null);
        }

        return super.changeDimension(param0);
    }
}

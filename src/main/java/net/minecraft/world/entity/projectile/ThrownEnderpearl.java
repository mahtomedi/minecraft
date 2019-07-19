package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ThrownEnderpearl extends ThrowableItemProjectile {
    private LivingEntity originalOwner;

    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> param0, Level param1) {
        super(param0, param1);
    }

    public ThrownEnderpearl(Level param0, LivingEntity param1) {
        super(EntityType.ENDER_PEARL, param1, param0);
        this.originalOwner = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public ThrownEnderpearl(Level param0, double param1, double param2, double param3) {
        super(EntityType.ENDER_PEARL, param1, param2, param3, param0);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onHit(HitResult param0) {
        LivingEntity var0 = this.getOwner();
        if (param0.getType() == HitResult.Type.ENTITY) {
            Entity var1 = ((EntityHitResult)param0).getEntity();
            if (var1 == this.originalOwner) {
                return;
            }

            var1.hurt(DamageSource.thrown(this, var0), 0.0F);
        }

        if (param0.getType() == HitResult.Type.BLOCK) {
            BlockPos var2 = ((BlockHitResult)param0).getBlockPos();
            BlockEntity var3 = this.level.getBlockEntity(var2);
            if (var3 instanceof TheEndGatewayBlockEntity) {
                TheEndGatewayBlockEntity var4 = (TheEndGatewayBlockEntity)var3;
                if (var0 != null) {
                    if (var0 instanceof ServerPlayer) {
                        CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)var0, this.level.getBlockState(var2));
                    }

                    var4.teleportEntity(var0);
                    this.remove();
                    return;
                }

                var4.teleportEntity(this);
                return;
            }
        }

        for(int var5 = 0; var5 < 32; ++var5) {
            this.level
                .addParticle(
                    ParticleTypes.PORTAL, this.x, this.y + this.random.nextDouble() * 2.0, this.z, this.random.nextGaussian(), 0.0, this.random.nextGaussian()
                );
        }

        if (!this.level.isClientSide) {
            if (var0 instanceof ServerPlayer) {
                ServerPlayer var6 = (ServerPlayer)var0;
                if (var6.connection.getConnection().isConnected() && var6.level == this.level && !var6.isSleeping()) {
                    if (this.random.nextFloat() < 0.05F && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        Endermite var7 = EntityType.ENDERMITE.create(this.level);
                        var7.setPlayerSpawned(true);
                        var7.moveTo(var0.x, var0.y, var0.z, var0.yRot, var0.xRot);
                        this.level.addFreshEntity(var7);
                    }

                    if (var0.isPassenger()) {
                        var0.stopRiding();
                    }

                    var0.teleportTo(this.x, this.y, this.z);
                    var0.fallDistance = 0.0F;
                    var0.hurt(DamageSource.FALL, 5.0F);
                }
            } else if (var0 != null) {
                var0.teleportTo(this.x, this.y, this.z);
                var0.fallDistance = 0.0F;
            }

            this.remove();
        }

    }

    @Override
    public void tick() {
        LivingEntity var0 = this.getOwner();
        if (var0 != null && var0 instanceof Player && !var0.isAlive()) {
            this.remove();
        } else {
            super.tick();
        }

    }

    @Nullable
    @Override
    public Entity changeDimension(DimensionType param0) {
        if (this.owner.dimension != param0) {
            this.owner = null;
        }

        return super.changeDimension(param0);
    }
}

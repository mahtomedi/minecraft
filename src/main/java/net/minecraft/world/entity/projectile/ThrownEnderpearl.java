package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
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
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        Entity var0 = param0.getEntity();
        if (var0 != this.originalOwner) {
            var0.hurt(DamageSource.thrown(this, this.getOwner()), 0.0F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult param0) {
        super.onHitBlock(param0);
        Entity var0 = this.getOwner();
        BlockPos var1 = param0.getBlockPos();
        BlockEntity var2 = this.level.getBlockEntity(var1);
        if (var2 instanceof TheEndGatewayBlockEntity) {
            TheEndGatewayBlockEntity var3 = (TheEndGatewayBlockEntity)var2;
            if (var0 != null) {
                if (var0 instanceof ServerPlayer) {
                    CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)var0, this.level.getBlockState(var1));
                }

                var3.teleportEntity(var0);
                this.remove();
            } else {
                var3.teleportEntity(this);
            }
        }

    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        Entity var0 = this.getOwner();

        for(int var1 = 0; var1 < 32; ++var1) {
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

        if (!this.level.isClientSide && !this.removed) {
            if (var0 instanceof ServerPlayer) {
                ServerPlayer var2 = (ServerPlayer)var0;
                if (var2.connection.getConnection().isConnected() && var2.level == this.level && !var2.isSleeping()) {
                    if (this.random.nextFloat() < 0.05F && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        Endermite var3 = EntityType.ENDERMITE.create(this.level);
                        var3.setPlayerSpawned(true);
                        var3.moveTo(var0.getX(), var0.getY(), var0.getZ(), var0.yRot, var0.xRot);
                        this.level.addFreshEntity(var3);
                    }

                    if (var0.isPassenger()) {
                        var0.stopRiding();
                    }

                    var0.teleportTo(this.getX(), this.getY(), this.getZ());
                    var0.fallDistance = 0.0F;
                    var0.hurt(DamageSource.FALL, 5.0F);
                }
            } else if (var0 != null) {
                var0.teleportTo(this.getX(), this.getY(), this.getZ());
                var0.fallDistance = 0.0F;
            }

            this.remove();
        }

    }

    @Override
    public void tick() {
        Entity var0 = this.getOwner();
        if (var0 != null && var0 instanceof Player && !var0.isAlive()) {
            this.remove();
        } else {
            super.tick();
        }

    }

    @Nullable
    @Override
    public Entity changeDimension(ResourceKey<DimensionType> param0) {
        Entity var0 = this.getOwner();
        if (var0 != null && var0.level.dimension() != param0) {
            this.setOwner(null);
        }

        return super.changeDimension(param0);
    }
}

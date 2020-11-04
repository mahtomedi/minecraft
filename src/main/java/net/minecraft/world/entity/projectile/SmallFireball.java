package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SmallFireball extends Fireball {
    public SmallFireball(EntityType<? extends SmallFireball> param0, Level param1) {
        super(param0, param1);
    }

    public SmallFireball(Level param0, LivingEntity param1, double param2, double param3, double param4) {
        super(EntityType.SMALL_FIREBALL, param1, param2, param3, param4, param0);
    }

    public SmallFireball(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(EntityType.SMALL_FIREBALL, param1, param2, param3, param4, param5, param6, param0);
    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        if (!this.level.isClientSide) {
            Entity var0 = param0.getEntity();
            if (!var0.fireImmune()) {
                Entity var1 = this.getOwner();
                int var2 = var0.getRemainingFireTicks();
                var0.setSecondsOnFire(5);
                boolean var3 = var0.hurt(DamageSource.fireball(this, var1), 5.0F);
                if (!var3) {
                    var0.setRemainingFireTicks(var2);
                } else if (var1 instanceof LivingEntity) {
                    this.doEnchantDamageEffects((LivingEntity)var1, var0);
                }
            }

        }
    }

    @Override
    protected void onHitBlock(BlockHitResult param0) {
        super.onHitBlock(param0);
        if (!this.level.isClientSide) {
            Entity var0 = this.getOwner();
            if (var0 == null || !(var0 instanceof Mob) || this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                BlockPos var2 = param0.getBlockPos().relative(param0.getDirection());
                if (this.level.isEmptyBlock(var2)) {
                    this.level.setBlockAndUpdate(var2, BaseFireBlock.getState(this.level, var2));
                }
            }

        }
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        if (!this.level.isClientSide) {
            this.discard();
        }

    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        return false;
    }
}

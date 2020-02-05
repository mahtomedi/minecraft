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
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        if (!this.level.isClientSide) {
            if (param0.getType() == HitResult.Type.ENTITY) {
                Entity var0 = ((EntityHitResult)param0).getEntity();
                if (!var0.fireImmune()) {
                    int var1 = var0.getRemainingFireTicks();
                    var0.setSecondsOnFire(5);
                    boolean var2 = var0.hurt(DamageSource.fireball(this, this.owner), 5.0F);
                    if (var2) {
                        this.doEnchantDamageEffects(this.owner, var0);
                    } else {
                        var0.setRemainingFireTicks(var1);
                    }
                }
            } else if (this.owner == null || !(this.owner instanceof Mob) || this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                BlockHitResult var3 = (BlockHitResult)param0;
                BlockPos var4 = var3.getBlockPos().relative(var3.getDirection());
                if (this.level.isEmptyBlock(var4)) {
                    this.level.setBlockAndUpdate(var4, BaseFireBlock.getState(this.level, var4));
                }
            }

            this.remove();
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

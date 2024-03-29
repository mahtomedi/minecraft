package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class Stray extends AbstractSkeleton {
    public Stray(EntityType<? extends Stray> param0, Level param1) {
        super(param0, param1);
    }

    public static boolean checkStraySpawnRules(EntityType<Stray> param0, ServerLevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4) {
        BlockPos var0 = param3;

        do {
            var0 = var0.above();
        } while(param1.getBlockState(var0).is(Blocks.POWDER_SNOW));

        return checkMonsterSpawnRules(param0, param1, param2, param3, param4) && (MobSpawnType.isSpawner(param2) || param1.canSeeSky(var0.below()));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRAY_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.STRAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRAY_DEATH;
    }

    @Override
    SoundEvent getStepSound() {
        return SoundEvents.STRAY_STEP;
    }

    @Override
    protected AbstractArrow getArrow(ItemStack param0, float param1) {
        AbstractArrow var0 = super.getArrow(param0, param1);
        if (var0 instanceof Arrow) {
            ((Arrow)var0).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600));
        }

        return var0;
    }
}

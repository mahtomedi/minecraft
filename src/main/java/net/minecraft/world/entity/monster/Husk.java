package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Husk extends Zombie {
    public Husk(EntityType<? extends Husk> param0, Level param1) {
        super(param0, param1);
    }

    public static boolean checkHuskSpawnRules(EntityType<Husk> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return checkMonsterSpawnRules(param0, param1, param2, param3, param4) && (param2 == MobSpawnType.SPAWNER || param1.canSeeSky(param3));
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.HUSK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HUSK_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.HUSK_STEP;
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        boolean var0 = super.doHurtTarget(param0);
        if (var0 && this.getMainHandItem().isEmpty() && param0 instanceof LivingEntity) {
            float var1 = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            ((LivingEntity)param0).addEffect(new MobEffectInstance(MobEffects.HUNGER, 140 * (int)var1));
        }

        return var0;
    }

    @Override
    protected boolean convertsInWater() {
        return true;
    }

    @Override
    protected void doUnderWaterConversion() {
        this.convertTo(EntityType.ZOMBIE);
        this.level.levelEvent(null, 1041, this.blockPosition(), 0);
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }
}

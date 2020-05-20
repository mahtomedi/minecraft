package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Mule extends AbstractChestedHorse {
    public Mule(EntityType<? extends Mule> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.MULE_AMBIENT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        super.getAngrySound();
        return SoundEvents.MULE_ANGRY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.MULE_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.MULE_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        super.getHurtSound(param0);
        return SoundEvents.MULE_HURT;
    }

    @Override
    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.MULE_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    @Override
    public AgableMob getBreedOffspring(AgableMob param0) {
        return EntityType.MULE.create(this.level);
    }
}

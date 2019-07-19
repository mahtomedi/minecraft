package net.minecraft.world.entity.animal.horse;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public class Donkey extends AbstractChestedHorse {
    public Donkey(EntityType<? extends Donkey> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.DONKEY_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.DONKEY_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        super.getHurtSound(param0);
        return SoundEvents.DONKEY_HURT;
    }

    @Override
    public boolean canMate(Animal param0) {
        if (param0 == this) {
            return false;
        } else if (!(param0 instanceof Donkey) && !(param0 instanceof Horse)) {
            return false;
        } else {
            return this.canParent() && ((AbstractHorse)param0).canParent();
        }
    }

    @Override
    public AgableMob getBreedOffspring(AgableMob param0) {
        EntityType<? extends AbstractHorse> var0 = param0 instanceof Horse ? EntityType.MULE : EntityType.DONKEY;
        AbstractHorse var1 = var0.create(this.level);
        this.setOffspringAttributes(param0, var1);
        return var1;
    }
}

package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public class Donkey extends AbstractChestedHorse {
    public Donkey(EntityType<? extends Donkey> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.DONKEY_AMBIENT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.DONKEY_ANGRY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DONKEY_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.DONKEY_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
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

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        EntityType<? extends AbstractHorse> var0 = param1 instanceof Horse ? EntityType.MULE : EntityType.DONKEY;
        AbstractHorse var1 = var0.create(param0);
        if (var1 != null) {
            this.setOffspringAttributes(param1, var1);
        }

        return var1;
    }
}

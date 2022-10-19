package net.minecraft.world.entity.ai.goal;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class RandomStandGoal extends Goal {
    private final AbstractHorse horse;
    private int nextStand;

    public RandomStandGoal(AbstractHorse param0) {
        this.horse = param0;
        this.resetStandInterval(param0);
    }

    @Override
    public void start() {
        this.horse.standIfPossible();
        this.playStandSound();
    }

    private void playStandSound() {
        SoundEvent var0 = this.horse.getAmbientStandSound();
        if (var0 != null) {
            this.horse.playSound(var0);
        }

    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public boolean canUse() {
        ++this.nextStand;
        if (this.nextStand > 0 && this.horse.getRandom().nextInt(1000) < this.nextStand) {
            this.resetStandInterval(this.horse);
            return !this.horse.isImmobile() && this.horse.getRandom().nextInt(10) == 0;
        } else {
            return false;
        }
    }

    private void resetStandInterval(AbstractHorse param0) {
        this.nextStand = -param0.getAmbientStandInterval();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}

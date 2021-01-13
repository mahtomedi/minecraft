package net.minecraft.world.entity.ai.goal;

import java.util.List;
import net.minecraft.world.entity.animal.Animal;

public class FollowParentGoal extends Goal {
    private final Animal animal;
    private Animal parent;
    private final double speedModifier;
    private int timeToRecalcPath;

    public FollowParentGoal(Animal param0, double param1) {
        this.animal = param0;
        this.speedModifier = param1;
    }

    @Override
    public boolean canUse() {
        if (this.animal.getAge() >= 0) {
            return false;
        } else {
            List<Animal> var0 = this.animal.level.getEntitiesOfClass(this.animal.getClass(), this.animal.getBoundingBox().inflate(8.0, 4.0, 8.0));
            Animal var1 = null;
            double var2 = Double.MAX_VALUE;

            for(Animal var3 : var0) {
                if (var3.getAge() >= 0) {
                    double var4 = this.animal.distanceToSqr(var3);
                    if (!(var4 > var2)) {
                        var2 = var4;
                        var1 = var3;
                    }
                }
            }

            if (var1 == null) {
                return false;
            } else if (var2 < 9.0) {
                return false;
            } else {
                this.parent = var1;
                return true;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.animal.getAge() >= 0) {
            return false;
        } else if (!this.parent.isAlive()) {
            return false;
        } else {
            double var0 = this.animal.distanceToSqr(this.parent);
            return !(var0 < 9.0) && !(var0 > 256.0);
        }
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.parent = null;
    }

    @Override
    public void tick() {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            this.animal.getNavigation().moveTo(this.parent, this.speedModifier);
        }
    }
}

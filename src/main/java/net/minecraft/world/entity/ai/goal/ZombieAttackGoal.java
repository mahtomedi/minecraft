package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.monster.Zombie;

public class ZombieAttackGoal extends MeleeAttackGoal {
    private final Zombie zombie;
    private int raiseArmTicks;

    public ZombieAttackGoal(Zombie param0, double param1, boolean param2) {
        super(param0, param1, param2);
        this.zombie = param0;
    }

    @Override
    public void start() {
        super.start();
        this.raiseArmTicks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.zombie.setAggressive(false);
    }

    @Override
    public void tick() {
        super.tick();
        ++this.raiseArmTicks;
        if (this.raiseArmTicks >= 5 && this.getTicksUntilNextAttack() < this.getAttackInterval() / 2) {
            this.zombie.setAggressive(true);
        } else {
            this.zombie.setAggressive(false);
        }

    }
}

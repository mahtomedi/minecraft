package net.minecraft.client.model;

import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractZombieModel<T extends Monster> extends HumanoidModel<T> {
    protected AbstractZombieModel(float param0, float param1, int param2, int param3) {
        super(param0, param1, param2, param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, this.isAggressive(param0), this.attackTime, param3);
    }

    public abstract boolean isAggressive(T var1);
}

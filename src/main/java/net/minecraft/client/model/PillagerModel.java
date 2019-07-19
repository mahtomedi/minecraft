package net.minecraft.client.model;

import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PillagerModel<T extends AbstractIllager> extends IllagerModel<T> {
    public PillagerModel(float param0, float param1, int param2, int param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.head.render(param6);
        this.body.render(param6);
        this.leftLeg.render(param6);
        this.rightLeg.render(param6);
        this.rightArm.render(param6);
        this.leftArm.render(param6);
    }
}

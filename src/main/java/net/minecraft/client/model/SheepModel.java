package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepModel<T extends Sheep> extends QuadrupedModel<T> {
    private float headXRot;

    public SheepModel() {
        super(12, 0.0F, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, 0.0F);
        this.head.setPos(0.0F, 6.0F, -8.0F);
        this.body = new ModelPart(this, 28, 8);
        this.body.addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, 0.0F);
        this.body.setPos(0.0F, 5.0F, 2.0F);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        this.head.y = 6.0F + param0.getHeadEatPositionScale(param3) * 9.0F;
        this.headXRot = param0.getHeadEatAngleScale(param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        this.head.xRot = this.headXRot;
    }
}

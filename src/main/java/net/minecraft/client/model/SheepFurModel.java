package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepFurModel<T extends Sheep> extends QuadrupedModel<T> {
    private float headXRot;

    public SheepFurModel() {
        super(12, 0.0F, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, 0.6F);
        this.head.setPos(0.0F, 6.0F, -8.0F);
        this.body = new ModelPart(this, 28, 8);
        this.body.addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, 1.75F);
        this.body.setPos(0.0F, 5.0F, 2.0F);
        float var0 = 0.5F;
        this.leg0 = new ModelPart(this, 0, 16);
        this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.5F);
        this.leg0.setPos(-3.0F, 12.0F, 7.0F);
        this.leg1 = new ModelPart(this, 0, 16);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.5F);
        this.leg1.setPos(3.0F, 12.0F, 7.0F);
        this.leg2 = new ModelPart(this, 0, 16);
        this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.5F);
        this.leg2.setPos(-3.0F, 12.0F, -5.0F);
        this.leg3 = new ModelPart(this, 0, 16);
        this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.5F);
        this.leg3.setPos(3.0F, 12.0F, -5.0F);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        this.head.y = 6.0F + param0.getHeadEatPositionScale(param3) * 9.0F;
        this.headXRot = param0.getHeadEatAngleScale(param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.head.xRot = this.headXRot;
    }
}

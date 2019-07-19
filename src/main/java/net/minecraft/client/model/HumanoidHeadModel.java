package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidHeadModel extends SkullModel {
    private final ModelPart hat = new ModelPart(this, 32, 0);

    public HumanoidHeadModel() {
        super(0, 0, 64, 64);
        this.hat.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.25F);
        this.hat.setPos(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(float param0, float param1, float param2, float param3, float param4, float param5) {
        super.render(param0, param1, param2, param3, param4, param5);
        this.hat.yRot = this.head.yRot;
        this.hat.xRot = this.head.xRot;
        this.hat.render(param5);
    }
}

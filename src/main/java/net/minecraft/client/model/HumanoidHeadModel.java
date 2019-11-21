package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidHeadModel extends SkullModel {
    private final ModelPart hat = new ModelPart(this, 32, 0);

    public HumanoidHeadModel() {
        super(0, 0, 64, 64);
        this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.25F);
        this.hat.setPos(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void setupAnim(float param0, float param1, float param2) {
        super.setupAnim(param0, param1, param2);
        this.hat.yRot = this.head.yRot;
        this.hat.xRot = this.head.xRot;
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        super.renderToBuffer(param0, param1, param2, param3, param4, param5, param6, param7);
        this.hat.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }
}

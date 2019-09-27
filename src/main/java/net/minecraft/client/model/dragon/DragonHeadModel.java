package net.minecraft.client.model.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DragonHeadModel extends SkullModel {
    private final ModelPart head;
    private final ModelPart jaw;

    public DragonHeadModel(float param0) {
        this.texWidth = 256;
        this.texHeight = 256;
        float var0 = -16.0F;
        this.head = new ModelPart(this);
        this.head.addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, param0, 176, 44);
        this.head.addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, param0, 112, 30);
        this.head.mirror = true;
        this.head.addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, param0, 0, 0);
        this.head.addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, param0, 112, 0);
        this.head.mirror = false;
        this.head.addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, param0, 0, 0);
        this.head.addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, param0, 112, 0);
        this.jaw = new ModelPart(this);
        this.jaw.setPos(0.0F, 4.0F, -8.0F);
        this.jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, param0, 176, 65);
        this.head.addChild(this.jaw);
    }

    @Override
    public void render(PoseStack param0, VertexConsumer param1, float param2, float param3, float param4, float param5, int param6) {
        this.jaw.xRot = (float)(Math.sin((double)(param2 * (float) Math.PI * 0.2F)) + 1.0) * 0.2F;
        this.head.yRot = param3 * (float) (Math.PI / 180.0);
        this.head.xRot = param4 * (float) (Math.PI / 180.0);
        param0.pushPose();
        param0.translate(0.0, -0.374375F, 0.0);
        param0.scale(0.75F, 0.75F, 0.75F);
        this.head.render(param0, param1, param5, param6, null);
        param0.popPose();
    }
}

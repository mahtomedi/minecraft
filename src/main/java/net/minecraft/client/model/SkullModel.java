package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullModel extends Model {
    protected final ModelPart head;

    public SkullModel() {
        this(0, 35, 64, 64);
    }

    public SkullModel(int param0, int param1, int param2, int param3) {
        super(RenderType::entityTranslucent);
        this.texWidth = param2;
        this.texHeight = param3;
        this.head = new ModelPart(this, param0, param1);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F);
        this.head.setPos(0.0F, 0.0F, 0.0F);
    }

    public void setupAnim(float param0, float param1, float param2) {
        this.head.yRot = param1 * (float) (Math.PI / 180.0);
        this.head.xRot = param2 * (float) (Math.PI / 180.0);
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6) {
        this.head.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6);
    }
}

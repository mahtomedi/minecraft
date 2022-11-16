package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinHeadModel extends SkullModelBase {
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;

    public PiglinHeadModel(ModelPart param0) {
        this.head = param0.getChild("head");
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");
    }

    public static MeshDefinition createHeadModel() {
        MeshDefinition var0 = new MeshDefinition();
        PiglinModel.addHead(CubeDeformation.NONE, var0);
        return var0;
    }

    @Override
    public void setupAnim(float param0, float param1, float param2) {
        this.head.yRot = param1 * (float) (Math.PI / 180.0);
        this.head.xRot = param2 * (float) (Math.PI / 180.0);
        float var0 = 1.2F;
        this.leftEar.zRot = (float)(-(Math.cos((double)(param0 * (float) Math.PI * 0.2F * 1.2F)) + 2.5)) * 0.2F;
        this.rightEar.zRot = (float)(Math.cos((double)(param0 * (float) Math.PI * 0.2F)) + 2.5) * 0.2F;
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        this.head.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }
}

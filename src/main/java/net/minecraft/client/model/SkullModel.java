package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullModel extends SkullModelBase {
    private final ModelPart root;
    protected final ModelPart head;

    public SkullModel(ModelPart param0) {
        this.root = param0;
        this.head = param0.getChild("head");
    }

    public static MeshDefinition createHeadModel() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return var0;
    }

    public static LayerDefinition createHumanoidHeadLayer() {
        MeshDefinition var0 = createHeadModel();
        PartDefinition var1 = var0.getRoot();
        var1.getChild("head")
            .addOrReplaceChild(
                "hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.ZERO
            );
        return LayerDefinition.create(var0, 64, 64);
    }

    public static LayerDefinition createMobHeadLayer() {
        MeshDefinition var0 = createHeadModel();
        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public void setupAnim(float param0, float param1, float param2) {
        this.head.yRot = param1 * (float) (Math.PI / 180.0);
        this.head.xRot = param2 * (float) (Math.PI / 180.0);
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        this.root.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }
}

package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BookModel extends Model {
    private final ModelPart root;
    private final ModelPart leftLid;
    private final ModelPart rightLid;
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart flipPage1;
    private final ModelPart flipPage2;

    public BookModel(ModelPart param0) {
        super(RenderType::entitySolid);
        this.root = param0;
        this.leftLid = param0.getChild("left_lid");
        this.rightLid = param0.getChild("right_lid");
        this.leftPages = param0.getChild("left_pages");
        this.rightPages = param0.getChild("right_pages");
        this.flipPage1 = param0.getChild("flip_page1");
        this.flipPage2 = param0.getChild("flip_page2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, -1.0F)
        );
        var1.addOrReplaceChild(
            "right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, 1.0F)
        );
        var1.addOrReplaceChild(
            "seam",
            CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F),
            PartPose.rotation(0.0F, (float) (Math.PI / 2), 0.0F)
        );
        var1.addOrReplaceChild("left_pages", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
        var1.addOrReplaceChild("right_pages", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
        CubeListBuilder var2 = CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
        var1.addOrReplaceChild("flip_page1", var2, PartPose.ZERO);
        var1.addOrReplaceChild("flip_page2", var2, PartPose.ZERO);
        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        this.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public void render(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        this.root.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public void setupAnim(float param0, float param1, float param2, float param3) {
        float var0 = (Mth.sin(param0 * 0.02F) * 0.1F + 1.25F) * param3;
        this.leftLid.yRot = (float) Math.PI + var0;
        this.rightLid.yRot = -var0;
        this.leftPages.yRot = var0;
        this.rightPages.yRot = -var0;
        this.flipPage1.yRot = var0 - var0 * 2.0F * param1;
        this.flipPage2.yRot = var0 - var0 * 2.0F * param2;
        this.leftPages.x = Mth.sin(var0);
        this.rightPages.x = Mth.sin(var0);
        this.flipPage1.x = Mth.sin(var0);
        this.flipPage2.x = Mth.sin(var0);
    }
}

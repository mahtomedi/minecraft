package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllayModel extends HierarchicalModel<Allay> implements ArmedModel {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;

    public AllayModel(ModelPart param0) {
        this.root = param0.getChild("root");
        this.body = this.root.getChild("body");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.right_wing = this.body.getChild("right_wing");
        this.left_wing = this.body.getChild("left_wing");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 23.5F, 0.0F));
        var2.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -3.99F, 0.0F)
        );
        PartDefinition var3 = var2.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 10)
                .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16)
                .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)),
            PartPose.offset(0.0F, -4.0F, 0.0F)
        );
        var3.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create().texOffs(23, 0).addBox(-0.75F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)),
            PartPose.offset(-1.75F, 0.5F, 0.0F)
        );
        var3.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create().texOffs(23, 6).addBox(-0.25F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)),
            PartPose.offset(1.75F, 0.5F, 0.0F)
        );
        var3.addOrReplaceChild(
            "right_wing",
            CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-0.5F, 0.0F, 0.65F)
        );
        var3.addOrReplaceChild(
            "left_wing",
            CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.5F, 0.0F, 0.65F)
        );
        return LayerDefinition.create(var0, 32, 32);
    }

    public void setupAnim(Allay param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = param3 * 20.0F * (float) (Math.PI / 180.0) + param2;
        this.right_wing.xRot = 0.43633232F;
        this.right_wing.yRot = -0.61086524F + Mth.cos(var0) * (float) Math.PI * 0.15F;
        this.left_wing.xRot = 0.43633232F;
        this.left_wing.yRot = 0.61086524F - Mth.cos(var0) * (float) Math.PI * 0.15F;
        if (this.isIdle(param2)) {
            float var1 = param3 * 9.0F * (float) (Math.PI / 180.0);
            this.root.y = 23.5F + Mth.cos(var1) * 0.25F;
            this.right_arm.zRot = 0.43633232F - Mth.cos(var1 + (float) (Math.PI * 3.0 / 2.0)) * (float) Math.PI * 0.075F;
            this.left_arm.zRot = -0.43633232F + Mth.cos(var1 + (float) (Math.PI * 3.0 / 2.0)) * (float) Math.PI * 0.075F;
        } else {
            this.root.y = 23.5F;
            this.right_arm.zRot = 0.43633232F;
            this.left_arm.zRot = -0.43633232F;
        }

        if (!param0.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            this.right_arm.xRot = -1.134464F;
            this.right_arm.yRot = 0.27925268F;
            this.right_arm.zRot = (float) (-Math.PI / 180.0);
            this.left_arm.xRot = -1.134464F;
            this.left_arm.yRot = (float) (-Math.PI / 15);
            this.left_arm.zRot = (float) (Math.PI / 180.0);
        }

    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        this.root.render(param0, param1, param2, param3);
    }

    public void prepareMobModel(Allay param0, float param1, float param2, float param3) {
        this.right_arm.xRot = 0.0F;
        this.right_arm.yRot = 0.0F;
        this.right_arm.zRot = 0.3927F;
        this.left_arm.xRot = 0.0F;
        this.left_arm.yRot = 0.0F;
        this.left_arm.zRot = -0.3927F;
    }

    @Override
    public void translateToHand(HumanoidArm param0, PoseStack param1) {
        param1.scale(0.7F, 0.7F, 0.7F);
        float var0 = 1.8F + (this.root.y - 23.5F) / 11.2F;
        param1.translate(0.05F, (double)var0, 0.2F);
        param1.mulPose(Vector3f.XP.rotationDegrees(-65.0F));
    }

    private boolean isIdle(float param0) {
        return param0 == 0.0F;
    }
}

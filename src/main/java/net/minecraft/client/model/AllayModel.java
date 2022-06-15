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
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllayModel extends HierarchicalModel<Allay> implements ArmedModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private static final float FLYING_ANIMATION_X_ROT = (float) (Math.PI * 2.0 / 9.0);
    private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = (float) (-Math.PI / 4);
    private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = (float) (-Math.PI / 3);

    public AllayModel(ModelPart param0) {
        this.root = param0.getChild("root");
        this.head = this.root.getChild("head");
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
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float var0 = param3 * 20.0F * (float) (Math.PI / 180.0) + param2;
        float var1 = Mth.cos(var0) * (float) Math.PI * 0.15F;
        float var2 = param3 - (float)param0.tickCount;
        float var3 = param3 * 9.0F * (float) (Math.PI / 180.0);
        float var4 = Math.min(param2 / 0.3F, 1.0F);
        float var5 = 1.0F - var4;
        float var6 = param0.getHoldingItemAnimationProgress(var2);
        if (param0.isDancing()) {
            float var7 = param3 * 8.0F * (float) (Math.PI / 180.0) + param2;
            float var8 = Mth.cos(var7) * 16.0F * (float) (Math.PI / 180.0);
            float var9 = param0.getSpinningProgress(var2);
            float var10 = Mth.cos(var7) * 14.0F * (float) (Math.PI / 180.0);
            float var11 = Mth.cos(var7) * 30.0F * (float) (Math.PI / 180.0);
            this.root.yRot = param0.isSpinning() ? ((float) (Math.PI * 4)) * var9 : this.root.yRot;
            this.root.zRot = var8 * (1.0F - var9);
            this.head.yRot = var11 * (1.0F - var9);
            this.head.zRot = var10 * (1.0F - var9);
        } else {
            this.head.xRot = param5 * (float) (Math.PI / 180.0);
            this.head.yRot = param4 * (float) (Math.PI / 180.0);
        }

        this.right_wing.xRot = 0.43633232F;
        this.right_wing.yRot = -0.61086524F + var1;
        this.left_wing.xRot = 0.43633232F;
        this.left_wing.yRot = 0.61086524F - var1;
        float var12 = var4 * (float) (Math.PI * 2.0 / 9.0);
        this.body.xRot = var12;
        float var13 = Mth.lerp(var6, var12, Mth.lerp(var4, (float) (-Math.PI / 3), (float) (-Math.PI / 4)));
        this.root.y += (float)Math.cos((double)var3) * 0.25F * var5;
        this.right_arm.xRot = var13;
        this.left_arm.xRot = var13;
        float var14 = var5 * (1.0F - var6);
        float var15 = 0.43633232F - Mth.cos(var3 + (float) (Math.PI * 3.0 / 2.0)) * (float) Math.PI * 0.075F * var14;
        this.left_arm.zRot = -var15;
        this.right_arm.zRot = var15;
        this.right_arm.yRot = 0.27925268F * var6;
        this.left_arm.yRot = -0.27925268F * var6;
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        this.root.render(param0, param1, param2, param3);
    }

    @Override
    public void translateToHand(HumanoidArm param0, PoseStack param1) {
        float var0 = -1.5F;
        float var1 = 1.5F;
        this.root.translateAndRotate(param1);
        this.body.translateAndRotate(param1);
        param1.translate(0.0, -0.09375, 0.09375);
        param1.mulPose(Vector3f.XP.rotation(this.right_arm.xRot + 0.43633232F));
        param1.scale(0.7F, 0.7F, 0.7F);
        param1.translate(0.0625, 0.0, 0.0);
    }
}

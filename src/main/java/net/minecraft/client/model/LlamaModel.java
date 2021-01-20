package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaModel<T extends AbstractChestedHorse> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightChest;
    private final ModelPart leftChest;

    public LlamaModel(ModelPart param0) {
        this.head = param0.getChild("head");
        this.body = param0.getChild("body");
        this.rightChest = param0.getChild("right_chest");
        this.leftChest = param0.getChild("left_chest");
        this.rightHindLeg = param0.getChild("right_hind_leg");
        this.leftHindLeg = param0.getChild("left_hind_leg");
        this.rightFrontLeg = param0.getChild("right_front_leg");
        this.leftFrontLeg = param0.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation param0) {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F, param0)
                .texOffs(0, 14)
                .addBox("neck", -4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F, param0)
                .texOffs(17, 0)
                .addBox("ear", -4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, param0)
                .texOffs(17, 0)
                .addBox("ear", 1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, param0),
            PartPose.offset(0.0F, 7.0F, -6.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(29, 0).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, param0),
            PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_chest",
            CubeListBuilder.create().texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, param0),
            PartPose.offsetAndRotation(-8.5F, 3.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
        );
        var1.addOrReplaceChild(
            "left_chest",
            CubeListBuilder.create().texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, param0),
            PartPose.offsetAndRotation(5.5F, 3.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
        );
        int var2 = 4;
        int var3 = 14;
        CubeListBuilder var4 = CubeListBuilder.create().texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, param0);
        var1.addOrReplaceChild("right_hind_leg", var4, PartPose.offset(-3.5F, 10.0F, 6.0F));
        var1.addOrReplaceChild("left_hind_leg", var4, PartPose.offset(3.5F, 10.0F, 6.0F));
        var1.addOrReplaceChild("right_front_leg", var4, PartPose.offset(-3.5F, 10.0F, -5.0F));
        var1.addOrReplaceChild("left_front_leg", var4, PartPose.offset(3.5F, 10.0F, -5.0F));
        return LayerDefinition.create(var0, 128, 64);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.rightHindLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.leftHindLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.rightFrontLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leftFrontLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        boolean var0 = !param0.isBaby() && param0.hasChest();
        this.rightChest.visible = var0;
        this.leftChest.visible = var0;
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        if (this.young) {
            float var0 = 2.0F;
            param0.pushPose();
            float var1 = 0.7F;
            param0.scale(0.71428573F, 0.64935064F, 0.7936508F);
            param0.translate(0.0, 1.3125, 0.22F);
            this.head.render(param0, param1, param2, param3, param4, param5, param6, param7);
            param0.popPose();
            param0.pushPose();
            float var2 = 1.1F;
            param0.scale(0.625F, 0.45454544F, 0.45454544F);
            param0.translate(0.0, 2.0625, 0.0);
            this.body.render(param0, param1, param2, param3, param4, param5, param6, param7);
            param0.popPose();
            param0.pushPose();
            param0.scale(0.45454544F, 0.41322312F, 0.45454544F);
            param0.translate(0.0, 2.0625, 0.0);
            ImmutableList.of(this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest)
                .forEach(param8 -> param8.render(param0, param1, param2, param3, param4, param5, param6, param7));
            param0.popPose();
        } else {
            ImmutableList.of(this.head, this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest)
                .forEach(param8 -> param8.render(param0, param1, param2, param3, param4, param5, param6, param7));
        }

    }
}

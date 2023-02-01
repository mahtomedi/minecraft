package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.definitions.CamelAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CamelModel<T extends Camel> extends HierarchicalModel<T> {
    private static final float MAX_WALK_ANIMATION_SPEED = 2.0F;
    private static final float WALK_ANIMATION_SCALE_FACTOR = 2.5F;
    private static final String SADDLE = "saddle";
    private static final String BRIDLE = "bridle";
    private static final String REINS = "reins";
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public CamelModel(ModelPart param0) {
        this.root = param0;
        ModelPart var0 = param0.getChild("body");
        this.head = var0.getChild("head");
        this.saddleParts = new ModelPart[]{var0.getChild("saddle"), this.head.getChild("bridle")};
        this.ridingParts = new ModelPart[]{this.head.getChild("reins")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        CubeDeformation var2 = new CubeDeformation(0.1F);
        PartDefinition var3 = var1.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 25).addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F), PartPose.offset(0.0F, 4.0F, 9.5F)
        );
        var3.addOrReplaceChild(
            "hump", CubeListBuilder.create().texOffs(74, 0).addBox(-4.5F, -5.0F, -5.5F, 9.0F, 5.0F, 11.0F), PartPose.offset(0.0F, -12.0F, -10.0F)
        );
        var3.addOrReplaceChild(
            "tail", CubeListBuilder.create().texOffs(122, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 0.0F), PartPose.offset(0.0F, -9.0F, 3.5F)
        );
        PartDefinition var4 = var3.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(60, 24)
                .addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F)
                .texOffs(21, 0)
                .addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F)
                .texOffs(50, 0)
                .addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F),
            PartPose.offset(0.0F, -3.0F, -19.5F)
        );
        var4.addOrReplaceChild(
            "left_ear", CubeListBuilder.create().texOffs(45, 0).addBox(-0.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(3.0F, -21.0F, -9.5F)
        );
        var4.addOrReplaceChild(
            "right_ear", CubeListBuilder.create().texOffs(67, 0).addBox(-2.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(-3.0F, -21.0F, -9.5F)
        );
        var1.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(58, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(4.9F, 1.0F, 9.5F)
        );
        var1.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(94, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(-4.9F, 1.0F, 9.5F)
        );
        var1.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(4.9F, 1.0F, -10.5F)
        );
        var1.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(-4.9F, 1.0F, -10.5F)
        );
        var3.addOrReplaceChild(
            "saddle",
            CubeListBuilder.create()
                .texOffs(74, 64)
                .addBox(-4.5F, -17.0F, -15.5F, 9.0F, 5.0F, 11.0F, var2)
                .texOffs(92, 114)
                .addBox(-3.5F, -20.0F, -15.5F, 7.0F, 3.0F, 11.0F, var2)
                .texOffs(0, 89)
                .addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F, var2),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        var4.addOrReplaceChild(
            "reins",
            CubeListBuilder.create()
                .texOffs(98, 42)
                .addBox(3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F)
                .texOffs(84, 57)
                .addBox(-3.5F, -18.0F, -2.0F, 7.0F, 7.0F, 0.0F)
                .texOffs(98, 42)
                .addBox(-3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        var4.addOrReplaceChild(
            "bridle",
            CubeListBuilder.create()
                .texOffs(60, 87)
                .addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F, var2)
                .texOffs(21, 64)
                .addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F, var2)
                .texOffs(50, 64)
                .addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F, var2)
                .texOffs(74, 70)
                .addBox(2.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F)
                .texOffs(74, 70)
                .mirror()
                .addBox(-3.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 128, 128);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(param0, param4, param5, param3);
        this.toggleInvisibleParts(param0);
        this.animateWalk(CamelAnimation.CAMEL_WALK, param1, param2, 2.0F, 2.5F);
        this.animate(param0.sitAnimationState, CamelAnimation.CAMEL_SIT, param3, 1.0F);
        this.animate(param0.sitPoseAnimationState, CamelAnimation.CAMEL_SIT_POSE, param3, 1.0F);
        this.animate(param0.sitUpAnimationState, CamelAnimation.CAMEL_STANDUP, param3, 1.0F);
        this.animate(param0.idleAnimationState, CamelAnimation.CAMEL_IDLE, param3, 1.0F);
        this.animate(param0.dashAnimationState, CamelAnimation.CAMEL_DASH, param3, 1.0F);
    }

    private void applyHeadRotation(T param0, float param1, float param2, float param3) {
        param1 = Mth.clamp(param1, -30.0F, 30.0F);
        param2 = Mth.clamp(param2, -25.0F, 45.0F);
        if (param0.getJumpCooldown() > 0) {
            float var0 = param3 - (float)param0.tickCount;
            float var1 = 45.0F * ((float)param0.getJumpCooldown() - var0) / 55.0F;
            param2 = Mth.clamp(param2 + var1, -25.0F, 70.0F);
        }

        this.head.yRot = param1 * (float) (Math.PI / 180.0);
        this.head.xRot = param2 * (float) (Math.PI / 180.0);
    }

    private void toggleInvisibleParts(T param0) {
        boolean var0 = param0.isSaddled();
        boolean var1 = param0.isVehicle();

        for(ModelPart var2 : this.saddleParts) {
            var2.visible = var0;
        }

        for(ModelPart var3 : this.ridingParts) {
            var3.visible = var1 && var0;
        }

    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        if (this.young) {
            float var0 = 2.0F;
            float var1 = 1.1F;
            param0.pushPose();
            param0.scale(0.45454544F, 0.41322312F, 0.45454544F);
            param0.translate(0.0F, 2.0625F, 0.0F);
            this.root().render(param0, param1, param2, param3, param4, param5, param6, param7);
            param0.popPose();
        } else {
            this.root().render(param0, param1, param2, param3, param4, param5, param6, param7);
        }

    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

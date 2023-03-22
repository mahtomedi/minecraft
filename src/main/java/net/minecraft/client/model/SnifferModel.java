package net.minecraft.client.model;

import net.minecraft.client.animation.definitions.SnifferAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnifferModel<T extends Sniffer> extends AgeableHierarchicalModel<T> {
    private static final float WALK_ANIMATION_SPEED_FACTOR = 9000.0F;
    private static final float WALK_ANIMATION_SPEED_MAX = 2.0F;
    private static final float SEARCHING_ANIMATION_SPEED_MAX = 1.0F;
    private final ModelPart root;
    private final ModelPart head;

    public SnifferModel(ModelPart param0) {
        super(0.5F, 24.0F);
        this.root = param0.getChild("root");
        this.head = this.root.getChild("bone").getChild("body").getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition var2 = var1.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition var3 = var2.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(62, 68)
                .addBox(-12.5F, -14.0F, -20.0F, 25.0F, 29.0F, 40.0F, new CubeDeformation(0.0F))
                .texOffs(62, 0)
                .addBox(-12.5F, -14.0F, -20.0F, 25.0F, 24.0F, 40.0F, new CubeDeformation(0.5F))
                .texOffs(87, 68)
                .addBox(-12.5F, 12.0F, -20.0F, 25.0F, 0.0F, 40.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        var2.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(32, 87).addBox(-3.5F, -1.0F, -4.0F, 7.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-7.5F, 10.0F, -15.0F)
        );
        var2.addOrReplaceChild(
            "right_mid_leg",
            CubeListBuilder.create().texOffs(32, 105).addBox(-3.5F, -1.0F, -4.0F, 7.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-7.5F, 10.0F, 0.0F)
        );
        var2.addOrReplaceChild(
            "right_hind_leg",
            CubeListBuilder.create().texOffs(32, 123).addBox(-3.5F, -1.0F, -4.0F, 7.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-7.5F, 10.0F, 15.0F)
        );
        var2.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(0, 87).addBox(-3.5F, -1.0F, -4.0F, 7.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(7.5F, 10.0F, -15.0F)
        );
        var2.addOrReplaceChild(
            "left_mid_leg",
            CubeListBuilder.create().texOffs(0, 105).addBox(-3.5F, -1.0F, -4.0F, 7.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(7.5F, 10.0F, 0.0F)
        );
        var2.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(0, 123).addBox(-3.5F, -1.0F, -4.0F, 7.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(7.5F, 10.0F, 15.0F)
        );
        PartDefinition var4 = var3.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(8, 15)
                .addBox(-6.5F, -7.5F, -11.5F, 13.0F, 18.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(8, 4)
                .addBox(-6.5F, 7.5F, -11.5F, 13.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 6.5F, -19.5F)
        );
        var4.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create().texOffs(2, 0).addBox(0.0F, 0.0F, -3.0F, 1.0F, 19.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offset(6.51F, -7.5F, -4.51F)
        );
        var4.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 19.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-6.51F, -7.5F, -4.51F)
        );
        var4.addOrReplaceChild(
            "nose",
            CubeListBuilder.create().texOffs(10, 45).addBox(-6.5F, -2.0F, -9.0F, 13.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -4.5F, -11.5F)
        );
        var4.addOrReplaceChild(
            "lower_beak",
            CubeListBuilder.create().texOffs(10, 57).addBox(-6.5F, -7.0F, -8.0F, 13.0F, 12.0F, 9.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 2.5F, -12.5F)
        );
        return LayerDefinition.create(var0, 192, 192);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        float var0 = Math.min((float)param0.getDeltaMovement().horizontalDistanceSqr() * 9000.0F, 2.0F);
        this.animate(param0.walkingAnimationState, SnifferAnimation.SNIFFER_WALK, param3, var0);
        this.animate(param0.diggingAnimationState, SnifferAnimation.SNIFFER_DIG, param3);
        this.animate(param0.searchingAnimationState, SnifferAnimation.SNIFFER_SNIFF_SEARCH, param3, Math.min(var0, 1.0F));
        this.animate(param0.sniffingAnimationState, SnifferAnimation.SNIFFER_LONGSNIFF, param3);
        this.animate(param0.risingAnimationState, SnifferAnimation.SNIFFER_STAND_UP, param3);
        this.animate(param0.feelingHappyAnimationState, SnifferAnimation.SNIFFER_HAPPY, param3);
        this.animate(param0.scentingAnimationState, SnifferAnimation.SNIFFER_SNIFFSNIFF, param3);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

package net.minecraft.client.model;

import net.minecraft.client.animation.definitions.BreezeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeModel<T extends Breeze> extends HierarchicalModel<T> {
    private static final float WIND_TOP_SPEED = 0.6F;
    private static final float WIND_MIDDLE_SPEED = 0.8F;
    private static final float WIND_BOTTOM_SPEED = 1.0F;
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart windBody;
    private final ModelPart windTop;
    private final ModelPart windMid;
    private final ModelPart windBottom;
    private final ModelPart rods;

    public BreezeModel(ModelPart param0) {
        super(RenderType::entityTranslucent);
        this.root = param0;
        this.windBody = param0.getChild("wind_body");
        this.windBottom = this.windBody.getChild("wind_bottom");
        this.windMid = this.windBottom.getChild("wind_mid");
        this.windTop = this.windMid.getChild("wind_top");
        this.head = param0.getChild("body").getChild("head");
        this.rods = param0.getChild("body").getChild("rods");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition var3 = var2.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
        var3.addOrReplaceChild(
            "rod_1",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(2.5981F, -3.0F, 1.5F, -2.7489F, -1.0472F, 3.1416F)
        );
        var3.addOrReplaceChild(
            "rod_2",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-2.5981F, -3.0F, 1.5F, -2.7489F, 1.0472F, 3.1416F)
        );
        var3.addOrReplaceChild(
            "rod_3",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, -3.0F, -3.0F, 0.3927F, 0.0F, 0.0F)
        );
        PartDefinition var4 = var2.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 4.0F, 0.0F)
        );
        PartDefinition var5 = var1.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition var6 = var5.addOrReplaceChild("wind_bottom", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition var7 = var6.addOrReplaceChild("wind_mid", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 0.0F));
        var7.addOrReplaceChild("wind_top", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 0.0F));
        return LayerDefinition.create(var0, 32, 32);
    }

    public static LayerDefinition createEyesLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition var3 = var2.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition var4 = var2.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
        var4.addOrReplaceChild(
            "eyes",
            CubeListBuilder.create()
                .texOffs(4, 24)
                .addBox(-5.0F, -5.0F, -4.2F, 10.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(7, 16)
                .addBox(-4.0F, -2.0F, -4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        PartDefinition var5 = var1.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition var6 = var5.addOrReplaceChild("wind_bottom", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition var7 = var6.addOrReplaceChild("wind_mid", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 0.0F));
        var7.addOrReplaceChild("wind_top", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 0.0F));
        return LayerDefinition.create(var0, 32, 32);
    }

    public static LayerDefinition createWindBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition var3 = var2.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
        var2.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition var4 = var1.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition var5 = var4.addOrReplaceChild(
            "wind_bottom",
            CubeListBuilder.create().texOffs(1, 83).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        PartDefinition var6 = var5.addOrReplaceChild(
            "wind_mid",
            CubeListBuilder.create()
                .texOffs(74, 28)
                .addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(78, 32)
                .addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(49, 71)
                .addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -7.0F, 0.0F)
        );
        var6.addOrReplaceChild(
            "wind_top",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-9.0F, -8.0F, -9.0F, 18.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(6, 6)
                .addBox(-6.0F, -8.0F, -6.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(105, 57)
                .addBox(-2.5F, -8.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -6.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 128, 128);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float var0 = param3 * (float) Math.PI * -0.1F;
        this.windTop.x = Mth.cos(var0) * 1.0F * 0.6F;
        this.windTop.z = Mth.sin(var0) * 1.0F * 0.6F;
        this.windMid.x = Mth.sin(var0) * 0.5F * 0.8F;
        this.windMid.z = Mth.cos(var0) * 0.8F;
        this.windBottom.x = Mth.cos(var0) * -0.25F * 1.0F;
        this.windBottom.z = Mth.sin(var0) * -0.25F * 1.0F;
        this.head.y = 4.0F + Mth.cos(var0) / 4.0F;
        this.rods.yRot = param3 * (float) Math.PI * 0.1F;
        this.animate(param0.shoot, BreezeAnimation.SHOOT, param3);
        this.animate(param0.slide, BreezeAnimation.SLIDE, param3);
        this.animate(param0.longJump, BreezeAnimation.JUMP, param3);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public ModelPart windTop() {
        return this.windTop;
    }

    public ModelPart windMiddle() {
        return this.windMid;
    }

    public ModelPart windBottom() {
        return this.windBottom;
    }
}

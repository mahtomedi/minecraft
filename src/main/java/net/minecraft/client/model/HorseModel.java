package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseModel<T extends AbstractHorse> extends AgeableListModel<T> {
    private static final float DEG_125 = 2.1816616F;
    private static final float DEG_60 = (float) (Math.PI / 3);
    private static final float DEG_45 = (float) (Math.PI / 4);
    private static final float DEG_30 = (float) (Math.PI / 6);
    private static final float DEG_15 = (float) (Math.PI / 12);
    protected static final String HEAD_PARTS = "head_parts";
    private static final String LEFT_HIND_BABY_LEG = "left_hind_baby_leg";
    private static final String RIGHT_HIND_BABY_LEG = "right_hind_baby_leg";
    private static final String LEFT_FRONT_BABY_LEG = "left_front_baby_leg";
    private static final String RIGHT_FRONT_BABY_LEG = "right_front_baby_leg";
    private static final String SADDLE = "saddle";
    private static final String LEFT_SADDLE_MOUTH = "left_saddle_mouth";
    private static final String LEFT_SADDLE_LINE = "left_saddle_line";
    private static final String RIGHT_SADDLE_MOUTH = "right_saddle_mouth";
    private static final String RIGHT_SADDLE_LINE = "right_saddle_line";
    private static final String HEAD_SADDLE = "head_saddle";
    private static final String MOUTH_SADDLE_WRAP = "mouth_saddle_wrap";
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindBabyLeg;
    private final ModelPart leftHindBabyLeg;
    private final ModelPart rightFrontBabyLeg;
    private final ModelPart leftFrontBabyLeg;
    private final ModelPart tail;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public HorseModel(ModelPart param0) {
        super(true, 16.2F, 1.36F, 2.7272F, 2.0F, 20.0F);
        this.body = param0.getChild("body");
        this.headParts = param0.getChild("head_parts");
        this.rightHindLeg = param0.getChild("right_hind_leg");
        this.leftHindLeg = param0.getChild("left_hind_leg");
        this.rightFrontLeg = param0.getChild("right_front_leg");
        this.leftFrontLeg = param0.getChild("left_front_leg");
        this.rightHindBabyLeg = param0.getChild("right_hind_baby_leg");
        this.leftHindBabyLeg = param0.getChild("left_hind_baby_leg");
        this.rightFrontBabyLeg = param0.getChild("right_front_baby_leg");
        this.leftFrontBabyLeg = param0.getChild("left_front_baby_leg");
        this.tail = this.body.getChild("tail");
        ModelPart var0 = this.body.getChild("saddle");
        ModelPart var1 = this.headParts.getChild("left_saddle_mouth");
        ModelPart var2 = this.headParts.getChild("right_saddle_mouth");
        ModelPart var3 = this.headParts.getChild("left_saddle_line");
        ModelPart var4 = this.headParts.getChild("right_saddle_line");
        ModelPart var5 = this.headParts.getChild("head_saddle");
        ModelPart var6 = this.headParts.getChild("mouth_saddle_wrap");
        this.saddleParts = new ModelPart[]{var0, var1, var2, var5, var6};
        this.ridingParts = new ModelPart[]{var3, var4};
    }

    public static MeshDefinition createBodyMesh(CubeDeformation param0) {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F, new CubeDeformation(0.05F)),
            PartPose.offset(0.0F, 11.0F, 5.0F)
        );
        PartDefinition var3 = var1.addOrReplaceChild(
            "head_parts",
            CubeListBuilder.create().texOffs(0, 35).addBox(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, -12.0F, (float) (Math.PI / 6), 0.0F, 0.0F)
        );
        PartDefinition var4 = var3.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F, param0), PartPose.ZERO
        );
        var3.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -11.0F, 5.01F, 2.0F, 16.0F, 2.0F, param0), PartPose.ZERO);
        var3.addOrReplaceChild("upper_mouth", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F, param0), PartPose.ZERO);
        var1.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, param0),
            PartPose.offset(4.0F, 14.0F, 7.0F)
        );
        var1.addOrReplaceChild(
            "right_hind_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, param0),
            PartPose.offset(-4.0F, 14.0F, 7.0F)
        );
        var1.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, param0),
            PartPose.offset(4.0F, 14.0F, -12.0F)
        );
        var1.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, param0),
            PartPose.offset(-4.0F, 14.0F, -12.0F)
        );
        CubeDeformation var5 = param0.extend(0.0F, 5.5F, 0.0F);
        var1.addOrReplaceChild(
            "left_hind_baby_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, var5),
            PartPose.offset(4.0F, 14.0F, 7.0F)
        );
        var1.addOrReplaceChild(
            "right_hind_baby_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, var5),
            PartPose.offset(-4.0F, 14.0F, 7.0F)
        );
        var1.addOrReplaceChild(
            "left_front_baby_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, var5),
            PartPose.offset(4.0F, 14.0F, -12.0F)
        );
        var1.addOrReplaceChild(
            "right_front_baby_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, var5),
            PartPose.offset(-4.0F, 14.0F, -12.0F)
        );
        var2.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(42, 36).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F, param0),
            PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, (float) (Math.PI / 6), 0.0F, 0.0F)
        );
        var2.addOrReplaceChild(
            "saddle", CubeListBuilder.create().texOffs(26, 0).addBox(-5.0F, -8.0F, -9.0F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.ZERO
        );
        var3.addOrReplaceChild("left_saddle_mouth", CubeListBuilder.create().texOffs(29, 5).addBox(2.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, param0), PartPose.ZERO);
        var3.addOrReplaceChild(
            "right_saddle_mouth", CubeListBuilder.create().texOffs(29, 5).addBox(-3.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, param0), PartPose.ZERO
        );
        var3.addOrReplaceChild(
            "left_saddle_line",
            CubeListBuilder.create().texOffs(32, 2).addBox(3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F, param0),
            PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F)
        );
        var3.addOrReplaceChild(
            "right_saddle_line",
            CubeListBuilder.create().texOffs(32, 2).addBox(-3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F, param0),
            PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F)
        );
        var3.addOrReplaceChild(
            "head_saddle", CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -11.0F, -1.9F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.2F)), PartPose.ZERO
        );
        var3.addOrReplaceChild(
            "mouth_saddle_wrap",
            CubeListBuilder.create().texOffs(19, 0).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.2F)),
            PartPose.ZERO
        );
        var4.addOrReplaceChild(
            "left_ear", CubeListBuilder.create().texOffs(19, 16).addBox(0.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.ZERO
        );
        var4.addOrReplaceChild(
            "right_ear", CubeListBuilder.create().texOffs(19, 16).addBox(-2.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.ZERO
        );
        return var0;
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        boolean var0 = param0.isSaddled();
        boolean var1 = param0.isVehicle();

        for(ModelPart var2 : this.saddleParts) {
            var2.visible = var0;
        }

        for(ModelPart var3 : this.ridingParts) {
            var3.visible = var1 && var0;
        }

        this.body.y = 11.0F;
    }

    @Override
    public Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.headParts);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(
            this.body,
            this.rightHindLeg,
            this.leftHindLeg,
            this.rightFrontLeg,
            this.leftFrontLeg,
            this.rightHindBabyLeg,
            this.leftHindBabyLeg,
            this.rightFrontBabyLeg,
            this.leftFrontBabyLeg
        );
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        float var0 = Mth.rotlerp(param0.yBodyRotO, param0.yBodyRot, param3);
        float var1 = Mth.rotlerp(param0.yHeadRotO, param0.yHeadRot, param3);
        float var2 = Mth.lerp(param3, param0.xRotO, param0.getXRot());
        float var3 = var1 - var0;
        float var4 = var2 * (float) (Math.PI / 180.0);
        if (var3 > 20.0F) {
            var3 = 20.0F;
        }

        if (var3 < -20.0F) {
            var3 = -20.0F;
        }

        if (param2 > 0.2F) {
            var4 += Mth.cos(param1 * 0.4F) * 0.15F * param2;
        }

        float var5 = param0.getEatAnim(param3);
        float var6 = param0.getStandAnim(param3);
        float var7 = 1.0F - var6;
        float var8 = param0.getMouthAnim(param3);
        boolean var9 = param0.tailCounter != 0;
        float var10 = (float)param0.tickCount + param3;
        this.headParts.y = 4.0F;
        this.headParts.z = -12.0F;
        this.body.xRot = 0.0F;
        this.headParts.xRot = (float) (Math.PI / 6) + var4;
        this.headParts.yRot = var3 * (float) (Math.PI / 180.0);
        float var11 = param0.isInWater() ? 0.2F : 1.0F;
        float var12 = Mth.cos(var11 * param1 * 0.6662F + (float) Math.PI);
        float var13 = var12 * 0.8F * param2;
        float var14 = (1.0F - Math.max(var6, var5)) * ((float) (Math.PI / 6) + var4 + var8 * Mth.sin(var10) * 0.05F);
        this.headParts.xRot = var6 * ((float) (Math.PI / 12) + var4) + var5 * (2.1816616F + Mth.sin(var10) * 0.05F) + var14;
        this.headParts.yRot = var6 * var3 * (float) (Math.PI / 180.0) + (1.0F - Math.max(var6, var5)) * this.headParts.yRot;
        this.headParts.y = var6 * -4.0F + var5 * 11.0F + (1.0F - Math.max(var6, var5)) * this.headParts.y;
        this.headParts.z = var6 * -4.0F + var5 * -12.0F + (1.0F - Math.max(var6, var5)) * this.headParts.z;
        this.body.xRot = var6 * (float) (-Math.PI / 4) + var7 * this.body.xRot;
        float var15 = (float) (Math.PI / 12) * var6;
        float var16 = Mth.cos(var10 * 0.6F + (float) Math.PI);
        this.leftFrontLeg.y = 2.0F * var6 + 14.0F * var7;
        this.leftFrontLeg.z = -6.0F * var6 - 10.0F * var7;
        this.rightFrontLeg.y = this.leftFrontLeg.y;
        this.rightFrontLeg.z = this.leftFrontLeg.z;
        float var17 = ((float) (-Math.PI / 3) + var16) * var6 + var13 * var7;
        float var18 = ((float) (-Math.PI / 3) - var16) * var6 - var13 * var7;
        this.leftHindLeg.xRot = var15 - var12 * 0.5F * param2 * var7;
        this.rightHindLeg.xRot = var15 + var12 * 0.5F * param2 * var7;
        this.leftFrontLeg.xRot = var17;
        this.rightFrontLeg.xRot = var18;
        this.tail.xRot = (float) (Math.PI / 6) + param2 * 0.75F;
        this.tail.y = -5.0F + param2;
        this.tail.z = 2.0F + param2 * 2.0F;
        if (var9) {
            this.tail.yRot = Mth.cos(var10 * 0.7F);
        } else {
            this.tail.yRot = 0.0F;
        }

        this.rightHindBabyLeg.y = this.rightHindLeg.y;
        this.rightHindBabyLeg.z = this.rightHindLeg.z;
        this.rightHindBabyLeg.xRot = this.rightHindLeg.xRot;
        this.leftHindBabyLeg.y = this.leftHindLeg.y;
        this.leftHindBabyLeg.z = this.leftHindLeg.z;
        this.leftHindBabyLeg.xRot = this.leftHindLeg.xRot;
        this.rightFrontBabyLeg.y = this.rightFrontLeg.y;
        this.rightFrontBabyLeg.z = this.rightFrontLeg.z;
        this.rightFrontBabyLeg.xRot = this.rightFrontLeg.xRot;
        this.leftFrontBabyLeg.y = this.leftFrontLeg.y;
        this.leftFrontBabyLeg.z = this.leftFrontLeg.z;
        this.leftFrontBabyLeg.xRot = this.leftFrontLeg.xRot;
        boolean var19 = param0.isBaby();
        this.rightHindLeg.visible = !var19;
        this.leftHindLeg.visible = !var19;
        this.rightFrontLeg.visible = !var19;
        this.leftFrontLeg.visible = !var19;
        this.rightHindBabyLeg.visible = var19;
        this.leftHindBabyLeg.visible = var19;
        this.rightFrontBabyLeg.visible = var19;
        this.leftFrontBabyLeg.visible = var19;
        this.body.y = var19 ? 10.8F : 0.0F;
    }
}

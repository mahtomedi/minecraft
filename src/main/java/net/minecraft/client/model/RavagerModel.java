package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RavagerModel extends HierarchicalModel<Ravager> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart neck;

    public RavagerModel(ModelPart param0) {
        this.root = param0;
        this.neck = param0.getChild("neck");
        this.head = this.neck.getChild("head");
        this.mouth = this.head.getChild("mouth");
        this.rightHindLeg = param0.getChild("right_hind_leg");
        this.leftHindLeg = param0.getChild("left_hind_leg");
        this.rightFrontLeg = param0.getChild("right_front_leg");
        this.leftFrontLeg = param0.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        int var2 = 16;
        PartDefinition var3 = var1.addOrReplaceChild(
            "neck", CubeListBuilder.create().texOffs(68, 73).addBox(-5.0F, -1.0F, -18.0F, 10.0F, 10.0F, 18.0F), PartPose.offset(0.0F, -7.0F, 5.5F)
        );
        PartDefinition var4 = var3.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-8.0F, -20.0F, -14.0F, 16.0F, 20.0F, 16.0F)
                .texOffs(0, 0)
                .addBox(-2.0F, -6.0F, -18.0F, 4.0F, 8.0F, 4.0F),
            PartPose.offset(0.0F, 16.0F, -17.0F)
        );
        var4.addOrReplaceChild(
            "right_horn",
            CubeListBuilder.create().texOffs(74, 55).addBox(0.0F, -14.0F, -2.0F, 2.0F, 14.0F, 4.0F),
            PartPose.offsetAndRotation(-10.0F, -14.0F, -8.0F, 1.0995574F, 0.0F, 0.0F)
        );
        var4.addOrReplaceChild(
            "left_horn",
            CubeListBuilder.create().texOffs(74, 55).mirror().addBox(0.0F, -14.0F, -2.0F, 2.0F, 14.0F, 4.0F),
            PartPose.offsetAndRotation(8.0F, -14.0F, -8.0F, 1.0995574F, 0.0F, 0.0F)
        );
        var4.addOrReplaceChild(
            "mouth", CubeListBuilder.create().texOffs(0, 36).addBox(-8.0F, 0.0F, -16.0F, 16.0F, 3.0F, 16.0F), PartPose.offset(0.0F, -2.0F, 2.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 55)
                .addBox(-7.0F, -10.0F, -7.0F, 14.0F, 16.0F, 20.0F)
                .texOffs(0, 91)
                .addBox(-6.0F, 6.0F, -7.0F, 12.0F, 13.0F, 18.0F),
            PartPose.offsetAndRotation(0.0F, 1.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(96, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F), PartPose.offset(-8.0F, -13.0F, 18.0F)
        );
        var1.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(96, 0).mirror().addBox(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F),
            PartPose.offset(8.0F, -13.0F, 18.0F)
        );
        var1.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F), PartPose.offset(-8.0F, -13.0F, -5.0F)
        );
        var1.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F),
            PartPose.offset(8.0F, -13.0F, -5.0F)
        );
        return LayerDefinition.create(var0, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public void setupAnim(Ravager param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        float var0 = 0.4F * param2;
        this.rightHindLeg.xRot = Mth.cos(param1 * 0.6662F) * var0;
        this.leftHindLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * var0;
        this.rightFrontLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * var0;
        this.leftFrontLeg.xRot = Mth.cos(param1 * 0.6662F) * var0;
    }

    public void prepareMobModel(Ravager param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        int var0 = param0.getStunnedTick();
        int var1 = param0.getRoarTick();
        int var2 = 20;
        int var3 = param0.getAttackTick();
        int var4 = 10;
        if (var3 > 0) {
            float var5 = Mth.triangleWave((float)var3 - param3, 10.0F);
            float var6 = (1.0F + var5) * 0.5F;
            float var7 = var6 * var6 * var6 * 12.0F;
            float var8 = var7 * Mth.sin(this.neck.xRot);
            this.neck.z = -6.5F + var7;
            this.neck.y = -7.0F - var8;
            float var9 = Mth.sin(((float)var3 - param3) / 10.0F * (float) Math.PI * 0.25F);
            this.mouth.xRot = (float) (Math.PI / 2) * var9;
            if (var3 > 5) {
                this.mouth.xRot = Mth.sin(((float)(-4 + var3) - param3) / 4.0F) * (float) Math.PI * 0.4F;
            } else {
                this.mouth.xRot = (float) (Math.PI / 20) * Mth.sin((float) Math.PI * ((float)var3 - param3) / 10.0F);
            }
        } else {
            float var10 = -1.0F;
            float var11 = -1.0F * Mth.sin(this.neck.xRot);
            this.neck.x = 0.0F;
            this.neck.y = -7.0F - var11;
            this.neck.z = 5.5F;
            boolean var12 = var0 > 0;
            this.neck.xRot = var12 ? 0.21991149F : 0.0F;
            this.mouth.xRot = (float) Math.PI * (var12 ? 0.05F : 0.01F);
            if (var12) {
                double var13 = (double)var0 / 40.0;
                this.neck.x = (float)Math.sin(var13 * 10.0) * 3.0F;
            } else if (var1 > 0) {
                float var14 = Mth.sin(((float)(20 - var1) - param3) / 20.0F * (float) Math.PI * 0.25F);
                this.mouth.xRot = (float) (Math.PI / 2) * var14;
            }
        }

    }
}

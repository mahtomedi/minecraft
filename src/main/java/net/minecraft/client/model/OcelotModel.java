package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OcelotModel<T extends Entity> extends AgeableListModel<T> {
    protected final ModelPart leftHindLeg;
    protected final ModelPart rightHindLeg;
    protected final ModelPart leftFrontLeg;
    protected final ModelPart rightFrontLeg;
    protected final ModelPart tail1;
    protected final ModelPart tail2;
    protected final ModelPart head;
    protected final ModelPart body;
    protected int state = 1;

    public OcelotModel(ModelPart param0) {
        super(true, 10.0F, 4.0F);
        this.head = param0.getChild("head");
        this.body = param0.getChild("body");
        this.tail1 = param0.getChild("tail1");
        this.tail2 = param0.getChild("tail2");
        this.leftHindLeg = param0.getChild("left_hind_leg");
        this.rightHindLeg = param0.getChild("right_hind_leg");
        this.leftFrontLeg = param0.getChild("left_front_leg");
        this.rightFrontLeg = param0.getChild("right_front_leg");
    }

    public static MeshDefinition createBodyMesh(CubeDeformation param0) {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .addBox("main", -2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 5.0F, param0)
                .addBox("nose", -1.5F, 0.0F, -4.0F, 3, 2, 2, param0, 0, 24)
                .addBox("ear1", -2.0F, -3.0F, 0.0F, 1, 1, 2, param0, 0, 10)
                .addBox("ear2", 1.0F, -3.0F, 0.0F, 1, 1, 2, param0, 6, 10),
            PartPose.offset(0.0F, 15.0F, -9.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(20, 0).addBox(-2.0F, 3.0F, -8.0F, 4.0F, 16.0F, 6.0F, param0),
            PartPose.offsetAndRotation(0.0F, 12.0F, -10.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "tail1",
            CubeListBuilder.create().texOffs(0, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, param0),
            PartPose.offsetAndRotation(0.0F, 15.0F, 8.0F, 0.9F, 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "tail2", CubeListBuilder.create().texOffs(4, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, param0), PartPose.offset(0.0F, 20.0F, 14.0F)
        );
        CubeListBuilder var2 = CubeListBuilder.create().texOffs(8, 13).addBox(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, param0);
        var1.addOrReplaceChild("left_hind_leg", var2, PartPose.offset(1.1F, 18.0F, 5.0F));
        var1.addOrReplaceChild("right_hind_leg", var2, PartPose.offset(-1.1F, 18.0F, 5.0F));
        CubeListBuilder var3 = CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, param0);
        var1.addOrReplaceChild("left_front_leg", var3, PartPose.offset(1.2F, 14.1F, -5.0F));
        var1.addOrReplaceChild("right_front_leg", var3, PartPose.offset(-1.2F, 14.1F, -5.0F));
        return var0;
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.leftHindLeg, this.rightHindLeg, this.leftFrontLeg, this.rightFrontLeg, this.tail1, this.tail2);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        if (this.state != 3) {
            this.body.xRot = (float) (Math.PI / 2);
            if (this.state == 2) {
                this.leftHindLeg.xRot = Mth.cos(param1 * 0.6662F) * param2;
                this.rightHindLeg.xRot = Mth.cos(param1 * 0.6662F + 0.3F) * param2;
                this.leftFrontLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI + 0.3F) * param2;
                this.rightFrontLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * param2;
                this.tail2.xRot = 1.7278761F + (float) (Math.PI / 10) * Mth.cos(param1) * param2;
            } else {
                this.leftHindLeg.xRot = Mth.cos(param1 * 0.6662F) * param2;
                this.rightHindLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * param2;
                this.leftFrontLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * param2;
                this.rightFrontLeg.xRot = Mth.cos(param1 * 0.6662F) * param2;
                if (this.state == 1) {
                    this.tail2.xRot = 1.7278761F + (float) (Math.PI / 4) * Mth.cos(param1) * param2;
                } else {
                    this.tail2.xRot = 1.7278761F + 0.47123894F * Mth.cos(param1) * param2;
                }
            }
        }

    }

    @Override
    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        this.body.y = 12.0F;
        this.body.z = -10.0F;
        this.head.y = 15.0F;
        this.head.z = -9.0F;
        this.tail1.y = 15.0F;
        this.tail1.z = 8.0F;
        this.tail2.y = 20.0F;
        this.tail2.z = 14.0F;
        this.leftFrontLeg.y = 14.1F;
        this.leftFrontLeg.z = -5.0F;
        this.rightFrontLeg.y = 14.1F;
        this.rightFrontLeg.z = -5.0F;
        this.leftHindLeg.y = 18.0F;
        this.leftHindLeg.z = 5.0F;
        this.rightHindLeg.y = 18.0F;
        this.rightHindLeg.z = 5.0F;
        this.tail1.xRot = 0.9F;
        if (param0.isCrouching()) {
            ++this.body.y;
            this.head.y += 2.0F;
            ++this.tail1.y;
            this.tail2.y += -4.0F;
            this.tail2.z += 2.0F;
            this.tail1.xRot = (float) (Math.PI / 2);
            this.tail2.xRot = (float) (Math.PI / 2);
            this.state = 0;
        } else if (param0.isSprinting()) {
            this.tail2.y = this.tail1.y;
            this.tail2.z += 2.0F;
            this.tail1.xRot = (float) (Math.PI / 2);
            this.tail2.xRot = (float) (Math.PI / 2);
            this.state = 2;
        } else {
            this.state = 1;
        }

    }
}

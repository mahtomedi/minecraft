package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotModel extends HierarchicalModel<Parrot> {
    private static final String FEATHER = "feather";
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart head;
    private final ModelPart feather;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ParrotModel(ModelPart param0) {
        this.root = param0;
        this.body = param0.getChild("body");
        this.tail = param0.getChild("tail");
        this.leftWing = param0.getChild("left_wing");
        this.rightWing = param0.getChild("right_wing");
        this.head = param0.getChild("head");
        this.feather = this.head.getChild("feather");
        this.leftLeg = param0.getChild("left_leg");
        this.rightLeg = param0.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("body", CubeListBuilder.create().texOffs(2, 8).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 16.5F, -3.0F));
        var1.addOrReplaceChild(
            "tail", CubeListBuilder.create().texOffs(22, 1).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 21.07F, 1.16F)
        );
        var1.addOrReplaceChild(
            "left_wing", CubeListBuilder.create().texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F), PartPose.offset(1.5F, 16.94F, -2.76F)
        );
        var1.addOrReplaceChild(
            "right_wing", CubeListBuilder.create().texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F), PartPose.offset(-1.5F, 16.94F, -2.76F)
        );
        PartDefinition var2 = var1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(2, 2).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 15.69F, -2.76F)
        );
        var2.addOrReplaceChild(
            "head2", CubeListBuilder.create().texOffs(10, 0).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 1.0F, 4.0F), PartPose.offset(0.0F, -2.0F, -1.0F)
        );
        var2.addOrReplaceChild(
            "beak1", CubeListBuilder.create().texOffs(11, 7).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -0.5F, -1.5F)
        );
        var2.addOrReplaceChild(
            "beak2", CubeListBuilder.create().texOffs(16, 7).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -1.75F, -2.45F)
        );
        var2.addOrReplaceChild(
            "feather", CubeListBuilder.create().texOffs(2, 18).addBox(0.0F, -4.0F, -2.0F, 0.0F, 5.0F, 4.0F), PartPose.offset(0.0F, -2.15F, 0.15F)
        );
        CubeListBuilder var3 = CubeListBuilder.create().texOffs(14, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F);
        var1.addOrReplaceChild("left_leg", var3, PartPose.offset(1.0F, 22.0F, -1.05F));
        var1.addOrReplaceChild("right_leg", var3, PartPose.offset(-1.0F, 22.0F, -1.05F));
        return LayerDefinition.create(var0, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public void setupAnim(Parrot param0, float param1, float param2, float param3, float param4, float param5) {
        this.setupAnim(getState(param0), param0.tickCount, param1, param2, param3, param4, param5);
    }

    public void prepareMobModel(Parrot param0, float param1, float param2, float param3) {
        this.prepare(getState(param0));
    }

    public void renderOnShoulder(
        PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7, int param8
    ) {
        this.prepare(ParrotModel.State.ON_SHOULDER);
        this.setupAnim(ParrotModel.State.ON_SHOULDER, param8, param4, param5, 0.0F, param6, param7);
        this.root.render(param0, param1, param2, param3);
    }

    private void setupAnim(ParrotModel.State param0, int param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.xRot = param6 * (float) (Math.PI / 180.0);
        this.head.yRot = param5 * (float) (Math.PI / 180.0);
        this.head.zRot = 0.0F;
        this.head.x = 0.0F;
        this.body.x = 0.0F;
        this.tail.x = 0.0F;
        this.rightWing.x = -1.5F;
        this.leftWing.x = 1.5F;
        switch(param0) {
            case SITTING:
                break;
            case PARTY:
                float var0 = Mth.cos((float)param1);
                float var1 = Mth.sin((float)param1);
                this.head.x = var0;
                this.head.y = 15.69F + var1;
                this.head.xRot = 0.0F;
                this.head.yRot = 0.0F;
                this.head.zRot = Mth.sin((float)param1) * 0.4F;
                this.body.x = var0;
                this.body.y = 16.5F + var1;
                this.leftWing.zRot = -0.0873F - param4;
                this.leftWing.x = 1.5F + var0;
                this.leftWing.y = 16.94F + var1;
                this.rightWing.zRot = 0.0873F + param4;
                this.rightWing.x = -1.5F + var0;
                this.rightWing.y = 16.94F + var1;
                this.tail.x = var0;
                this.tail.y = 21.07F + var1;
                break;
            case STANDING:
                this.leftLeg.xRot += Mth.cos(param2 * 0.6662F) * 1.4F * param3;
                this.rightLeg.xRot += Mth.cos(param2 * 0.6662F + (float) Math.PI) * 1.4F * param3;
            case FLYING:
            case ON_SHOULDER:
            default:
                float var2 = param4 * 0.3F;
                this.head.y = 15.69F + var2;
                this.tail.xRot = 1.015F + Mth.cos(param2 * 0.6662F) * 0.3F * param3;
                this.tail.y = 21.07F + var2;
                this.body.y = 16.5F + var2;
                this.leftWing.zRot = -0.0873F - param4;
                this.leftWing.y = 16.94F + var2;
                this.rightWing.zRot = 0.0873F + param4;
                this.rightWing.y = 16.94F + var2;
                this.leftLeg.y = 22.0F + var2;
                this.rightLeg.y = 22.0F + var2;
        }

    }

    private void prepare(ParrotModel.State param0) {
        this.feather.xRot = -0.2214F;
        this.body.xRot = 0.4937F;
        this.leftWing.xRot = -0.6981F;
        this.leftWing.yRot = (float) -Math.PI;
        this.rightWing.xRot = -0.6981F;
        this.rightWing.yRot = (float) -Math.PI;
        this.leftLeg.xRot = -0.0299F;
        this.rightLeg.xRot = -0.0299F;
        this.leftLeg.y = 22.0F;
        this.rightLeg.y = 22.0F;
        this.leftLeg.zRot = 0.0F;
        this.rightLeg.zRot = 0.0F;
        switch(param0) {
            case SITTING:
                float var0 = 1.9F;
                this.head.y = 17.59F;
                this.tail.xRot = 1.5388988F;
                this.tail.y = 22.97F;
                this.body.y = 18.4F;
                this.leftWing.zRot = -0.0873F;
                this.leftWing.y = 18.84F;
                this.rightWing.zRot = 0.0873F;
                this.rightWing.y = 18.84F;
                ++this.leftLeg.y;
                ++this.rightLeg.y;
                ++this.leftLeg.xRot;
                ++this.rightLeg.xRot;
                break;
            case PARTY:
                this.leftLeg.zRot = (float) (-Math.PI / 9);
                this.rightLeg.zRot = (float) (Math.PI / 9);
            case STANDING:
            case ON_SHOULDER:
            default:
                break;
            case FLYING:
                this.leftLeg.xRot += (float) (Math.PI * 2.0 / 9.0);
                this.rightLeg.xRot += (float) (Math.PI * 2.0 / 9.0);
        }

    }

    private static ParrotModel.State getState(Parrot param0) {
        if (param0.isPartyParrot()) {
            return ParrotModel.State.PARTY;
        } else if (param0.isInSittingPose()) {
            return ParrotModel.State.SITTING;
        } else {
            return param0.isFlying() ? ParrotModel.State.FLYING : ParrotModel.State.STANDING;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum State {
        FLYING,
        STANDING,
        SITTING,
        PARTY,
        ON_SHOULDER;
    }
}

package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Random;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerModel<T extends LivingEntity> extends HumanoidModel<T> {
    private final List<ModelPart> parts;
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final ModelPart cloak;
    private final ModelPart ear;
    private final boolean slim;

    public PlayerModel(ModelPart param0, boolean param1) {
        super(param0, RenderType::entityTranslucent);
        this.slim = param1;
        this.ear = param0.getChild("ear");
        this.cloak = param0.getChild("cloak");
        this.leftSleeve = param0.getChild("left_sleeve");
        this.rightSleeve = param0.getChild("right_sleeve");
        this.leftPants = param0.getChild("left_pants");
        this.rightPants = param0.getChild("right_pants");
        this.jacket = param0.getChild("jacket");
        this.parts = param0.getAllParts().filter(param0x -> !param0x.isEmpty()).collect(ImmutableList.toImmutableList());
    }

    public static MeshDefinition createMesh(CubeDeformation param0, boolean param1) {
        MeshDefinition var0 = HumanoidModel.createMesh(param0, 0.0F);
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("ear", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, param0), PartPose.ZERO);
        var1.addOrReplaceChild(
            "cloak",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, param0, 1.0F, 0.5F),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        float var2 = 0.25F;
        if (param1) {
            var1.addOrReplaceChild(
                "left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, param0), PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            var1.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, param0),
                PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
            var1.addOrReplaceChild(
                "left_sleeve",
                CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, param0.extend(0.25F)),
                PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            var1.addOrReplaceChild(
                "right_sleeve",
                CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, param0.extend(0.25F)),
                PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
        } else {
            var1.addOrReplaceChild(
                "left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0), PartPose.offset(5.0F, 2.0F, 0.0F)
            );
            var1.addOrReplaceChild(
                "left_sleeve",
                CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(0.25F)),
                PartPose.offset(5.0F, 2.0F, 0.0F)
            );
            var1.addOrReplaceChild(
                "right_sleeve",
                CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(0.25F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
            );
        }

        var1.addOrReplaceChild(
            "left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0), PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_pants",
            CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(0.25F)),
            PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_pants",
            CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(0.25F)),
            PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, param0.extend(0.25F)), PartPose.ZERO
        );
        return var0;
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.leftPants, this.rightPants, this.leftSleeve, this.rightSleeve, this.jacket));
    }

    public void renderEars(PoseStack param0, VertexConsumer param1, int param2, int param3) {
        this.ear.copyFrom(this.head);
        this.ear.x = 0.0F;
        this.ear.y = 0.0F;
        this.ear.render(param0, param1, param2, param3);
    }

    public void renderCloak(PoseStack param0, VertexConsumer param1, int param2, int param3) {
        this.cloak.render(param0, param1, param2, param3);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        if (param0.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            if (param0.isCrouching()) {
                this.cloak.z = 1.4F;
                this.cloak.y = 1.85F;
            } else {
                this.cloak.z = 0.0F;
                this.cloak.y = 0.0F;
            }
        } else if (param0.isCrouching()) {
            this.cloak.z = 0.3F;
            this.cloak.y = 0.8F;
        } else {
            this.cloak.z = -1.1F;
            this.cloak.y = -0.85F;
        }

    }

    @Override
    public void setAllVisible(boolean param0) {
        super.setAllVisible(param0);
        this.leftSleeve.visible = param0;
        this.rightSleeve.visible = param0;
        this.leftPants.visible = param0;
        this.rightPants.visible = param0;
        this.jacket.visible = param0;
        this.cloak.visible = param0;
        this.ear.visible = param0;
    }

    @Override
    public void translateToHand(HumanoidArm param0, PoseStack param1) {
        ModelPart var0 = this.getArm(param0);
        if (this.slim) {
            float var1 = 0.5F * (float)(param0 == HumanoidArm.RIGHT ? 1 : -1);
            var0.x += var1;
            var0.translateAndRotate(param1);
            var0.x -= var1;
        } else {
            var0.translateAndRotate(param1);
        }

    }

    public ModelPart getRandomModelPart(Random param0) {
        return this.parts.get(param0.nextInt(this.parts.size()));
    }
}

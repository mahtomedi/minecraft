package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ArmedModel;
import net.minecraft.client.renderer.entity.HeadedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IllagerModel<T extends AbstractIllager> extends EntityModel<T> implements ArmedModel, HeadedModel {
    protected final ModelPart head;
    private final ModelPart hat;
    protected final ModelPart body;
    protected final ModelPart arms;
    protected final ModelPart leftLeg;
    protected final ModelPart rightLeg;
    private final ModelPart nose;
    protected final ModelPart rightArm;
    protected final ModelPart leftArm;
    private float itemUseTicks;

    public IllagerModel(float param0, float param1, int param2, int param3) {
        this.head = new ModelPart(this).setTexSize(param2, param3);
        this.head.setPos(0.0F, 0.0F + param1, 0.0F);
        this.head.texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, param0);
        this.hat = new ModelPart(this, 32, 0).setTexSize(param2, param3);
        this.hat.addBox(-4.0F, -10.0F, -4.0F, 8, 12, 8, param0 + 0.45F);
        this.head.addChild(this.hat);
        this.hat.visible = false;
        this.nose = new ModelPart(this).setTexSize(param2, param3);
        this.nose.setPos(0.0F, param1 - 2.0F, 0.0F);
        this.nose.texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2, 4, 2, param0);
        this.head.addChild(this.nose);
        this.body = new ModelPart(this).setTexSize(param2, param3);
        this.body.setPos(0.0F, 0.0F + param1, 0.0F);
        this.body.texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6, param0);
        this.body.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, param0 + 0.5F);
        this.arms = new ModelPart(this).setTexSize(param2, param3);
        this.arms.setPos(0.0F, 0.0F + param1 + 2.0F, 0.0F);
        this.arms.texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4, 8, 4, param0);
        ModelPart var0 = new ModelPart(this, 44, 22).setTexSize(param2, param3);
        var0.mirror = true;
        var0.addBox(4.0F, -2.0F, -2.0F, 4, 8, 4, param0);
        this.arms.addChild(var0);
        this.arms.texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8, 4, 4, param0);
        this.leftLeg = new ModelPart(this, 0, 22).setTexSize(param2, param3);
        this.leftLeg.setPos(-2.0F, 12.0F + param1, 0.0F);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, param0);
        this.rightLeg = new ModelPart(this, 0, 22).setTexSize(param2, param3);
        this.rightLeg.mirror = true;
        this.rightLeg.setPos(2.0F, 12.0F + param1, 0.0F);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, param0);
        this.rightArm = new ModelPart(this, 40, 46).setTexSize(param2, param3);
        this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, param0);
        this.rightArm.setPos(-5.0F, 2.0F + param1, 0.0F);
        this.leftArm = new ModelPart(this, 40, 46).setTexSize(param2, param3);
        this.leftArm.mirror = true;
        this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, param0);
        this.leftArm.setPos(5.0F, 2.0F + param1, 0.0F);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.head.render(param6);
        this.body.render(param6);
        this.leftLeg.render(param6);
        this.rightLeg.render(param6);
        if (param0.getArmPose() == AbstractIllager.IllagerArmPose.CROSSED) {
            this.arms.render(param6);
        } else {
            this.rightArm.render(param6);
            this.leftArm.render(param6);
        }

    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.arms.y = 3.0F;
        this.arms.z = -1.0F;
        this.arms.xRot = -0.75F;
        if (this.riding) {
            this.rightArm.xRot = (float) (-Math.PI / 5);
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = (float) (-Math.PI / 5);
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = (float) (Math.PI / 10);
            this.leftLeg.zRot = 0.07853982F;
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = (float) (-Math.PI / 10);
            this.rightLeg.zRot = -0.07853982F;
        } else {
            this.rightArm.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 2.0F * param2 * 0.5F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = Mth.cos(param1 * 0.6662F) * 2.0F * param2 * 0.5F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.leftLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2 * 0.5F;
            this.leftLeg.yRot = 0.0F;
            this.leftLeg.zRot = 0.0F;
            this.rightLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2 * 0.5F;
            this.rightLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.0F;
        }

        AbstractIllager.IllagerArmPose var0 = param0.getArmPose();
        if (var0 == AbstractIllager.IllagerArmPose.ATTACKING) {
            float var1 = Mth.sin(this.attackTime * (float) Math.PI);
            float var2 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightArm.yRot = (float) (Math.PI / 20);
            this.leftArm.yRot = (float) (-Math.PI / 20);
            if (param0.getMainArm() == HumanoidArm.RIGHT) {
                this.rightArm.xRot = -1.8849558F + Mth.cos(param3 * 0.09F) * 0.15F;
                this.leftArm.xRot = -0.0F + Mth.cos(param3 * 0.19F) * 0.5F;
                this.rightArm.xRot += var1 * 2.2F - var2 * 0.4F;
                this.leftArm.xRot += var1 * 1.2F - var2 * 0.4F;
            } else {
                this.rightArm.xRot = -0.0F + Mth.cos(param3 * 0.19F) * 0.5F;
                this.leftArm.xRot = -1.8849558F + Mth.cos(param3 * 0.09F) * 0.15F;
                this.rightArm.xRot += var1 * 1.2F - var2 * 0.4F;
                this.leftArm.xRot += var1 * 2.2F - var2 * 0.4F;
            }

            this.rightArm.zRot += Mth.cos(param3 * 0.09F) * 0.05F + 0.05F;
            this.leftArm.zRot -= Mth.cos(param3 * 0.09F) * 0.05F + 0.05F;
            this.rightArm.xRot += Mth.sin(param3 * 0.067F) * 0.05F;
            this.leftArm.xRot -= Mth.sin(param3 * 0.067F) * 0.05F;
        } else if (var0 == AbstractIllager.IllagerArmPose.SPELLCASTING) {
            this.rightArm.z = 0.0F;
            this.rightArm.x = -5.0F;
            this.leftArm.z = 0.0F;
            this.leftArm.x = 5.0F;
            this.rightArm.xRot = Mth.cos(param3 * 0.6662F) * 0.25F;
            this.leftArm.xRot = Mth.cos(param3 * 0.6662F) * 0.25F;
            this.rightArm.zRot = (float) (Math.PI * 3.0 / 4.0);
            this.leftArm.zRot = (float) (-Math.PI * 3.0 / 4.0);
            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
        } else if (var0 == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.rightArm.xRot = ((float) (-Math.PI / 2)) + this.head.xRot;
            this.leftArm.xRot = -0.9424779F + this.head.xRot;
            this.leftArm.yRot = this.head.yRot - 0.4F;
            this.leftArm.zRot = (float) (Math.PI / 2);
        } else if (var0 == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
            this.rightArm.yRot = -0.3F + this.head.yRot;
            this.leftArm.yRot = 0.6F + this.head.yRot;
            this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot + 0.1F;
            this.leftArm.xRot = -1.5F + this.head.xRot;
        } else if (var0 == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE) {
            this.rightArm.yRot = -0.8F;
            this.rightArm.xRot = -0.97079635F;
            this.leftArm.xRot = -0.97079635F;
            float var3 = Mth.clamp(this.itemUseTicks, 0.0F, 25.0F);
            this.leftArm.yRot = Mth.lerp(var3 / 25.0F, 0.4F, 0.85F);
            this.leftArm.xRot = Mth.lerp(var3 / 25.0F, this.leftArm.xRot, (float) (-Math.PI / 2));
        } else if (var0 == AbstractIllager.IllagerArmPose.CELEBRATING) {
            this.rightArm.z = 0.0F;
            this.rightArm.x = -5.0F;
            this.rightArm.xRot = Mth.cos(param3 * 0.6662F) * 0.05F;
            this.rightArm.zRot = 2.670354F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.z = 0.0F;
            this.leftArm.x = 5.0F;
            this.leftArm.xRot = Mth.cos(param3 * 0.6662F) * 0.05F;
            this.leftArm.zRot = (float) (-Math.PI * 3.0 / 4.0);
            this.leftArm.yRot = 0.0F;
        }

    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        this.itemUseTicks = (float)param0.getTicksUsingItem();
        super.prepareMobModel(param0, param1, param2, param3);
    }

    private ModelPart getArm(HumanoidArm param0) {
        return param0 == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    public ModelPart getHat() {
        return this.hat;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void translateToHand(float param0, HumanoidArm param1) {
        this.getArm(param1).translateTo(0.0625F);
    }
}

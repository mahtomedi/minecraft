package net.minecraft.client.model;

import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieVillagerModel<T extends Zombie> extends HumanoidModel<T> implements VillagerHeadModel {
    private ModelPart hatRim;

    public ZombieVillagerModel(Function<ResourceLocation, RenderType> param0, float param1, boolean param2) {
        super(param0, param1, 0.0F, 64, param2 ? 32 : 64);
        if (param2) {
            this.head = new ModelPart(this, 0, 0);
            this.head.addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, param1);
            this.body = new ModelPart(this, 16, 16);
            this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, param1 + 0.1F);
            this.rightLeg = new ModelPart(this, 0, 16);
            this.rightLeg.setPos(-2.0F, 12.0F, 0.0F);
            this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1 + 0.1F);
            this.leftLeg = new ModelPart(this, 0, 16);
            this.leftLeg.mirror = true;
            this.leftLeg.setPos(2.0F, 12.0F, 0.0F);
            this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1 + 0.1F);
        } else {
            this.head = new ModelPart(this, 0, 0);
            this.head.texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, param1);
            this.head.texOffs(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F, param1);
            this.hat = new ModelPart(this, 32, 0);
            this.hat.addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, param1 + 0.5F);
            this.hatRim = new ModelPart(this);
            this.hatRim.texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, param1);
            this.hatRim.xRot = (float) (-Math.PI / 2);
            this.hat.addChild(this.hatRim);
            this.body = new ModelPart(this, 16, 20);
            this.body.addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, param1);
            this.body.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, param1 + 0.05F);
            this.rightArm = new ModelPart(this, 44, 22);
            this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1);
            this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
            this.leftArm = new ModelPart(this, 44, 22);
            this.leftArm.mirror = true;
            this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1);
            this.leftArm.setPos(5.0F, 2.0F, 0.0F);
            this.rightLeg = new ModelPart(this, 0, 22);
            this.rightLeg.setPos(-2.0F, 12.0F, 0.0F);
            this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1);
            this.leftLeg = new ModelPart(this, 0, 22);
            this.leftLeg.mirror = true;
            this.leftLeg.setPos(2.0F, 12.0F, 0.0F);
            this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1);
        }

    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        float var0 = Mth.sin(this.attackTime * (float) Math.PI);
        float var1 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightArm.yRot = -(0.1F - var0 * 0.6F);
        this.leftArm.yRot = 0.1F - var0 * 0.6F;
        float var2 = (float) -Math.PI / (param0.isAggressive() ? 1.5F : 2.25F);
        this.rightArm.xRot = var2;
        this.leftArm.xRot = var2;
        this.rightArm.xRot += var0 * 1.2F - var1 * 0.4F;
        this.leftArm.xRot += var0 * 1.2F - var1 * 0.4F;
        this.rightArm.zRot += Mth.cos(param3 * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(param3 * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(param3 * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(param3 * 0.067F) * 0.05F;
    }

    @Override
    public void hatVisible(boolean param0) {
        this.head.visible = param0;
        this.hat.visible = param0;
        this.hatRim.visible = param0;
    }
}

package net.minecraft.client.model;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Random;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerModel<T extends LivingEntity> extends HumanoidModel<T> {
    private List<ModelPart> cubes = Lists.newArrayList();
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final ModelPart cloak;
    private final ModelPart ear;
    private final boolean slim;

    public PlayerModel(float param0, boolean param1) {
        super(param0, 0.0F, 64, 64);
        this.slim = param1;
        this.ear = new ModelPart(this, 24, 0);
        this.ear.addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, param0);
        this.cloak = new ModelPart(this, 0, 0);
        this.cloak.setTexSize(64, 32);
        this.cloak.addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, param0);
        if (param1) {
            this.leftArm = new ModelPart(this, 32, 48);
            this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, param0);
            this.leftArm.setPos(5.0F, 2.5F, 0.0F);
            this.rightArm = new ModelPart(this, 40, 16);
            this.rightArm.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, param0);
            this.rightArm.setPos(-5.0F, 2.5F, 0.0F);
            this.leftSleeve = new ModelPart(this, 48, 48);
            this.leftSleeve.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, param0 + 0.25F);
            this.leftSleeve.setPos(5.0F, 2.5F, 0.0F);
            this.rightSleeve = new ModelPart(this, 40, 32);
            this.rightSleeve.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, param0 + 0.25F);
            this.rightSleeve.setPos(-5.0F, 2.5F, 10.0F);
        } else {
            this.leftArm = new ModelPart(this, 32, 48);
            this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
            this.leftArm.setPos(5.0F, 2.0F, 0.0F);
            this.leftSleeve = new ModelPart(this, 48, 48);
            this.leftSleeve.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0 + 0.25F);
            this.leftSleeve.setPos(5.0F, 2.0F, 0.0F);
            this.rightSleeve = new ModelPart(this, 40, 32);
            this.rightSleeve.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0 + 0.25F);
            this.rightSleeve.setPos(-5.0F, 2.0F, 10.0F);
        }

        this.leftLeg = new ModelPart(this, 16, 48);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.leftPants = new ModelPart(this, 0, 48);
        this.leftPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0 + 0.25F);
        this.leftPants.setPos(1.9F, 12.0F, 0.0F);
        this.rightPants = new ModelPart(this, 0, 32);
        this.rightPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0 + 0.25F);
        this.rightPants.setPos(-1.9F, 12.0F, 0.0F);
        this.jacket = new ModelPart(this, 16, 32);
        this.jacket.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, param0 + 0.25F);
        this.jacket.setPos(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.render(param0, param1, param2, param3, param4, param5, param6);
        RenderSystem.pushMatrix();
        if (this.young) {
            float var0 = 2.0F;
            RenderSystem.scalef(0.5F, 0.5F, 0.5F);
            RenderSystem.translatef(0.0F, 24.0F * param6, 0.0F);
            this.leftPants.render(param6);
            this.rightPants.render(param6);
            this.leftSleeve.render(param6);
            this.rightSleeve.render(param6);
            this.jacket.render(param6);
        } else {
            if (param0.isCrouching()) {
                RenderSystem.translatef(0.0F, 0.2F, 0.0F);
            }

            this.leftPants.render(param6);
            this.rightPants.render(param6);
            this.leftSleeve.render(param6);
            this.rightSleeve.render(param6);
            this.jacket.render(param6);
        }

        RenderSystem.popMatrix();
    }

    public void renderEars(float param0) {
        this.ear.copyFrom(this.head);
        this.ear.x = 0.0F;
        this.ear.y = 0.0F;
        this.ear.render(param0);
    }

    public void renderCloak(float param0) {
        this.cloak.render(param0);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        if (param0.isCrouching()) {
            this.cloak.y = 2.0F;
        } else {
            this.cloak.y = 0.0F;
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
    public void translateToHand(float param0, HumanoidArm param1) {
        ModelPart var0 = this.getArm(param1);
        if (this.slim) {
            float var1 = 0.5F * (float)(param1 == HumanoidArm.RIGHT ? 1 : -1);
            var0.x += var1;
            var0.translateTo(param0);
            var0.x -= var1;
        } else {
            var0.translateTo(param0);
        }

    }

    public ModelPart getRandomModelPart(Random param0) {
        return this.cubes.get(param0.nextInt(this.cubes.size()));
    }

    @Override
    public void accept(ModelPart param0) {
        if (this.cubes == null) {
            this.cubes = Lists.newArrayList();
        }

        this.cubes.add(param0);
    }
}

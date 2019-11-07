package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Vex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VexModel extends HumanoidModel<Vex> {
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public VexModel() {
        super(0.0F, 0.0F, 64, 64);
        this.leftLeg.visible = false;
        this.hat.visible = false;
        this.rightLeg = new ModelPart(this, 32, 0);
        this.rightLeg.addBox(-1.0F, -1.0F, -2.0F, 6.0F, 10.0F, 4.0F, 0.0F);
        this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        this.rightWing = new ModelPart(this, 0, 32);
        this.rightWing.addBox(-20.0F, 0.0F, 0.0F, 20.0F, 12.0F, 1.0F);
        this.leftWing = new ModelPart(this, 0, 32);
        this.leftWing.mirror = true;
        this.leftWing.addBox(0.0F, 0.0F, 0.0F, 20.0F, 12.0F, 1.0F);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.rightWing, this.leftWing));
    }

    public void setupAnim(Vex param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        if (param0.isCharging()) {
            if (param0.getMainArm() == HumanoidArm.RIGHT) {
                this.rightArm.xRot = 3.7699115F;
            } else {
                this.leftArm.xRot = 3.7699115F;
            }
        }

        this.rightLeg.xRot += (float) (Math.PI / 5);
        this.rightWing.z = 2.0F;
        this.leftWing.z = 2.0F;
        this.rightWing.y = 1.0F;
        this.leftWing.y = 1.0F;
        this.rightWing.yRot = 0.47123894F + Mth.cos(param3 * 0.8F) * (float) Math.PI * 0.05F;
        this.leftWing.yRot = -this.rightWing.yRot;
        this.leftWing.zRot = -0.47123894F;
        this.leftWing.xRot = 0.47123894F;
        this.rightWing.xRot = 0.47123894F;
        this.rightWing.zRot = 0.47123894F;
    }
}

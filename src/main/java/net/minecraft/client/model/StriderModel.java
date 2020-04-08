package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Strider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StriderModel<T extends Strider> extends ListModel<T> {
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart body;
    private final ModelPart bristle0;
    private final ModelPart bristle1;
    private final ModelPart bristle2;
    private final ModelPart bristle3;
    private final ModelPart bristle4;
    private final ModelPart bristle5;

    public StriderModel() {
        this.texWidth = 64;
        this.texHeight = 128;
        this.rightLeg = new ModelPart(this, 0, 32);
        this.rightLeg.setPos(-4.0F, 8.0F, 0.0F);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F, 0.0F);
        this.leftLeg = new ModelPart(this, 0, 32);
        this.leftLeg.setPos(4.0F, 8.0F, 0.0F);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F, 0.0F);
        this.body = new ModelPart(this, 0, 0);
        this.body.setPos(0.0F, 1.0F, 0.0F);
        this.body.addBox(-8.0F, -6.0F, -8.0F, 16.0F, 14.0F, 16.0F, 0.0F);
        this.bristle0 = new ModelPart(this, 16, 65);
        this.bristle0.setPos(-8.0F, 4.0F, -8.0F);
        this.bristle0.addBox(-12.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F, true);
        this.setRotationAngle(this.bristle0, 0.0F, 0.0F, -1.2217305F);
        this.bristle1 = new ModelPart(this, 16, 49);
        this.bristle1.setPos(-8.0F, -1.0F, -8.0F);
        this.bristle1.addBox(-12.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F, true);
        this.setRotationAngle(this.bristle1, 0.0F, 0.0F, -1.134464F);
        this.bristle2 = new ModelPart(this, 16, 33);
        this.bristle2.setPos(-8.0F, -5.0F, -8.0F);
        this.bristle2.addBox(-12.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F, true);
        this.setRotationAngle(this.bristle2, 0.0F, 0.0F, -0.87266463F);
        this.bristle3 = new ModelPart(this, 16, 33);
        this.bristle3.setPos(8.0F, -6.0F, -8.0F);
        this.bristle3.addBox(0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F);
        this.setRotationAngle(this.bristle3, 0.0F, 0.0F, 0.87266463F);
        this.bristle4 = new ModelPart(this, 16, 49);
        this.bristle4.setPos(8.0F, -2.0F, -8.0F);
        this.bristle4.addBox(0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F);
        this.setRotationAngle(this.bristle4, 0.0F, 0.0F, 1.134464F);
        this.bristle5 = new ModelPart(this, 16, 65);
        this.bristle5.setPos(8.0F, 3.0F, -8.0F);
        this.bristle5.addBox(0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F);
        this.setRotationAngle(this.bristle5, 0.0F, 0.0F, 1.2217305F);
        this.body.addChild(this.bristle0);
        this.body.addChild(this.bristle1);
        this.body.addChild(this.bristle2);
        this.body.addChild(this.bristle3);
        this.body.addChild(this.bristle4);
        this.body.addChild(this.bristle5);
    }

    public void setupAnim(Strider param0, float param1, float param2, float param3, float param4, float param5) {
        param2 = Math.min(0.25F, param2);
        if (param0.getPassengers().size() <= 0) {
            this.body.xRot = param5 * (float) (Math.PI / 180.0);
            this.body.yRot = param4 * (float) (Math.PI / 180.0);
        } else {
            this.body.xRot = 0.0F;
            this.body.yRot = 0.0F;
        }

        float var0 = 1.5F;
        this.body.zRot = 0.1F * Mth.sin(param1 * 1.5F) * 4.0F * param2;
        this.body.y = 2.0F;
        this.body.y -= 2.0F * Mth.cos(param1 * 1.5F) * 2.0F * param2;
        this.leftLeg.xRot = Mth.sin(param1 * 1.5F * 0.5F) * 2.0F * param2;
        this.rightLeg.xRot = Mth.sin(param1 * 1.5F * 0.5F + (float) Math.PI) * 2.0F * param2;
        this.leftLeg.zRot = (float) (Math.PI / 18) * Mth.cos(param1 * 1.5F * 0.5F) * param2;
        this.rightLeg.zRot = (float) (Math.PI / 18) * Mth.cos(param1 * 1.5F * 0.5F + (float) Math.PI) * param2;
        this.leftLeg.y = 8.0F + 2.0F * Mth.sin(param1 * 1.5F * 0.5F + (float) Math.PI) * 2.0F * param2;
        this.rightLeg.y = 8.0F + 2.0F * Mth.sin(param1 * 1.5F * 0.5F) * 2.0F * param2;
        this.bristle0.zRot = -1.2217305F;
        this.bristle1.zRot = -1.134464F;
        this.bristle2.zRot = -0.87266463F;
        this.bristle3.zRot = 0.87266463F;
        this.bristle4.zRot = 1.134464F;
        this.bristle5.zRot = 1.2217305F;
        float var1 = Mth.cos(param1 * 1.5F + (float) Math.PI) * param2;
        this.bristle0.zRot += var1 * 1.3F;
        this.bristle1.zRot += var1 * 1.2F;
        this.bristle2.zRot += var1 * 0.6F;
        this.bristle3.zRot += var1 * 0.6F;
        this.bristle4.zRot += var1 * 1.2F;
        this.bristle5.zRot += var1 * 1.3F;
        float var2 = 1.0F;
        float var3 = 1.0F;
        this.bristle0.zRot += 0.05F * Mth.sin(param3 * 1.0F * -0.4F);
        this.bristle1.zRot += 0.1F * Mth.sin(param3 * 1.0F * 0.2F);
        this.bristle2.zRot += 0.1F * Mth.sin(param3 * 1.0F * 0.4F);
        this.bristle3.zRot += 0.1F * Mth.sin(param3 * 1.0F * 0.4F);
        this.bristle4.zRot += 0.1F * Mth.sin(param3 * 1.0F * 0.2F);
        this.bristle5.zRot += 0.05F * Mth.sin(param3 * 1.0F * -0.4F);
    }

    public void setRotationAngle(ModelPart param0, float param1, float param2, float param3) {
        param0.xRot = param1;
        param0.yRot = param2;
        param0.zRot = param3;
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.body, this.leftLeg, this.rightLeg);
    }
}

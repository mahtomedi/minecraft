package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PolarBearModel<T extends PolarBear> extends QuadrupedModel<T> {
    public PolarBearModel() {
        super(12, 0.0F, true, 16.0F, 4.0F, 2.25F, 2.0F, 24);
        this.texWidth = 128;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-3.5F, -3.0F, -3.0F, 7.0F, 7.0F, 7.0F, 0.0F);
        this.head.setPos(0.0F, 10.0F, -16.0F);
        this.head.texOffs(0, 44).addBox(-2.5F, 1.0F, -6.0F, 5.0F, 3.0F, 3.0F, 0.0F);
        this.head.texOffs(26, 0).addBox(-4.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F, 0.0F);
        ModelPart var0 = this.head.texOffs(26, 0);
        var0.mirror = true;
        var0.addBox(2.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F, 0.0F);
        this.body = new ModelPart(this);
        this.body.texOffs(0, 19).addBox(-5.0F, -13.0F, -7.0F, 14.0F, 14.0F, 11.0F, 0.0F);
        this.body.texOffs(39, 0).addBox(-4.0F, -25.0F, -7.0F, 12.0F, 12.0F, 10.0F, 0.0F);
        this.body.setPos(-2.0F, 9.0F, 12.0F);
        int var1 = 10;
        this.leg0 = new ModelPart(this, 50, 22);
        this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F, 0.0F);
        this.leg0.setPos(-3.5F, 14.0F, 6.0F);
        this.leg1 = new ModelPart(this, 50, 22);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F, 0.0F);
        this.leg1.setPos(3.5F, 14.0F, 6.0F);
        this.leg2 = new ModelPart(this, 50, 40);
        this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F, 0.0F);
        this.leg2.setPos(-2.5F, 14.0F, -7.0F);
        this.leg3 = new ModelPart(this, 50, 40);
        this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F, 0.0F);
        this.leg3.setPos(2.5F, 14.0F, -7.0F);
        --this.leg0.x;
        ++this.leg1.x;
        this.leg0.z += 0.0F;
        this.leg1.z += 0.0F;
        --this.leg2.x;
        ++this.leg3.x;
        --this.leg2.z;
        --this.leg3.z;
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        float var0 = param3 - (float)param0.tickCount;
        float var1 = param0.getStandingAnimationScale(var0);
        var1 *= var1;
        float var2 = 1.0F - var1;
        this.body.xRot = (float) (Math.PI / 2) - var1 * (float) Math.PI * 0.35F;
        this.body.y = 9.0F * var2 + 11.0F * var1;
        this.leg2.y = 14.0F * var2 - 6.0F * var1;
        this.leg2.z = -8.0F * var2 - 4.0F * var1;
        this.leg2.xRot -= var1 * (float) Math.PI * 0.45F;
        this.leg3.y = this.leg2.y;
        this.leg3.z = this.leg2.z;
        this.leg3.xRot -= var1 * (float) Math.PI * 0.45F;
        if (this.young) {
            this.head.y = 10.0F * var2 - 9.0F * var1;
            this.head.z = -16.0F * var2 - 7.0F * var1;
        } else {
            this.head.y = 10.0F * var2 - 14.0F * var1;
            this.head.z = -16.0F * var2 - 3.0F * var1;
        }

        this.head.xRot += var1 * (float) Math.PI * 0.15F;
    }
}

package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemModel<T extends IronGolem> extends ListModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart arm0;
    private final ModelPart arm1;
    private final ModelPart leg0;
    private final ModelPart leg1;

    public IronGolemModel() {
        int var0 = 128;
        int var1 = 128;
        this.head = new ModelPart(this).setTexSize(128, 128);
        this.head.setPos(0.0F, -7.0F, -2.0F);
        this.head.texOffs(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F, 0.0F);
        this.head.texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F, 0.0F);
        this.body = new ModelPart(this).setTexSize(128, 128);
        this.body.setPos(0.0F, -7.0F, 0.0F);
        this.body.texOffs(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F, 0.0F);
        this.body.texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, 0.5F);
        this.arm0 = new ModelPart(this).setTexSize(128, 128);
        this.arm0.setPos(0.0F, -7.0F, 0.0F);
        this.arm0.texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, 0.0F);
        this.arm1 = new ModelPart(this).setTexSize(128, 128);
        this.arm1.setPos(0.0F, -7.0F, 0.0F);
        this.arm1.texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, 0.0F);
        this.leg0 = new ModelPart(this, 0, 22).setTexSize(128, 128);
        this.leg0.setPos(-4.0F, 11.0F, 0.0F);
        this.leg0.texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, 0.0F);
        this.leg1 = new ModelPart(this, 0, 22).setTexSize(128, 128);
        this.leg1.mirror = true;
        this.leg1.texOffs(60, 0).setPos(5.0F, 11.0F, 0.0F);
        this.leg1.addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, 0.0F);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.arm0, this.arm1);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.leg0.xRot = -1.5F * this.triangleWave(param1, 13.0F) * param2;
        this.leg1.xRot = 1.5F * this.triangleWave(param1, 13.0F) * param2;
        this.leg0.yRot = 0.0F;
        this.leg1.yRot = 0.0F;
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        int var0 = param0.getAttackAnimationTick();
        if (var0 > 0) {
            this.arm0.xRot = -2.0F + 1.5F * this.triangleWave((float)var0 - param3, 10.0F);
            this.arm1.xRot = -2.0F + 1.5F * this.triangleWave((float)var0 - param3, 10.0F);
        } else {
            int var1 = param0.getOfferFlowerTick();
            if (var1 > 0) {
                this.arm0.xRot = -0.8F + 0.025F * this.triangleWave((float)var1, 70.0F);
                this.arm1.xRot = 0.0F;
            } else {
                this.arm0.xRot = (-0.2F + 1.5F * this.triangleWave(param1, 13.0F)) * param2;
                this.arm1.xRot = (-0.2F - 1.5F * this.triangleWave(param1, 13.0F)) * param2;
            }
        }

    }

    private float triangleWave(float param0, float param1) {
        return (Math.abs(param0 % param1 - param1 * 0.5F) - param1 * 0.25F) / (param1 * 0.25F);
    }

    public ModelPart getFlowerHoldingArm() {
        return this.arm0;
    }
}

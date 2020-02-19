package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HoglinModel extends AgeableListModel<Hoglin> {
    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart body;
    private final ModelPart frontRightLeg;
    private final ModelPart frontLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart backLeftLeg;

    public HoglinModel() {
        super(true, 8.0F, 6.0F, 1.9F, 2.0F, 24.0F);
        this.texWidth = 128;
        this.texHeight = 128;
        this.body = new ModelPart(this);
        this.body.setPos(0.0F, 7.0F, 0.0F);
        this.body.texOffs(1, 1).addBox(-8.0F, -7.0F, -13.0F, 16.0F, 14.0F, 26.0F);
        ModelPart var0 = new ModelPart(this);
        var0.setPos(0.0F, -14.0F, -7.0F);
        var0.texOffs(5, 67).addBox(0.0F, 0.0F, -9.0F, 0.0F, 10.0F, 19.0F, 0.001F);
        this.body.addChild(var0);
        this.head = new ModelPart(this);
        this.head.setPos(0.0F, 2.0F, -12.0F);
        this.head.texOffs(1, 42).addBox(-7.0F, -3.0F, -19.0F, 14.0F, 6.0F, 19.0F);
        this.rightEar = new ModelPart(this);
        this.rightEar.setPos(-6.0F, -2.0F, -3.0F);
        this.rightEar.texOffs(4, 16).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F);
        this.rightEar.zRot = (float) (-Math.PI * 2.0 / 9.0);
        this.head.addChild(this.rightEar);
        this.leftEar = new ModelPart(this);
        this.leftEar.setPos(6.0F, -2.0F, -3.0F);
        this.leftEar.texOffs(4, 21).addBox(0.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F);
        this.leftEar.zRot = (float) (Math.PI * 2.0 / 9.0);
        this.head.addChild(this.leftEar);
        ModelPart var1 = new ModelPart(this);
        var1.setPos(-7.0F, 2.0F, -12.0F);
        var1.texOffs(6, 45).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F);
        this.head.addChild(var1);
        ModelPart var2 = new ModelPart(this);
        var2.setPos(7.0F, 2.0F, -12.0F);
        var2.texOffs(6, 45).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F);
        this.head.addChild(var2);
        this.head.xRot = 0.87266463F;
        int var3 = 14;
        int var4 = 11;
        this.frontRightLeg = new ModelPart(this);
        this.frontRightLeg.setPos(-4.0F, 10.0F, -8.5F);
        this.frontRightLeg.texOffs(46, 75).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
        this.frontLeftLeg = new ModelPart(this);
        this.frontLeftLeg.setPos(4.0F, 10.0F, -8.5F);
        this.frontLeftLeg.texOffs(71, 75).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
        this.backRightLeg = new ModelPart(this);
        this.backRightLeg.setPos(-5.0F, 13.0F, 10.0F);
        this.backRightLeg.texOffs(51, 43).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F);
        this.backLeftLeg = new ModelPart(this);
        this.backLeftLeg.setPos(5.0F, 13.0F, 10.0F);
        this.backLeftLeg.texOffs(72, 43).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.frontRightLeg, this.frontLeftLeg, this.backRightLeg, this.backLeftLeg);
    }

    public void setupAnim(Hoglin param0, float param1, float param2, float param3, float param4, float param5) {
        this.rightEar.zRot = (float) (-Math.PI * 2.0 / 9.0) - param2 * Mth.sin(param1);
        this.leftEar.zRot = (float) (Math.PI * 2.0 / 9.0) + param2 * Mth.sin(param1);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        int var0 = param0.getAttackAnimationRemainingTicks();
        if (var0 > 0) {
            int var1 = 10 - var0;
            float var2 = Mth.triangleWave((float)var1, 10.0F);
            float var3 = (-var2 + 1.0F) / 2.0F;
            float var4 = -1.2217305F * var3;
            this.head.xRot = 0.87266463F + var4;
        } else {
            this.head.xRot = 0.87266463F;
        }

        float var5 = 1.2F;
        this.frontRightLeg.xRot = Mth.cos(param1) * 1.2F * param2;
        this.frontLeftLeg.xRot = Mth.cos(param1 + (float) Math.PI) * 1.2F * param2;
        this.backRightLeg.xRot = this.frontLeftLeg.xRot;
        this.backLeftLeg.xRot = this.frontRightLeg.xRot;
    }
}
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseModel<T extends AbstractHorse> extends AgeableListModel<T> {
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart babyLeg1;
    private final ModelPart babyLeg2;
    private final ModelPart babyLeg3;
    private final ModelPart babyLeg4;
    private final ModelPart tail;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public HorseModel(float param0) {
        super(true, 16.2F, 1.36F, 2.7272F, 2.0F, 20.0F);
        this.texWidth = 64;
        this.texHeight = 64;
        this.body = new ModelPart(this, 0, 32);
        this.body.addBox(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F, 0.05F);
        this.body.setPos(0.0F, 11.0F, 5.0F);
        this.headParts = new ModelPart(this, 0, 35);
        this.headParts.addBox(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F);
        this.headParts.xRot = (float) (Math.PI / 6);
        ModelPart var0 = new ModelPart(this, 0, 13);
        var0.addBox(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F, param0);
        ModelPart var1 = new ModelPart(this, 56, 36);
        var1.addBox(-1.0F, -11.0F, 5.01F, 2.0F, 16.0F, 2.0F, param0);
        ModelPart var2 = new ModelPart(this, 0, 25);
        var2.addBox(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F, param0);
        this.headParts.addChild(var0);
        this.headParts.addChild(var1);
        this.headParts.addChild(var2);
        this.addEarModels(this.headParts);
        this.leg1 = new ModelPart(this, 48, 21);
        this.leg1.mirror = true;
        this.leg1.addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, param0);
        this.leg1.setPos(4.0F, 14.0F, 7.0F);
        this.leg2 = new ModelPart(this, 48, 21);
        this.leg2.addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, param0);
        this.leg2.setPos(-4.0F, 14.0F, 7.0F);
        this.leg3 = new ModelPart(this, 48, 21);
        this.leg3.mirror = true;
        this.leg3.addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, param0);
        this.leg3.setPos(4.0F, 6.0F, -12.0F);
        this.leg4 = new ModelPart(this, 48, 21);
        this.leg4.addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, param0);
        this.leg4.setPos(-4.0F, 6.0F, -12.0F);
        float var3 = 5.5F;
        this.babyLeg1 = new ModelPart(this, 48, 21);
        this.babyLeg1.mirror = true;
        this.babyLeg1.addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, param0, param0 + 5.5F, param0);
        this.babyLeg1.setPos(4.0F, 14.0F, 7.0F);
        this.babyLeg2 = new ModelPart(this, 48, 21);
        this.babyLeg2.addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, param0, param0 + 5.5F, param0);
        this.babyLeg2.setPos(-4.0F, 14.0F, 7.0F);
        this.babyLeg3 = new ModelPart(this, 48, 21);
        this.babyLeg3.mirror = true;
        this.babyLeg3.addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, param0, param0 + 5.5F, param0);
        this.babyLeg3.setPos(4.0F, 6.0F, -12.0F);
        this.babyLeg4 = new ModelPart(this, 48, 21);
        this.babyLeg4.addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, param0, param0 + 5.5F, param0);
        this.babyLeg4.setPos(-4.0F, 6.0F, -12.0F);
        this.tail = new ModelPart(this, 42, 36);
        this.tail.addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F, param0);
        this.tail.setPos(0.0F, -5.0F, 2.0F);
        this.tail.xRot = (float) (Math.PI / 6);
        this.body.addChild(this.tail);
        ModelPart var4 = new ModelPart(this, 26, 0);
        var4.addBox(-5.0F, -8.0F, -9.0F, 10.0F, 9.0F, 9.0F, 0.5F);
        this.body.addChild(var4);
        ModelPart var5 = new ModelPart(this, 29, 5);
        var5.addBox(2.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, param0);
        this.headParts.addChild(var5);
        ModelPart var6 = new ModelPart(this, 29, 5);
        var6.addBox(-3.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, param0);
        this.headParts.addChild(var6);
        ModelPart var7 = new ModelPart(this, 32, 2);
        var7.addBox(3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F, param0);
        var7.xRot = (float) (-Math.PI / 6);
        this.headParts.addChild(var7);
        ModelPart var8 = new ModelPart(this, 32, 2);
        var8.addBox(-3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F, param0);
        var8.xRot = (float) (-Math.PI / 6);
        this.headParts.addChild(var8);
        ModelPart var9 = new ModelPart(this, 1, 1);
        var9.addBox(-3.0F, -11.0F, -1.9F, 6.0F, 5.0F, 6.0F, 0.2F);
        this.headParts.addChild(var9);
        ModelPart var10 = new ModelPart(this, 19, 0);
        var10.addBox(-2.0F, -11.0F, -4.0F, 4.0F, 5.0F, 2.0F, 0.2F);
        this.headParts.addChild(var10);
        this.saddleParts = new ModelPart[]{var4, var5, var6, var9, var10};
        this.ridingParts = new ModelPart[]{var7, var8};
    }

    protected void addEarModels(ModelPart param0) {
        ModelPart var0 = new ModelPart(this, 19, 16);
        var0.addBox(0.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, -0.001F);
        ModelPart var1 = new ModelPart(this, 19, 16);
        var1.addBox(-2.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, -0.001F);
        param0.addChild(var0);
        param0.addChild(var1);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        boolean var0 = param0.isSaddled();
        boolean var1 = param0.isVehicle();

        for(ModelPart var2 : this.saddleParts) {
            var2.visible = var0;
        }

        for(ModelPart var3 : this.ridingParts) {
            var3.visible = var1 && var0;
        }

        this.body.y = 11.0F;
    }

    @Override
    public Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.headParts);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.leg1, this.leg2, this.leg3, this.leg4, this.babyLeg1, this.babyLeg2, this.babyLeg3, this.babyLeg4);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        float var0 = Mth.rotlerp(param0.yBodyRotO, param0.yBodyRot, param3);
        float var1 = Mth.rotlerp(param0.yHeadRotO, param0.yHeadRot, param3);
        float var2 = Mth.lerp(param3, param0.xRotO, param0.xRot);
        float var3 = var1 - var0;
        float var4 = var2 * (float) (Math.PI / 180.0);
        if (var3 > 20.0F) {
            var3 = 20.0F;
        }

        if (var3 < -20.0F) {
            var3 = -20.0F;
        }

        if (param2 > 0.2F) {
            var4 += Mth.cos(param1 * 0.4F) * 0.15F * param2;
        }

        float var5 = param0.getEatAnim(param3);
        float var6 = param0.getStandAnim(param3);
        float var7 = 1.0F - var6;
        float var8 = param0.getMouthAnim(param3);
        boolean var9 = param0.tailCounter != 0;
        float var10 = (float)param0.tickCount + param3;
        this.headParts.y = 4.0F;
        this.headParts.z = -12.0F;
        this.body.xRot = 0.0F;
        this.headParts.xRot = (float) (Math.PI / 6) + var4;
        this.headParts.yRot = var3 * (float) (Math.PI / 180.0);
        float var11 = param0.isInWater() ? 0.2F : 1.0F;
        float var12 = Mth.cos(var11 * param1 * 0.6662F + (float) Math.PI);
        float var13 = var12 * 0.8F * param2;
        float var14 = (1.0F - Math.max(var6, var5)) * ((float) (Math.PI / 6) + var4 + var8 * Mth.sin(var10) * 0.05F);
        this.headParts.xRot = var6 * ((float) (Math.PI / 12) + var4) + var5 * (2.1816616F + Mth.sin(var10) * 0.05F) + var14;
        this.headParts.yRot = var6 * var3 * (float) (Math.PI / 180.0) + (1.0F - Math.max(var6, var5)) * this.headParts.yRot;
        this.headParts.y = var6 * -4.0F + var5 * 11.0F + (1.0F - Math.max(var6, var5)) * this.headParts.y;
        this.headParts.z = var6 * -4.0F + var5 * -12.0F + (1.0F - Math.max(var6, var5)) * this.headParts.z;
        this.body.xRot = var6 * (float) (-Math.PI / 4) + var7 * this.body.xRot;
        float var15 = (float) (Math.PI / 12) * var6;
        float var16 = Mth.cos(var10 * 0.6F + (float) Math.PI);
        this.leg3.y = 2.0F * var6 + 14.0F * var7;
        this.leg3.z = -6.0F * var6 - 10.0F * var7;
        this.leg4.y = this.leg3.y;
        this.leg4.z = this.leg3.z;
        float var17 = ((float) (-Math.PI / 3) + var16) * var6 + var13 * var7;
        float var18 = ((float) (-Math.PI / 3) - var16) * var6 - var13 * var7;
        this.leg1.xRot = var15 - var12 * 0.5F * param2 * var7;
        this.leg2.xRot = var15 + var12 * 0.5F * param2 * var7;
        this.leg3.xRot = var17;
        this.leg4.xRot = var18;
        this.tail.xRot = (float) (Math.PI / 6) + param2 * 0.75F;
        this.tail.y = -5.0F + param2;
        this.tail.z = 2.0F + param2 * 2.0F;
        if (var9) {
            this.tail.yRot = Mth.cos(var10 * 0.7F);
        } else {
            this.tail.yRot = 0.0F;
        }

        this.babyLeg1.y = this.leg1.y;
        this.babyLeg1.z = this.leg1.z;
        this.babyLeg1.xRot = this.leg1.xRot;
        this.babyLeg2.y = this.leg2.y;
        this.babyLeg2.z = this.leg2.z;
        this.babyLeg2.xRot = this.leg2.xRot;
        this.babyLeg3.y = this.leg3.y;
        this.babyLeg3.z = this.leg3.z;
        this.babyLeg3.xRot = this.leg3.xRot;
        this.babyLeg4.y = this.leg4.y;
        this.babyLeg4.z = this.leg4.z;
        this.babyLeg4.xRot = this.leg4.xRot;
        boolean var19 = param0.isBaby();
        this.leg1.visible = !var19;
        this.leg2.visible = !var19;
        this.leg3.visible = !var19;
        this.leg4.visible = !var19;
        this.babyLeg1.visible = var19;
        this.babyLeg2.visible = var19;
        this.babyLeg3.visible = var19;
        this.babyLeg4.visible = var19;
        this.body.y = var19 ? 10.8F : 0.0F;
    }
}

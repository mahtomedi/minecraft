package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body0;
    private final ModelPart body1;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart leg7;

    public SpiderModel() {
        float var0 = 0.0F;
        int var1 = 15;
        this.head = new ModelPart(this, 32, 4);
        this.head.addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F);
        this.head.setPos(0.0F, 15.0F, -3.0F);
        this.body0 = new ModelPart(this, 0, 0);
        this.body0.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, 0.0F);
        this.body0.setPos(0.0F, 15.0F, 0.0F);
        this.body1 = new ModelPart(this, 0, 12);
        this.body1.addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F, 0.0F);
        this.body1.setPos(0.0F, 15.0F, 9.0F);
        this.leg0 = new ModelPart(this, 18, 0);
        this.leg0.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
        this.leg0.setPos(-4.0F, 15.0F, 2.0F);
        this.leg1 = new ModelPart(this, 18, 0);
        this.leg1.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
        this.leg1.setPos(4.0F, 15.0F, 2.0F);
        this.leg2 = new ModelPart(this, 18, 0);
        this.leg2.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
        this.leg2.setPos(-4.0F, 15.0F, 1.0F);
        this.leg3 = new ModelPart(this, 18, 0);
        this.leg3.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
        this.leg3.setPos(4.0F, 15.0F, 1.0F);
        this.leg4 = new ModelPart(this, 18, 0);
        this.leg4.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
        this.leg4.setPos(-4.0F, 15.0F, 0.0F);
        this.leg5 = new ModelPart(this, 18, 0);
        this.leg5.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
        this.leg5.setPos(4.0F, 15.0F, 0.0F);
        this.leg6 = new ModelPart(this, 18, 0);
        this.leg6.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
        this.leg6.setPos(-4.0F, 15.0F, -1.0F);
        this.leg7 = new ModelPart(this, 18, 0);
        this.leg7.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
        this.leg7.setPos(4.0F, 15.0F, -1.0F);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.head.render(param6);
        this.body0.render(param6);
        this.body1.render(param6);
        this.leg0.render(param6);
        this.leg1.render(param6);
        this.leg2.render(param6);
        this.leg3.render(param6);
        this.leg4.render(param6);
        this.leg5.render(param6);
        this.leg6.render(param6);
        this.leg7.render(param6);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        float var0 = (float) (Math.PI / 4);
        this.leg0.zRot = (float) (-Math.PI / 4);
        this.leg1.zRot = (float) (Math.PI / 4);
        this.leg2.zRot = -0.58119464F;
        this.leg3.zRot = 0.58119464F;
        this.leg4.zRot = -0.58119464F;
        this.leg5.zRot = 0.58119464F;
        this.leg6.zRot = (float) (-Math.PI / 4);
        this.leg7.zRot = (float) (Math.PI / 4);
        float var1 = -0.0F;
        float var2 = (float) (Math.PI / 8);
        this.leg0.yRot = (float) (Math.PI / 4);
        this.leg1.yRot = (float) (-Math.PI / 4);
        this.leg2.yRot = (float) (Math.PI / 8);
        this.leg3.yRot = (float) (-Math.PI / 8);
        this.leg4.yRot = (float) (-Math.PI / 8);
        this.leg5.yRot = (float) (Math.PI / 8);
        this.leg6.yRot = (float) (-Math.PI / 4);
        this.leg7.yRot = (float) (Math.PI / 4);
        float var3 = -(Mth.cos(param1 * 0.6662F * 2.0F + 0.0F) * 0.4F) * param2;
        float var4 = -(Mth.cos(param1 * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * param2;
        float var5 = -(Mth.cos(param1 * 0.6662F * 2.0F + (float) (Math.PI / 2)) * 0.4F) * param2;
        float var6 = -(Mth.cos(param1 * 0.6662F * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * param2;
        float var7 = Math.abs(Mth.sin(param1 * 0.6662F + 0.0F) * 0.4F) * param2;
        float var8 = Math.abs(Mth.sin(param1 * 0.6662F + (float) Math.PI) * 0.4F) * param2;
        float var9 = Math.abs(Mth.sin(param1 * 0.6662F + (float) (Math.PI / 2)) * 0.4F) * param2;
        float var10 = Math.abs(Mth.sin(param1 * 0.6662F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * param2;
        this.leg0.yRot += var3;
        this.leg1.yRot += -var3;
        this.leg2.yRot += var4;
        this.leg3.yRot += -var4;
        this.leg4.yRot += var5;
        this.leg5.yRot += -var5;
        this.leg6.yRot += var6;
        this.leg7.yRot += -var6;
        this.leg0.zRot += var7;
        this.leg1.zRot += -var7;
        this.leg2.zRot += var8;
        this.leg3.zRot += -var8;
        this.leg4.zRot += var9;
        this.leg5.zRot += -var9;
        this.leg6.zRot += var10;
        this.leg7.zRot += -var10;
    }
}

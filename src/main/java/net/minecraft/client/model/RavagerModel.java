package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RavagerModel extends EntityModel<Ravager> {
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart neck;

    public RavagerModel() {
        this.texWidth = 128;
        this.texHeight = 128;
        int var0 = 16;
        float var1 = 0.0F;
        this.neck = new ModelPart(this);
        this.neck.setPos(0.0F, -7.0F, -1.5F);
        this.neck.texOffs(68, 73).addBox(-5.0F, -1.0F, -18.0F, 10, 10, 18, 0.0F);
        this.head = new ModelPart(this);
        this.head.setPos(0.0F, 16.0F, -17.0F);
        this.head.texOffs(0, 0).addBox(-8.0F, -20.0F, -14.0F, 16, 20, 16, 0.0F);
        this.head.texOffs(0, 0).addBox(-2.0F, -6.0F, -18.0F, 4, 8, 4, 0.0F);
        ModelPart var2 = new ModelPart(this);
        var2.setPos(-10.0F, -14.0F, -8.0F);
        var2.texOffs(74, 55).addBox(0.0F, -14.0F, -2.0F, 2, 14, 4, 0.0F);
        var2.xRot = 1.0995574F;
        this.head.addChild(var2);
        ModelPart var3 = new ModelPart(this);
        var3.mirror = true;
        var3.setPos(8.0F, -14.0F, -8.0F);
        var3.texOffs(74, 55).addBox(0.0F, -14.0F, -2.0F, 2, 14, 4, 0.0F);
        var3.xRot = 1.0995574F;
        this.head.addChild(var3);
        this.mouth = new ModelPart(this);
        this.mouth.setPos(0.0F, -2.0F, 2.0F);
        this.mouth.texOffs(0, 36).addBox(-8.0F, 0.0F, -16.0F, 16, 3, 16, 0.0F);
        this.head.addChild(this.mouth);
        this.neck.addChild(this.head);
        this.body = new ModelPart(this);
        this.body.texOffs(0, 55).addBox(-7.0F, -10.0F, -7.0F, 14, 16, 20, 0.0F);
        this.body.texOffs(0, 91).addBox(-6.0F, 6.0F, -7.0F, 12, 13, 18, 0.0F);
        this.body.setPos(0.0F, 1.0F, 2.0F);
        this.leg0 = new ModelPart(this, 96, 0);
        this.leg0.addBox(-4.0F, 0.0F, -4.0F, 8, 37, 8, 0.0F);
        this.leg0.setPos(-8.0F, -13.0F, 18.0F);
        this.leg1 = new ModelPart(this, 96, 0);
        this.leg1.mirror = true;
        this.leg1.addBox(-4.0F, 0.0F, -4.0F, 8, 37, 8, 0.0F);
        this.leg1.setPos(8.0F, -13.0F, 18.0F);
        this.leg2 = new ModelPart(this, 64, 0);
        this.leg2.addBox(-4.0F, 0.0F, -4.0F, 8, 37, 8, 0.0F);
        this.leg2.setPos(-8.0F, -13.0F, -5.0F);
        this.leg3 = new ModelPart(this, 64, 0);
        this.leg3.mirror = true;
        this.leg3.addBox(-4.0F, 0.0F, -4.0F, 8, 37, 8, 0.0F);
        this.leg3.setPos(8.0F, -13.0F, -5.0F);
    }

    public void render(Ravager param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.neck.render(param6);
        this.body.render(param6);
        this.leg0.render(param6);
        this.leg1.render(param6);
        this.leg2.render(param6);
        this.leg3.render(param6);
    }

    public void setupAnim(Ravager param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.body.xRot = (float) (Math.PI / 2);
        float var0 = 0.4F * param2;
        this.leg0.xRot = Mth.cos(param1 * 0.6662F) * var0;
        this.leg1.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * var0;
        this.leg2.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * var0;
        this.leg3.xRot = Mth.cos(param1 * 0.6662F) * var0;
    }

    public void prepareMobModel(Ravager param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        int var0 = param0.getStunnedTick();
        int var1 = param0.getRoarTick();
        int var2 = 20;
        int var3 = param0.getAttackTick();
        int var4 = 10;
        if (var3 > 0) {
            float var5 = this.triangleWave((float)var3 - param3, 10.0F);
            float var6 = (1.0F + var5) * 0.5F;
            float var7 = var6 * var6 * var6 * 12.0F;
            float var8 = var7 * Mth.sin(this.neck.xRot);
            this.neck.z = -6.5F + var7;
            this.neck.y = -7.0F - var8;
            float var9 = Mth.sin(((float)var3 - param3) / 10.0F * (float) Math.PI * 0.25F);
            this.mouth.xRot = (float) (Math.PI / 2) * var9;
            if (var3 > 5) {
                this.mouth.xRot = Mth.sin(((float)(-4 + var3) - param3) / 4.0F) * (float) Math.PI * 0.4F;
            } else {
                this.mouth.xRot = (float) (Math.PI / 20) * Mth.sin((float) Math.PI * ((float)var3 - param3) / 10.0F);
            }
        } else {
            float var10 = -1.0F;
            float var11 = -1.0F * Mth.sin(this.neck.xRot);
            this.neck.x = 0.0F;
            this.neck.y = -7.0F - var11;
            this.neck.z = 5.5F;
            boolean var12 = var0 > 0;
            this.neck.xRot = var12 ? 0.21991149F : 0.0F;
            this.mouth.xRot = (float) Math.PI * (var12 ? 0.05F : 0.01F);
            if (var12) {
                double var13 = (double)var0 / 40.0;
                this.neck.x = (float)Math.sin(var13 * 10.0) * 3.0F;
            } else if (var1 > 0) {
                float var14 = Mth.sin(((float)(20 - var1) - param3) / 20.0F * (float) Math.PI * 0.25F);
                this.mouth.xRot = (float) (Math.PI / 2) * var14;
            }
        }

    }

    private float triangleWave(float param0, float param1) {
        return (Math.abs(param0 % param1 - param1 * 0.5F) - param1 * 0.25F) / (param1 * 0.25F);
    }
}

package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoatModel extends EntityModel<Boat> {
    private final ModelPart[] cubes = new ModelPart[5];
    private final ModelPart[] paddles = new ModelPart[2];
    private final ModelPart waterPatch;

    public BoatModel() {
        this.cubes[0] = new ModelPart(this, 0, 0).setTexSize(128, 64);
        this.cubes[1] = new ModelPart(this, 0, 19).setTexSize(128, 64);
        this.cubes[2] = new ModelPart(this, 0, 27).setTexSize(128, 64);
        this.cubes[3] = new ModelPart(this, 0, 35).setTexSize(128, 64);
        this.cubes[4] = new ModelPart(this, 0, 43).setTexSize(128, 64);
        int var0 = 32;
        int var1 = 6;
        int var2 = 20;
        int var3 = 4;
        int var4 = 28;
        this.cubes[0].addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
        this.cubes[0].setPos(0.0F, 3.0F, 1.0F);
        this.cubes[1].addBox(-13.0F, -7.0F, -1.0F, 18, 6, 2, 0.0F);
        this.cubes[1].setPos(-15.0F, 4.0F, 4.0F);
        this.cubes[2].addBox(-8.0F, -7.0F, -1.0F, 16, 6, 2, 0.0F);
        this.cubes[2].setPos(15.0F, 4.0F, 0.0F);
        this.cubes[3].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
        this.cubes[3].setPos(0.0F, 4.0F, -9.0F);
        this.cubes[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
        this.cubes[4].setPos(0.0F, 4.0F, 9.0F);
        this.cubes[0].xRot = (float) (Math.PI / 2);
        this.cubes[1].yRot = (float) (Math.PI * 3.0 / 2.0);
        this.cubes[2].yRot = (float) (Math.PI / 2);
        this.cubes[3].yRot = (float) Math.PI;
        this.paddles[0] = this.makePaddle(true);
        this.paddles[0].setPos(3.0F, -5.0F, 9.0F);
        this.paddles[1] = this.makePaddle(false);
        this.paddles[1].setPos(3.0F, -5.0F, -9.0F);
        this.paddles[1].yRot = (float) Math.PI;
        this.paddles[0].zRot = (float) (Math.PI / 16);
        this.paddles[1].zRot = (float) (Math.PI / 16);
        this.waterPatch = new ModelPart(this, 0, 0).setTexSize(128, 64);
        this.waterPatch.addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
        this.waterPatch.setPos(0.0F, -3.0F, 1.0F);
        this.waterPatch.xRot = (float) (Math.PI / 2);
    }

    public void render(Boat param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        RenderSystem.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);

        for(int var0 = 0; var0 < 5; ++var0) {
            this.cubes[var0].render(param6);
        }

        this.animatePaddle(param0, 0, param6, param1);
        this.animatePaddle(param0, 1, param6, param1);
    }

    public void renderSecondPass(Entity param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        RenderSystem.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.colorMask(false, false, false, false);
        this.waterPatch.render(param6);
        RenderSystem.colorMask(true, true, true, true);
    }

    protected ModelPart makePaddle(boolean param0) {
        ModelPart var0 = new ModelPart(this, 62, param0 ? 0 : 20).setTexSize(128, 64);
        int var1 = 20;
        int var2 = 7;
        int var3 = 6;
        float var4 = -5.0F;
        var0.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18);
        var0.addBox(param0 ? -1.001F : 0.001F, -3.0F, 8.0F, 1, 6, 7);
        return var0;
    }

    protected void animatePaddle(Boat param0, int param1, float param2, float param3) {
        float var0 = param0.getRowingTime(param1, param3);
        ModelPart var1 = this.paddles[param1];
        var1.xRot = (float)Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (double)((Mth.sin(-var0) + 1.0F) / 2.0F));
        var1.yRot = (float)Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (double)((Mth.sin(-var0 + 1.0F) + 1.0F) / 2.0F));
        if (param1 == 1) {
            var1.yRot = (float) Math.PI - var1.yRot;
        }

        var1.render(param2);
    }
}

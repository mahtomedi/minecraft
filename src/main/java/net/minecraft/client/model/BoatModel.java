package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoatModel extends ListModel<Boat> {
    private final ModelPart[] paddles = new ModelPart[2];
    private final ModelPart waterPatch;
    private final ImmutableList<ModelPart> parts;

    public BoatModel() {
        ModelPart[] var0 = new ModelPart[]{
            new ModelPart(this, 0, 0).setTexSize(128, 64),
            new ModelPart(this, 0, 19).setTexSize(128, 64),
            new ModelPart(this, 0, 27).setTexSize(128, 64),
            new ModelPart(this, 0, 35).setTexSize(128, 64),
            new ModelPart(this, 0, 43).setTexSize(128, 64)
        };
        int var1 = 32;
        int var2 = 6;
        int var3 = 20;
        int var4 = 4;
        int var5 = 28;
        var0[0].addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
        var0[0].setPos(0.0F, 3.0F, 1.0F);
        var0[1].addBox(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F, 0.0F);
        var0[1].setPos(-15.0F, 4.0F, 4.0F);
        var0[2].addBox(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F, 0.0F);
        var0[2].setPos(15.0F, 4.0F, 0.0F);
        var0[3].addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
        var0[3].setPos(0.0F, 4.0F, -9.0F);
        var0[4].addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
        var0[4].setPos(0.0F, 4.0F, 9.0F);
        var0[0].xRot = (float) (Math.PI / 2);
        var0[1].yRot = (float) (Math.PI * 3.0 / 2.0);
        var0[2].yRot = (float) (Math.PI / 2);
        var0[3].yRot = (float) Math.PI;
        this.paddles[0] = this.makePaddle(true);
        this.paddles[0].setPos(3.0F, -5.0F, 9.0F);
        this.paddles[1] = this.makePaddle(false);
        this.paddles[1].setPos(3.0F, -5.0F, -9.0F);
        this.paddles[1].yRot = (float) Math.PI;
        this.paddles[0].zRot = (float) (Math.PI / 16);
        this.paddles[1].zRot = (float) (Math.PI / 16);
        this.waterPatch = new ModelPart(this, 0, 0).setTexSize(128, 64);
        this.waterPatch.addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
        this.waterPatch.setPos(0.0F, -3.0F, 1.0F);
        this.waterPatch.xRot = (float) (Math.PI / 2);
        Builder<ModelPart> var6 = ImmutableList.builder();
        var6.addAll(Arrays.asList(var0));
        var6.addAll(Arrays.asList(this.paddles));
        this.parts = var6.build();
    }

    public void setupAnim(Boat param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.animatePaddle(param0, 0, param6, param1);
        this.animatePaddle(param0, 1, param6, param1);
    }

    public ImmutableList<ModelPart> parts() {
        return this.parts;
    }

    public ModelPart waterPatch() {
        return this.waterPatch;
    }

    protected ModelPart makePaddle(boolean param0) {
        ModelPart var0 = new ModelPart(this, 62, param0 ? 0 : 20).setTexSize(128, 64);
        int var1 = 20;
        int var2 = 7;
        int var3 = 6;
        float var4 = -5.0F;
        var0.addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F);
        var0.addBox(param0 ? -1.001F : 0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F);
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

    }
}

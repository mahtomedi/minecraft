package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelUtils {
    public static float rotlerpRad(float param0, float param1, float param2) {
        float var0 = param1 - param0;

        while(var0 < (float) -Math.PI) {
            var0 += (float) (Math.PI * 2);
        }

        while(var0 >= (float) Math.PI) {
            var0 -= (float) (Math.PI * 2);
        }

        return param0 + param2 * var0;
    }

    public static void setRotation(ModelPart param0, float param1, float param2, float param3) {
        param0.xRot = param1;
        param0.yRot = param2;
        param0.zRot = param3;
    }
}

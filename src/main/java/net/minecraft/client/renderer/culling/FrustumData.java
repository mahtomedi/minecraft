package net.minecraft.client.renderer.culling;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FrustumData {
    public final float[][] frustumData = new float[6][4];
    public final float[] projectionMatrix = new float[16];
    public final float[] modelViewMatrix = new float[16];
    public final float[] clip = new float[16];

    private double discriminant(float[] param0, double param1, double param2, double param3) {
        return (double)param0[0] * param1 + (double)param0[1] * param2 + (double)param0[2] * param3 + (double)param0[3];
    }

    public boolean cubeInFrustum(double param0, double param1, double param2, double param3, double param4, double param5) {
        for(int var0 = 0; var0 < 6; ++var0) {
            float[] var1 = this.frustumData[var0];
            if (!(this.discriminant(var1, param0, param1, param2) > 0.0)
                && !(this.discriminant(var1, param3, param1, param2) > 0.0)
                && !(this.discriminant(var1, param0, param4, param2) > 0.0)
                && !(this.discriminant(var1, param3, param4, param2) > 0.0)
                && !(this.discriminant(var1, param0, param1, param5) > 0.0)
                && !(this.discriminant(var1, param3, param1, param5) > 0.0)
                && !(this.discriminant(var1, param0, param4, param5) > 0.0)
                && !(this.discriminant(var1, param3, param4, param5) > 0.0)) {
                return false;
            }
        }

        return true;
    }
}

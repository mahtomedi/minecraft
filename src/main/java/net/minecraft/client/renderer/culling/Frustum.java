package net.minecraft.client.renderer.culling;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Frustum {
    public static final int OFFSET_STEP = 4;
    private final Vector4f[] frustumData = new Vector4f[6];
    private Vector4f viewVector;
    private double camX;
    private double camY;
    private double camZ;

    public Frustum(Matrix4f param0, Matrix4f param1) {
        this.calculateFrustum(param0, param1);
    }

    public Frustum(Frustum param0) {
        System.arraycopy(param0.frustumData, 0, this.frustumData, 0, param0.frustumData.length);
        this.camX = param0.camX;
        this.camY = param0.camY;
        this.camZ = param0.camZ;
        this.viewVector = param0.viewVector;
    }

    public Frustum offsetToFullyIncludeCameraCube(int param0) {
        double var0 = Math.floor(this.camX / (double)param0) * (double)param0;
        double var1 = Math.floor(this.camY / (double)param0) * (double)param0;
        double var2 = Math.floor(this.camZ / (double)param0) * (double)param0;
        double var3 = Math.ceil(this.camX / (double)param0) * (double)param0;
        double var4 = Math.ceil(this.camY / (double)param0) * (double)param0;

        for(double var5 = Math.ceil(this.camZ / (double)param0) * (double)param0;
            !this.cubeCompletelyInFrustum(
                (float)(var0 - this.camX),
                (float)(var1 - this.camY),
                (float)(var2 - this.camZ),
                (float)(var3 - this.camX),
                (float)(var4 - this.camY),
                (float)(var5 - this.camZ)
            );
            this.camZ -= (double)(this.viewVector.z() * 4.0F)
        ) {
            this.camX -= (double)(this.viewVector.x() * 4.0F);
            this.camY -= (double)(this.viewVector.y() * 4.0F);
        }

        return this;
    }

    public void prepare(double param0, double param1, double param2) {
        this.camX = param0;
        this.camY = param1;
        this.camZ = param2;
    }

    private void calculateFrustum(Matrix4f param0, Matrix4f param1) {
        Matrix4f var0 = param1.copy();
        var0.multiply(param0);
        var0.transpose();
        this.viewVector = new Vector4f(0.0F, 0.0F, 1.0F, 0.0F);
        this.viewVector.transform(var0);
        this.getPlane(var0, -1, 0, 0, 0);
        this.getPlane(var0, 1, 0, 0, 1);
        this.getPlane(var0, 0, -1, 0, 2);
        this.getPlane(var0, 0, 1, 0, 3);
        this.getPlane(var0, 0, 0, -1, 4);
        this.getPlane(var0, 0, 0, 1, 5);
    }

    private void getPlane(Matrix4f param0, int param1, int param2, int param3, int param4) {
        Vector4f var0 = new Vector4f((float)param1, (float)param2, (float)param3, 1.0F);
        var0.transform(param0);
        var0.normalize();
        this.frustumData[param4] = var0;
    }

    public boolean isVisible(AABB param0) {
        return this.cubeInFrustum(param0.minX, param0.minY, param0.minZ, param0.maxX, param0.maxY, param0.maxZ);
    }

    private boolean cubeInFrustum(double param0, double param1, double param2, double param3, double param4, double param5) {
        float var0 = (float)(param0 - this.camX);
        float var1 = (float)(param1 - this.camY);
        float var2 = (float)(param2 - this.camZ);
        float var3 = (float)(param3 - this.camX);
        float var4 = (float)(param4 - this.camY);
        float var5 = (float)(param5 - this.camZ);
        return this.cubeInFrustum(var0, var1, var2, var3, var4, var5);
    }

    private boolean cubeInFrustum(float param0, float param1, float param2, float param3, float param4, float param5) {
        for(int var0 = 0; var0 < 6; ++var0) {
            Vector4f var1 = this.frustumData[var0];
            if (!(var1.dot(new Vector4f(param0, param1, param2, 1.0F)) > 0.0F)
                && !(var1.dot(new Vector4f(param3, param1, param2, 1.0F)) > 0.0F)
                && !(var1.dot(new Vector4f(param0, param4, param2, 1.0F)) > 0.0F)
                && !(var1.dot(new Vector4f(param3, param4, param2, 1.0F)) > 0.0F)
                && !(var1.dot(new Vector4f(param0, param1, param5, 1.0F)) > 0.0F)
                && !(var1.dot(new Vector4f(param3, param1, param5, 1.0F)) > 0.0F)
                && !(var1.dot(new Vector4f(param0, param4, param5, 1.0F)) > 0.0F)
                && !(var1.dot(new Vector4f(param3, param4, param5, 1.0F)) > 0.0F)) {
                return false;
            }
        }

        return true;
    }

    private boolean cubeCompletelyInFrustum(float param0, float param1, float param2, float param3, float param4, float param5) {
        for(int var0 = 0; var0 < 6; ++var0) {
            Vector4f var1 = this.frustumData[var0];
            if (var1.dot(new Vector4f(param0, param1, param2, 1.0F)) <= 0.0F) {
                return false;
            }

            if (var1.dot(new Vector4f(param3, param1, param2, 1.0F)) <= 0.0F) {
                return false;
            }

            if (var1.dot(new Vector4f(param0, param4, param2, 1.0F)) <= 0.0F) {
                return false;
            }

            if (var1.dot(new Vector4f(param3, param4, param2, 1.0F)) <= 0.0F) {
                return false;
            }

            if (var1.dot(new Vector4f(param0, param1, param5, 1.0F)) <= 0.0F) {
                return false;
            }

            if (var1.dot(new Vector4f(param3, param1, param5, 1.0F)) <= 0.0F) {
                return false;
            }

            if (var1.dot(new Vector4f(param0, param4, param5, 1.0F)) <= 0.0F) {
                return false;
            }

            if (var1.dot(new Vector4f(param3, param4, param5, 1.0F)) <= 0.0F) {
                return false;
            }
        }

        return true;
    }
}

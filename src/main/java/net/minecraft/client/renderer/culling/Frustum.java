package net.minecraft.client.renderer.culling;

import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class Frustum {
    public static final int OFFSET_STEP = 4;
    private final FrustumIntersection intersection = new FrustumIntersection();
    private final Matrix4f matrix = new Matrix4f();
    private Vector4f viewVector;
    private double camX;
    private double camY;
    private double camZ;

    public Frustum(Matrix4f param0, Matrix4f param1) {
        this.calculateFrustum(param0, param1);
    }

    public Frustum(Frustum param0) {
        this.intersection.set(param0.matrix);
        this.matrix.set((Matrix4fc)param0.matrix);
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
            this.intersection
                    .intersectAab(
                        (float)(var0 - this.camX),
                        (float)(var1 - this.camY),
                        (float)(var2 - this.camZ),
                        (float)(var3 - this.camX),
                        (float)(var4 - this.camY),
                        (float)(var5 - this.camZ)
                    )
                != -2;
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
        param1.mul(param0, this.matrix);
        this.intersection.set(this.matrix);
        this.viewVector = this.matrix.transformTranspose(new Vector4f(0.0F, 0.0F, 1.0F, 0.0F));
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
        return this.intersection.testAab(var0, var1, var2, var3, var4, var5);
    }
}

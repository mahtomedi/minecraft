package net.minecraft.client.renderer.culling;

import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FrustumCuller implements Culler {
    private final FrustumData frustum;
    private double xOff;
    private double yOff;
    private double zOff;

    public FrustumCuller() {
        this(Frustum.getFrustum());
    }

    public FrustumCuller(FrustumData param0) {
        this.frustum = param0;
    }

    @Override
    public void prepare(double param0, double param1, double param2) {
        this.xOff = param0;
        this.yOff = param1;
        this.zOff = param2;
    }

    public boolean cubeInFrustum(double param0, double param1, double param2, double param3, double param4, double param5) {
        return this.frustum
            .cubeInFrustum(param0 - this.xOff, param1 - this.yOff, param2 - this.zOff, param3 - this.xOff, param4 - this.yOff, param5 - this.zOff);
    }

    @Override
    public boolean isVisible(AABB param0) {
        return this.cubeInFrustum(param0.minX, param0.minY, param0.minZ, param0.maxX, param0.maxY, param0.maxZ);
    }
}

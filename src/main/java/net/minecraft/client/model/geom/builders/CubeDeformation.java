package net.minecraft.client.model.geom.builders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CubeDeformation {
    public static final CubeDeformation NONE = new CubeDeformation(0.0F);
    final float growX;
    final float growY;
    final float growZ;

    public CubeDeformation(float param0, float param1, float param2) {
        this.growX = param0;
        this.growY = param1;
        this.growZ = param2;
    }

    public CubeDeformation(float param0) {
        this(param0, param0, param0);
    }

    public CubeDeformation extend(float param0) {
        return new CubeDeformation(this.growX + param0, this.growY + param0, this.growZ + param0);
    }

    public CubeDeformation extend(float param0, float param1, float param2) {
        return new CubeDeformation(this.growX + param0, this.growY + param1, this.growZ + param2);
    }
}

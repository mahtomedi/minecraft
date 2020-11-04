package net.minecraft.client.model.geom.builders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UVPair {
    private final float u;
    private final float v;

    public UVPair(float param0, float param1) {
        this.u = param0;
        this.v = param1;
    }

    public float u() {
        return this.u;
    }

    public float v() {
        return this.v;
    }

    @Override
    public String toString() {
        return "(" + this.u + "," + this.v + ")";
    }
}

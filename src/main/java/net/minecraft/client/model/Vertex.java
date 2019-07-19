package net.minecraft.client.model;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Vertex {
    public final Vec3 pos;
    public final float u;
    public final float v;

    public Vertex(float param0, float param1, float param2, float param3, float param4) {
        this(new Vec3((double)param0, (double)param1, (double)param2), param3, param4);
    }

    public Vertex remap(float param0, float param1) {
        return new Vertex(this, param0, param1);
    }

    public Vertex(Vertex param0, float param1, float param2) {
        this.pos = param0.pos;
        this.u = param1;
        this.v = param2;
    }

    public Vertex(Vec3 param0, float param1, float param2) {
        this.pos = param0;
        this.u = param1;
        this.v = param2;
    }
}

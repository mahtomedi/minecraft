package net.minecraft.realms;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tezzelator {
    public static final Tesselator t = Tesselator.getInstance();
    public static final Tezzelator instance = new Tezzelator();

    public void end() {
        t.end();
    }

    public Tezzelator vertex(double param0, double param1, double param2) {
        t.getBuilder().vertex(param0, param1, param2);
        return this;
    }

    public void begin(int param0, RealmsVertexFormat param1) {
        t.getBuilder().begin(param0, param1.getVertexFormat());
    }

    public void endVertex() {
        t.getBuilder().endVertex();
    }

    public Tezzelator color(int param0, int param1, int param2, int param3) {
        t.getBuilder().color(param0, param1, param2, param3);
        return this;
    }

    public Tezzelator tex(float param0, float param1) {
        t.getBuilder().uv(param0, param1);
        return this;
    }
}

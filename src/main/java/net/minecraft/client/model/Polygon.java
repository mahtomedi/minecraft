package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Polygon {
    public Vertex[] vertices;
    public final int vertexCount;
    private boolean flipNormal;

    public Polygon(Vertex[] param0) {
        this.vertices = param0;
        this.vertexCount = param0.length;
    }

    public Polygon(Vertex[] param0, int param1, int param2, int param3, int param4, float param5, float param6) {
        this(param0);
        float var0 = 0.0F / param5;
        float var1 = 0.0F / param6;
        param0[0] = param0[0].remap((float)param3 / param5 - var0, (float)param2 / param6 + var1);
        param0[1] = param0[1].remap((float)param1 / param5 + var0, (float)param2 / param6 + var1);
        param0[2] = param0[2].remap((float)param1 / param5 + var0, (float)param4 / param6 - var1);
        param0[3] = param0[3].remap((float)param3 / param5 - var0, (float)param4 / param6 - var1);
    }

    public void mirror() {
        Vertex[] var0 = new Vertex[this.vertices.length];

        for(int var1 = 0; var1 < this.vertices.length; ++var1) {
            var0[var1] = this.vertices[this.vertices.length - var1 - 1];
        }

        this.vertices = var0;
    }

    public void render(BufferBuilder param0, float param1) {
        Vec3 var0 = this.vertices[1].pos.vectorTo(this.vertices[0].pos);
        Vec3 var1 = this.vertices[1].pos.vectorTo(this.vertices[2].pos);
        Vec3 var2 = var1.cross(var0).normalize();
        float var3 = (float)var2.x;
        float var4 = (float)var2.y;
        float var5 = (float)var2.z;
        if (this.flipNormal) {
            var3 = -var3;
            var4 = -var4;
            var5 = -var5;
        }

        param0.begin(7, DefaultVertexFormat.ENTITY);

        for(int var6 = 0; var6 < 4; ++var6) {
            Vertex var7 = this.vertices[var6];
            param0.vertex(var7.pos.x * (double)param1, var7.pos.y * (double)param1, var7.pos.z * (double)param1)
                .uv((double)var7.u, (double)var7.v)
                .normal(var3, var4, var5)
                .endVertex();
        }

        Tesselator.getInstance().end();
    }
}

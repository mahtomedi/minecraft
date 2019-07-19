package net.minecraft.client.model.geom;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.model.Polygon;
import net.minecraft.client.model.Vertex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Cube {
    private final Vertex[] vertices;
    private final Polygon[] polygons;
    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;
    public String id;

    public Cube(ModelPart param0, int param1, int param2, float param3, float param4, float param5, int param6, int param7, int param8, float param9) {
        this(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param0.mirror);
    }

    public Cube(
        ModelPart param0, int param1, int param2, float param3, float param4, float param5, int param6, int param7, int param8, float param9, boolean param10
    ) {
        this.minX = param3;
        this.minY = param4;
        this.minZ = param5;
        this.maxX = param3 + (float)param6;
        this.maxY = param4 + (float)param7;
        this.maxZ = param5 + (float)param8;
        this.vertices = new Vertex[8];
        this.polygons = new Polygon[6];
        float var0 = param3 + (float)param6;
        float var1 = param4 + (float)param7;
        float var2 = param5 + (float)param8;
        param3 -= param9;
        param4 -= param9;
        param5 -= param9;
        var0 += param9;
        var1 += param9;
        var2 += param9;
        if (param10) {
            float var3 = var0;
            var0 = param3;
            param3 = var3;
        }

        Vertex var4 = new Vertex(param3, param4, param5, 0.0F, 0.0F);
        Vertex var5 = new Vertex(var0, param4, param5, 0.0F, 8.0F);
        Vertex var6 = new Vertex(var0, var1, param5, 8.0F, 8.0F);
        Vertex var7 = new Vertex(param3, var1, param5, 8.0F, 0.0F);
        Vertex var8 = new Vertex(param3, param4, var2, 0.0F, 0.0F);
        Vertex var9 = new Vertex(var0, param4, var2, 0.0F, 8.0F);
        Vertex var10 = new Vertex(var0, var1, var2, 8.0F, 8.0F);
        Vertex var11 = new Vertex(param3, var1, var2, 8.0F, 0.0F);
        this.vertices[0] = var4;
        this.vertices[1] = var5;
        this.vertices[2] = var6;
        this.vertices[3] = var7;
        this.vertices[4] = var8;
        this.vertices[5] = var9;
        this.vertices[6] = var10;
        this.vertices[7] = var11;
        this.polygons[0] = new Polygon(
            new Vertex[]{var9, var5, var6, var10},
            param1 + param8 + param6,
            param2 + param8,
            param1 + param8 + param6 + param8,
            param2 + param8 + param7,
            param0.xTexSize,
            param0.yTexSize
        );
        this.polygons[1] = new Polygon(
            new Vertex[]{var4, var8, var11, var7}, param1, param2 + param8, param1 + param8, param2 + param8 + param7, param0.xTexSize, param0.yTexSize
        );
        this.polygons[2] = new Polygon(
            new Vertex[]{var9, var8, var4, var5}, param1 + param8, param2, param1 + param8 + param6, param2 + param8, param0.xTexSize, param0.yTexSize
        );
        this.polygons[3] = new Polygon(
            new Vertex[]{var6, var7, var11, var10},
            param1 + param8 + param6,
            param2 + param8,
            param1 + param8 + param6 + param6,
            param2,
            param0.xTexSize,
            param0.yTexSize
        );
        this.polygons[4] = new Polygon(
            new Vertex[]{var5, var4, var7, var6},
            param1 + param8,
            param2 + param8,
            param1 + param8 + param6,
            param2 + param8 + param7,
            param0.xTexSize,
            param0.yTexSize
        );
        this.polygons[5] = new Polygon(
            new Vertex[]{var8, var9, var10, var11},
            param1 + param8 + param6 + param8,
            param2 + param8,
            param1 + param8 + param6 + param8 + param6,
            param2 + param8 + param7,
            param0.xTexSize,
            param0.yTexSize
        );
        if (param10) {
            for(Polygon var12 : this.polygons) {
                var12.mirror();
            }
        }

    }

    public void compile(BufferBuilder param0, float param1) {
        for(Polygon var0 : this.polygons) {
            var0.render(param0, param1);
        }

    }

    public Cube setId(String param0) {
        this.id = param0;
        return this;
    }
}

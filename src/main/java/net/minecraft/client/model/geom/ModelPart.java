package net.minecraft.client.model.geom;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelPart {
    private float xTexSize = 64.0F;
    private float yTexSize = 32.0F;
    private int xTexOffs;
    private int yTexOffs;
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public boolean mirror;
    public boolean visible = true;
    private final List<ModelPart.Cube> cubes = Lists.newArrayList();
    private final List<ModelPart> children = Lists.newArrayList();

    public ModelPart(Model param0) {
        param0.accept(this);
        this.setTexSize(param0.texWidth, param0.texHeight);
    }

    public ModelPart(Model param0, int param1, int param2) {
        this(param0.texWidth, param0.texHeight, param1, param2);
        param0.accept(this);
    }

    public ModelPart(int param0, int param1, int param2, int param3) {
        this.setTexSize(param0, param1);
        this.texOffs(param2, param3);
    }

    public void copyFrom(ModelPart param0) {
        this.xRot = param0.xRot;
        this.yRot = param0.yRot;
        this.zRot = param0.zRot;
        this.x = param0.x;
        this.y = param0.y;
        this.z = param0.z;
    }

    public void addChild(ModelPart param0) {
        this.children.add(param0);
    }

    public ModelPart texOffs(int param0, int param1) {
        this.xTexOffs = param0;
        this.yTexOffs = param1;
        return this;
    }

    public ModelPart addBox(String param0, float param1, float param2, float param3, int param4, int param5, int param6, float param7, int param8, int param9) {
        this.texOffs(param8, param9);
        this.addBox(
            this.xTexOffs, this.yTexOffs, param1, param2, param3, (float)param4, (float)param5, (float)param6, param7, param7, param7, this.mirror, false
        );
        return this;
    }

    public ModelPart addBox(float param0, float param1, float param2, float param3, float param4, float param5) {
        this.addBox(this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, 0.0F, 0.0F, 0.0F, this.mirror, false);
        return this;
    }

    public ModelPart addBox(float param0, float param1, float param2, float param3, float param4, float param5, boolean param6) {
        this.addBox(this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, 0.0F, 0.0F, 0.0F, param6, false);
        return this;
    }

    public void addBox(float param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.addBox(this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, param6, param6, param6, this.mirror, false);
    }

    public void addBox(float param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7, float param8) {
        this.addBox(this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, param6, param7, param8, this.mirror, false);
    }

    public void addBox(float param0, float param1, float param2, float param3, float param4, float param5, float param6, boolean param7) {
        this.addBox(this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, param6, param6, param6, param7, false);
    }

    private void addBox(
        int param0,
        int param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10,
        boolean param11,
        boolean param12
    ) {
        this.cubes
            .add(
                new ModelPart.Cube(
                    param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, this.xTexSize, this.yTexSize
                )
            );
    }

    public void setPos(float param0, float param1, float param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    public void render(PoseStack param0, VertexConsumer param1, int param2, int param3, @Nullable TextureAtlasSprite param4) {
        this.render(param0, param1, param2, param3, param4, 1.0F, 1.0F, 1.0F);
    }

    public void render(
        PoseStack param0, VertexConsumer param1, int param2, int param3, @Nullable TextureAtlasSprite param4, float param5, float param6, float param7
    ) {
        if (this.visible) {
            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                param0.pushPose();
                this.translateAndRotate(param0);
                this.compile(param0.last(), param1, param2, param3, param4, param5, param6, param7);

                for(ModelPart var0 : this.children) {
                    var0.render(param0, param1, param2, param3, param4, param5, param6, param7);
                }

                param0.popPose();
            }
        }
    }

    public void translateAndRotate(PoseStack param0) {
        param0.translate((double)(this.x / 16.0F), (double)(this.y / 16.0F), (double)(this.z / 16.0F));
        if (this.zRot != 0.0F) {
            param0.mulPose(Vector3f.ZP.rotation(this.zRot));
        }

        if (this.yRot != 0.0F) {
            param0.mulPose(Vector3f.YP.rotation(this.yRot));
        }

        if (this.xRot != 0.0F) {
            param0.mulPose(Vector3f.XP.rotation(this.xRot));
        }

    }

    private void compile(
        PoseStack.Pose param0, VertexConsumer param1, int param2, int param3, @Nullable TextureAtlasSprite param4, float param5, float param6, float param7
    ) {
        Matrix4f var0 = param0.pose();
        Matrix3f var1 = new Matrix3f(var0);

        for(ModelPart.Cube var2 : this.cubes) {
            for(ModelPart.Polygon var3 : var2.polygons) {
                Vector3f var4 = new Vector3f(var3.vertices[1].pos.vectorTo(var3.vertices[0].pos));
                Vector3f var5 = new Vector3f(var3.vertices[1].pos.vectorTo(var3.vertices[2].pos));
                var4.transform(var1);
                var5.transform(var1);
                var5.cross(var4);
                var5.normalize();
                float var6 = var5.x();
                float var7 = var5.y();
                float var8 = var5.z();

                for(int var9 = 0; var9 < 4; ++var9) {
                    ModelPart.Vertex var10 = var3.vertices[var9];
                    float var11 = (float)var10.pos.x / 16.0F;
                    float var12 = (float)var10.pos.y / 16.0F;
                    float var13 = (float)var10.pos.z / 16.0F;
                    Vector4f var14 = new Vector4f(var11, var12, var13, 1.0F);
                    var14.transform(var0);
                    float var15;
                    float var16;
                    if (param4 == null) {
                        var15 = var10.u;
                        var16 = var10.v;
                    } else {
                        var15 = param4.getU((double)(var10.u * 16.0F));
                        var16 = param4.getV((double)(var10.v * 16.0F));
                    }

                    param1.vertex((double)var14.x(), (double)var14.y(), (double)var14.z())
                        .color(param5, param6, param7, 1.0F)
                        .uv(var15, var16)
                        .overlayCoords(param3)
                        .uv2(param2)
                        .normal(var6, var7, var8)
                        .endVertex();
                }
            }
        }

    }

    public ModelPart setTexSize(int param0, int param1) {
        this.xTexSize = (float)param0;
        this.yTexSize = (float)param1;
        return this;
    }

    public ModelPart.Cube getRandomCube(Random param0) {
        return this.cubes.get(param0.nextInt(this.cubes.size()));
    }

    @OnlyIn(Dist.CLIENT)
    public static class Cube {
        private final ModelPart.Polygon[] polygons;
        public final float minX;
        public final float minY;
        public final float minZ;
        public final float maxX;
        public final float maxY;
        public final float maxZ;

        public Cube(
            int param0,
            int param1,
            float param2,
            float param3,
            float param4,
            float param5,
            float param6,
            float param7,
            float param8,
            float param9,
            float param10,
            boolean param11,
            float param12,
            float param13
        ) {
            this.minX = param2;
            this.minY = param3;
            this.minZ = param4;
            this.maxX = param2 + param5;
            this.maxY = param3 + param6;
            this.maxZ = param4 + param7;
            this.polygons = new ModelPart.Polygon[6];
            float var0 = param2 + param5;
            float var1 = param3 + param6;
            float var2 = param4 + param7;
            param2 -= param8;
            param3 -= param9;
            param4 -= param10;
            var0 += param8;
            var1 += param9;
            var2 += param10;
            if (param11) {
                float var3 = var0;
                var0 = param2;
                param2 = var3;
            }

            ModelPart.Vertex var4 = new ModelPart.Vertex(param2, param3, param4, 0.0F, 0.0F);
            ModelPart.Vertex var5 = new ModelPart.Vertex(var0, param3, param4, 0.0F, 8.0F);
            ModelPart.Vertex var6 = new ModelPart.Vertex(var0, var1, param4, 8.0F, 8.0F);
            ModelPart.Vertex var7 = new ModelPart.Vertex(param2, var1, param4, 8.0F, 0.0F);
            ModelPart.Vertex var8 = new ModelPart.Vertex(param2, param3, var2, 0.0F, 0.0F);
            ModelPart.Vertex var9 = new ModelPart.Vertex(var0, param3, var2, 0.0F, 8.0F);
            ModelPart.Vertex var10 = new ModelPart.Vertex(var0, var1, var2, 8.0F, 8.0F);
            ModelPart.Vertex var11 = new ModelPart.Vertex(param2, var1, var2, 8.0F, 0.0F);
            float var12 = (float)param0;
            float var13 = (float)param0 + param7;
            float var14 = (float)param0 + param7 + param5;
            float var15 = (float)param0 + param7 + param5 + param5;
            float var16 = (float)param0 + param7 + param5 + param7;
            float var17 = (float)param0 + param7 + param5 + param7 + param5;
            float var18 = (float)param1;
            float var19 = (float)param1 + param7;
            float var20 = (float)param1 + param7 + param6;
            this.polygons[2] = new ModelPart.Polygon(new ModelPart.Vertex[]{var9, var8, var4, var5}, var13, var18, var14, var19, param12, param13);
            this.polygons[3] = new ModelPart.Polygon(new ModelPart.Vertex[]{var6, var7, var11, var10}, var14, var19, var15, var18, param12, param13);
            this.polygons[1] = new ModelPart.Polygon(new ModelPart.Vertex[]{var4, var8, var11, var7}, var12, var19, var13, var20, param12, param13);
            this.polygons[4] = new ModelPart.Polygon(new ModelPart.Vertex[]{var5, var4, var7, var6}, var13, var19, var14, var20, param12, param13);
            this.polygons[0] = new ModelPart.Polygon(new ModelPart.Vertex[]{var9, var5, var6, var10}, var14, var19, var16, var20, param12, param13);
            this.polygons[5] = new ModelPart.Polygon(new ModelPart.Vertex[]{var8, var9, var10, var11}, var16, var19, var17, var20, param12, param13);
            if (param11) {
                for(ModelPart.Polygon var21 : this.polygons) {
                    var21.mirror();
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Polygon {
        public ModelPart.Vertex[] vertices;

        public Polygon(ModelPart.Vertex[] param0) {
            this.vertices = param0;
        }

        public Polygon(ModelPart.Vertex[] param0, float param1, float param2, float param3, float param4, float param5, float param6) {
            this(param0);
            float var0 = 0.0F / param5;
            float var1 = 0.0F / param6;
            param0[0] = param0[0].remap(param3 / param5 - var0, param2 / param6 + var1);
            param0[1] = param0[1].remap(param1 / param5 + var0, param2 / param6 + var1);
            param0[2] = param0[2].remap(param1 / param5 + var0, param4 / param6 - var1);
            param0[3] = param0[3].remap(param3 / param5 - var0, param4 / param6 - var1);
        }

        public void mirror() {
            ModelPart.Vertex[] var0 = new ModelPart.Vertex[this.vertices.length];

            for(int var1 = 0; var1 < this.vertices.length; ++var1) {
                var0[var1] = this.vertices[this.vertices.length - var1 - 1];
            }

            this.vertices = var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Vertex {
        public final Vec3 pos;
        public final float u;
        public final float v;

        public Vertex(float param0, float param1, float param2, float param3, float param4) {
            this(new Vec3((double)param0, (double)param1, (double)param2), param3, param4);
        }

        public ModelPart.Vertex remap(float param0, float param1) {
            return new ModelPart.Vertex(this.pos, param0, param1);
        }

        public Vertex(Vec3 param0, float param1, float param2) {
            this.pos = param0;
            this.u = param1;
            this.v = param2;
        }
    }
}

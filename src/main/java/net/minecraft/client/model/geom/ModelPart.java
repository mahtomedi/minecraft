package net.minecraft.client.model.geom;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelPart {
    private static final BufferBuilder COMPILE_BUFFER = new BufferBuilder(256);
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
    @Nullable
    private ByteBuffer compiled;
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
        this.cubes
            .add(
                new ModelPart.Cube(
                    this.xTexOffs,
                    this.yTexOffs,
                    param1,
                    param2,
                    param3,
                    (float)param4,
                    (float)param5,
                    (float)param6,
                    param7,
                    this.mirror,
                    this.xTexSize,
                    this.yTexSize
                )
            );
        return this;
    }

    public ModelPart addBox(float param0, float param1, float param2, float param3, float param4, float param5) {
        this.cubes
            .add(
                new ModelPart.Cube(
                    this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, 0.0F, this.mirror, this.xTexSize, this.yTexSize
                )
            );
        return this;
    }

    public ModelPart addBox(float param0, float param1, float param2, float param3, float param4, float param5, boolean param6) {
        this.cubes
            .add(new ModelPart.Cube(this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, 0.0F, param6, this.xTexSize, this.yTexSize));
        return this;
    }

    public void addBox(float param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.cubes
            .add(
                new ModelPart.Cube(
                    this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, param6, this.mirror, this.xTexSize, this.yTexSize
                )
            );
    }

    public void addBox(float param0, float param1, float param2, float param3, float param4, float param5, float param6, boolean param7) {
        this.cubes
            .add(new ModelPart.Cube(this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, param6, param7, this.xTexSize, this.yTexSize));
    }

    public void setPos(float param0, float param1, float param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    public void render(float param0) {
        if (this.visible) {
            this.compile(param0);
            if (this.compiled != null) {
                RenderSystem.pushMatrix();
                this.translateAndRotate(param0);
                ((Buffer)this.compiled).clear();
                int var0 = this.compiled.remaining() / DefaultVertexFormat.ENTITY.getVertexSize();
                BufferUploader.end(this.compiled, 7, DefaultVertexFormat.ENTITY, var0);

                for(ModelPart var1 : this.children) {
                    var1.render(param0);
                }

                RenderSystem.popMatrix();
            }
        }
    }

    public void render(BufferBuilder param0, float param1, int param2, int param3, TextureAtlasSprite param4) {
        this.render(param0, param1, param2, param3, param4, 1.0F, 1.0F, 1.0F);
    }

    public void render(BufferBuilder param0, float param1, int param2, int param3, TextureAtlasSprite param4, float param5, float param6, float param7) {
        if (this.visible) {
            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                param0.pushPose();
                param0.translate((double)(this.x * param1), (double)(this.y * param1), (double)(this.z * param1));
                if (this.zRot != 0.0F) {
                    param0.multiplyPose(new Quaternion(Vector3f.ZP, this.zRot, false));
                }

                if (this.yRot != 0.0F) {
                    param0.multiplyPose(new Quaternion(Vector3f.YP, this.yRot, false));
                }

                if (this.xRot != 0.0F) {
                    param0.multiplyPose(new Quaternion(Vector3f.XP, this.xRot, false));
                }

                this.compile(param0, param1, param2, param3, param4, param5, param6, param7);

                for(ModelPart var0 : this.children) {
                    var0.render(param0, param1, param2, param3, param4);
                }

                param0.popPose();
            }
        }
    }

    private void compile(float param0) {
        if (this.visible) {
            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                if (this.compiled == null) {
                    COMPILE_BUFFER.begin(7, DefaultVertexFormat.ENTITY);
                    this.compile(COMPILE_BUFFER, param0, 240, 240, null);
                    COMPILE_BUFFER.end();
                    Pair<BufferBuilder.DrawState, ByteBuffer> var0 = COMPILE_BUFFER.popNextBuffer();
                    ByteBuffer var1 = var0.getSecond();
                    this.compiled = MemoryTracker.createByteBuffer(var1.remaining());
                    this.compiled.put(var1);
                }

            }
        }
    }

    public void translateTo(float param0) {
        if (this.visible) {
            this.translateAndRotate(param0);
        }
    }

    private void translateAndRotate(float param0) {
        RenderSystem.translatef(this.x * param0, this.y * param0, this.z * param0);
        if (this.zRot != 0.0F) {
            RenderSystem.rotatef(this.zRot * (180.0F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
        }

        if (this.yRot != 0.0F) {
            RenderSystem.rotatef(this.yRot * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
        }

        if (this.xRot != 0.0F) {
            RenderSystem.rotatef(this.xRot * (180.0F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
        }

    }

    private void compile(BufferBuilder param0, float param1, int param2, int param3, @Nullable TextureAtlasSprite param4) {
        this.compile(param0, param1, param2, param3, param4, 1.0F, 1.0F, 1.0F);
    }

    private void compile(
        BufferBuilder param0, float param1, int param2, int param3, @Nullable TextureAtlasSprite param4, float param5, float param6, float param7
    ) {
        Matrix4f var0 = param0.getPose();
        VertexFormat var1 = param0.getVertexFormat();

        for(ModelPart.Cube var2 : this.cubes) {
            for(ModelPart.Polygon var3 : var2.polygons) {
                Vec3 var4 = var3.vertices[1].pos.vectorTo(var3.vertices[0].pos);
                Vec3 var5 = var3.vertices[1].pos.vectorTo(var3.vertices[2].pos);
                Vec3 var6 = var5.cross(var4).normalize();
                float var7 = (float)var6.x;
                float var8 = (float)var6.y;
                float var9 = (float)var6.z;

                for(int var10 = 0; var10 < 4; ++var10) {
                    ModelPart.Vertex var11 = var3.vertices[var10];
                    Vector4f var12 = new Vector4f((float)var11.pos.x * param1, (float)var11.pos.y * param1, (float)var11.pos.z * param1, 1.0F);
                    var12.transform(var0);
                    param0.vertex((double)var12.x(), (double)var12.y(), (double)var12.z());
                    if (var1.hasColor()) {
                        float var13 = Mth.diffuseLight(var7, var8, var9);
                        param0.color(var13 * param5, var13 * param6, var13 * param7, 1.0F);
                    }

                    if (param4 == null) {
                        param0.uv((double)var11.u, (double)var11.v);
                    } else {
                        param0.uv((double)param4.getU((double)(var11.u * 16.0F)), (double)param4.getV((double)(var11.v * 16.0F)));
                    }

                    if (var1.hasUv(1)) {
                        param0.uv2(param2, param3);
                    }

                    param0.normal(var7, var8, var9).endVertex();
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
            boolean param9,
            float param10,
            float param11
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
            param3 -= param8;
            param4 -= param8;
            var0 += param8;
            var1 += param8;
            var2 += param8;
            if (param9) {
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
            this.polygons[0] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var9, var5, var6, var10},
                (float)param0 + param7 + param5,
                (float)param1 + param7,
                (float)param0 + param7 + param5 + param7,
                (float)param1 + param7 + param6,
                param10,
                param11
            );
            this.polygons[1] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var4, var8, var11, var7},
                (float)param0,
                (float)param1 + param7,
                (float)param0 + param7,
                (float)param1 + param7 + param6,
                param10,
                param11
            );
            this.polygons[2] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var9, var8, var4, var5},
                (float)param0 + param7,
                (float)param1,
                (float)param0 + param7 + param5,
                (float)param1 + param7,
                param10,
                param11
            );
            this.polygons[3] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var6, var7, var11, var10},
                (float)param0 + param7 + param5,
                (float)param1 + param7,
                (float)param0 + param7 + param5 + param5,
                (float)param1,
                param10,
                param11
            );
            this.polygons[4] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var5, var4, var7, var6},
                (float)param0 + param7,
                (float)param1 + param7,
                (float)param0 + param7 + param5,
                (float)param1 + param7 + param6,
                param10,
                param11
            );
            this.polygons[5] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var8, var9, var10, var11},
                (float)param0 + param7 + param5 + param7,
                (float)param1 + param7,
                (float)param0 + param7 + param5 + param7 + param5,
                (float)param1 + param7 + param6,
                param10,
                param11
            );
            if (param9) {
                for(ModelPart.Polygon var12 : this.polygons) {
                    var12.mirror();
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

package net.minecraft.client.model.geom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ModelPart {
    public static final float DEFAULT_SCALE = 1.0F;
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public float xScale = 1.0F;
    public float yScale = 1.0F;
    public float zScale = 1.0F;
    public boolean visible = true;
    public boolean skipDraw;
    private final List<ModelPart.Cube> cubes;
    private final Map<String, ModelPart> children;
    private PartPose initialPose = PartPose.ZERO;

    public ModelPart(List<ModelPart.Cube> param0, Map<String, ModelPart> param1) {
        this.cubes = param0;
        this.children = param1;
    }

    public PartPose storePose() {
        return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
    }

    public PartPose getInitialPose() {
        return this.initialPose;
    }

    public void setInitialPose(PartPose param0) {
        this.initialPose = param0;
    }

    public void resetPose() {
        this.loadPose(this.initialPose);
    }

    public void loadPose(PartPose param0) {
        this.x = param0.x;
        this.y = param0.y;
        this.z = param0.z;
        this.xRot = param0.xRot;
        this.yRot = param0.yRot;
        this.zRot = param0.zRot;
        this.xScale = 1.0F;
        this.yScale = 1.0F;
        this.zScale = 1.0F;
    }

    public void copyFrom(ModelPart param0) {
        this.xScale = param0.xScale;
        this.yScale = param0.yScale;
        this.zScale = param0.zScale;
        this.xRot = param0.xRot;
        this.yRot = param0.yRot;
        this.zRot = param0.zRot;
        this.x = param0.x;
        this.y = param0.y;
        this.z = param0.z;
    }

    public boolean hasChild(String param0) {
        return this.children.containsKey(param0);
    }

    public ModelPart getChild(String param0) {
        ModelPart var0 = this.children.get(param0);
        if (var0 == null) {
            throw new NoSuchElementException("Can't find part " + param0);
        } else {
            return var0;
        }
    }

    public void setPos(float param0, float param1, float param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    public void setRotation(float param0, float param1, float param2) {
        this.xRot = param0;
        this.yRot = param1;
        this.zRot = param2;
    }

    public void render(PoseStack param0, VertexConsumer param1, int param2, int param3) {
        this.render(param0, param1, param2, param3, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void render(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        if (this.visible) {
            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                param0.pushPose();
                this.translateAndRotate(param0);
                if (!this.skipDraw) {
                    this.compile(param0.last(), param1, param2, param3, param4, param5, param6, param7);
                }

                for(ModelPart var0 : this.children.values()) {
                    var0.render(param0, param1, param2, param3, param4, param5, param6, param7);
                }

                param0.popPose();
            }
        }
    }

    public void visit(PoseStack param0, ModelPart.Visitor param1) {
        this.visit(param0, param1, "");
    }

    private void visit(PoseStack param0, ModelPart.Visitor param1, String param2) {
        if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
            param0.pushPose();
            this.translateAndRotate(param0);
            PoseStack.Pose var0 = param0.last();

            for(int var1 = 0; var1 < this.cubes.size(); ++var1) {
                param1.visit(var0, param2, var1, this.cubes.get(var1));
            }

            String var2 = param2 + "/";
            this.children.forEach((param3, param4) -> param4.visit(param0, param1, var2 + param3));
            param0.popPose();
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

        if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
            param0.scale(this.xScale, this.yScale, this.zScale);
        }

    }

    private void compile(PoseStack.Pose param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        for(ModelPart.Cube var0 : this.cubes) {
            var0.compile(param0, param1, param2, param3, param4, param5, param6, param7);
        }

    }

    public ModelPart.Cube getRandomCube(Random param0) {
        return this.cubes.get(param0.nextInt(this.cubes.size()));
    }

    public boolean isEmpty() {
        return this.cubes.isEmpty();
    }

    public void offsetPos(Vector3f param0) {
        this.x += param0.x();
        this.y += param0.y();
        this.z += param0.z();
    }

    public void offsetRotation(Vector3f param0) {
        this.xRot += param0.x();
        this.yRot += param0.y();
        this.zRot += param0.z();
    }

    public void offsetScale(Vector3f param0) {
        this.xScale += param0.x();
        this.yScale += param0.y();
        this.zScale += param0.z();
    }

    public Stream<ModelPart> getAllParts() {
        return Stream.concat(Stream.of(this), this.children.values().stream().flatMap(ModelPart::getAllParts));
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
            this.polygons[2] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var9, var8, var4, var5}, var13, var18, var14, var19, param12, param13, param11, Direction.DOWN
            );
            this.polygons[3] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var6, var7, var11, var10}, var14, var19, var15, var18, param12, param13, param11, Direction.UP
            );
            this.polygons[1] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var4, var8, var11, var7}, var12, var19, var13, var20, param12, param13, param11, Direction.WEST
            );
            this.polygons[4] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var5, var4, var7, var6}, var13, var19, var14, var20, param12, param13, param11, Direction.NORTH
            );
            this.polygons[0] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var9, var5, var6, var10}, var14, var19, var16, var20, param12, param13, param11, Direction.EAST
            );
            this.polygons[5] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{var8, var9, var10, var11}, var16, var19, var17, var20, param12, param13, param11, Direction.SOUTH
            );
        }

        public void compile(PoseStack.Pose param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
            Matrix4f var0 = param0.pose();
            Matrix3f var1 = param0.normal();

            for(ModelPart.Polygon var2 : this.polygons) {
                Vector3f var3 = var2.normal.copy();
                var3.transform(var1);
                float var4 = var3.x();
                float var5 = var3.y();
                float var6 = var3.z();

                for(ModelPart.Vertex var7 : var2.vertices) {
                    float var8 = var7.pos.x() / 16.0F;
                    float var9 = var7.pos.y() / 16.0F;
                    float var10 = var7.pos.z() / 16.0F;
                    Vector4f var11 = new Vector4f(var8, var9, var10, 1.0F);
                    var11.transform(var0);
                    param1.vertex(var11.x(), var11.y(), var11.z(), param4, param5, param6, param7, var7.u, var7.v, param3, param2, var4, var5, var6);
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Polygon {
        public final ModelPart.Vertex[] vertices;
        public final Vector3f normal;

        public Polygon(
            ModelPart.Vertex[] param0, float param1, float param2, float param3, float param4, float param5, float param6, boolean param7, Direction param8
        ) {
            this.vertices = param0;
            float var0 = 0.0F / param5;
            float var1 = 0.0F / param6;
            param0[0] = param0[0].remap(param3 / param5 - var0, param2 / param6 + var1);
            param0[1] = param0[1].remap(param1 / param5 + var0, param2 / param6 + var1);
            param0[2] = param0[2].remap(param1 / param5 + var0, param4 / param6 - var1);
            param0[3] = param0[3].remap(param3 / param5 - var0, param4 / param6 - var1);
            if (param7) {
                int var2 = param0.length;

                for(int var3 = 0; var3 < var2 / 2; ++var3) {
                    ModelPart.Vertex var4 = param0[var3];
                    param0[var3] = param0[var2 - 1 - var3];
                    param0[var2 - 1 - var3] = var4;
                }
            }

            this.normal = param8.step();
            if (param7) {
                this.normal.mul(-1.0F, 1.0F, 1.0F);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Vertex {
        public final Vector3f pos;
        public final float u;
        public final float v;

        public Vertex(float param0, float param1, float param2, float param3, float param4) {
            this(new Vector3f(param0, param1, param2), param3, param4);
        }

        public ModelPart.Vertex remap(float param0, float param1) {
            return new ModelPart.Vertex(this.pos, param0, param1);
        }

        public Vertex(Vector3f param0, float param1, float param2) {
            this.pos = param0;
            this.u = param1;
            this.v = param2;
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface Visitor {
        void visit(PoseStack.Pose var1, String var2, int var3, ModelPart.Cube var4);
    }
}

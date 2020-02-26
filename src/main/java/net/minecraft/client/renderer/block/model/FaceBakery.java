package net.minecraft.client.renderer.block.model;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FaceBakery {
    private static final float RESCALE_22_5 = 1.0F / (float)Math.cos((float) (Math.PI / 8)) - 1.0F;
    private static final float RESCALE_45 = 1.0F / (float)Math.cos((float) (Math.PI / 4)) - 1.0F;

    public BakedQuad bakeQuad(
        Vector3f param0,
        Vector3f param1,
        BlockElementFace param2,
        TextureAtlasSprite param3,
        Direction param4,
        ModelState param5,
        @Nullable BlockElementRotation param6,
        boolean param7,
        ResourceLocation param8
    ) {
        BlockFaceUV var0 = param2.uv;
        if (param5.isUvLocked()) {
            var0 = recomputeUVs(param2.uv, param4, param5.getRotation(), param8);
        }

        float[] var1 = new float[var0.uvs.length];
        System.arraycopy(var0.uvs, 0, var1, 0, var1.length);
        float var2 = param3.uvShrinkRatio();
        float var3 = (var0.uvs[0] + var0.uvs[0] + var0.uvs[2] + var0.uvs[2]) / 4.0F;
        float var4 = (var0.uvs[1] + var0.uvs[1] + var0.uvs[3] + var0.uvs[3]) / 4.0F;
        var0.uvs[0] = Mth.lerp(var2, var0.uvs[0], var3);
        var0.uvs[2] = Mth.lerp(var2, var0.uvs[2], var3);
        var0.uvs[1] = Mth.lerp(var2, var0.uvs[1], var4);
        var0.uvs[3] = Mth.lerp(var2, var0.uvs[3], var4);
        int[] var5 = this.makeVertices(var0, param3, param4, this.setupShape(param0, param1), param5.getRotation(), param6, param7);
        Direction var6 = calculateFacing(var5);
        System.arraycopy(var1, 0, var0.uvs, 0, var1.length);
        if (param6 == null) {
            this.recalculateWinding(var5, var6);
        }

        return new BakedQuad(var5, param2.tintIndex, var6, param3, param7);
    }

    public static BlockFaceUV recomputeUVs(BlockFaceUV param0, Direction param1, Transformation param2, ResourceLocation param3) {
        Matrix4f var0 = BlockMath.getUVLockTransform(param2, param1, () -> "Unable to resolve UVLock for model: " + param3).getMatrix();
        float var1 = param0.getU(param0.getReverseIndex(0));
        float var2 = param0.getV(param0.getReverseIndex(0));
        Vector4f var3 = new Vector4f(var1 / 16.0F, var2 / 16.0F, 0.0F, 1.0F);
        var3.transform(var0);
        float var4 = 16.0F * var3.x();
        float var5 = 16.0F * var3.y();
        float var6 = param0.getU(param0.getReverseIndex(2));
        float var7 = param0.getV(param0.getReverseIndex(2));
        Vector4f var8 = new Vector4f(var6 / 16.0F, var7 / 16.0F, 0.0F, 1.0F);
        var8.transform(var0);
        float var9 = 16.0F * var8.x();
        float var10 = 16.0F * var8.y();
        float var11;
        float var12;
        if (Math.signum(var6 - var1) == Math.signum(var9 - var4)) {
            var11 = var4;
            var12 = var9;
        } else {
            var11 = var9;
            var12 = var4;
        }

        float var15;
        float var16;
        if (Math.signum(var7 - var2) == Math.signum(var10 - var5)) {
            var15 = var5;
            var16 = var10;
        } else {
            var15 = var10;
            var16 = var5;
        }

        float var19 = (float)Math.toRadians((double)param0.rotation);
        Vector3f var20 = new Vector3f(Mth.cos(var19), Mth.sin(var19), 0.0F);
        Matrix3f var21 = new Matrix3f(var0);
        var20.transform(var21);
        int var22 = Math.floorMod(-((int)Math.round(Math.toDegrees(Math.atan2((double)var20.y(), (double)var20.x())) / 90.0)) * 90, 360);
        return new BlockFaceUV(new float[]{var11, var15, var12, var16}, var22);
    }

    private int[] makeVertices(
        BlockFaceUV param0,
        TextureAtlasSprite param1,
        Direction param2,
        float[] param3,
        Transformation param4,
        @Nullable BlockElementRotation param5,
        boolean param6
    ) {
        int[] var0 = new int[32];

        for(int var1 = 0; var1 < 4; ++var1) {
            this.bakeVertex(var0, var1, param2, param0, param3, param1, param4, param5, param6);
        }

        return var0;
    }

    private float[] setupShape(Vector3f param0, Vector3f param1) {
        float[] var0 = new float[Direction.values().length];
        var0[FaceInfo.Constants.MIN_X] = param0.x() / 16.0F;
        var0[FaceInfo.Constants.MIN_Y] = param0.y() / 16.0F;
        var0[FaceInfo.Constants.MIN_Z] = param0.z() / 16.0F;
        var0[FaceInfo.Constants.MAX_X] = param1.x() / 16.0F;
        var0[FaceInfo.Constants.MAX_Y] = param1.y() / 16.0F;
        var0[FaceInfo.Constants.MAX_Z] = param1.z() / 16.0F;
        return var0;
    }

    private void bakeVertex(
        int[] param0,
        int param1,
        Direction param2,
        BlockFaceUV param3,
        float[] param4,
        TextureAtlasSprite param5,
        Transformation param6,
        @Nullable BlockElementRotation param7,
        boolean param8
    ) {
        FaceInfo.VertexInfo var0 = FaceInfo.fromFacing(param2).getVertexInfo(param1);
        Vector3f var1 = new Vector3f(param4[var0.xFace], param4[var0.yFace], param4[var0.zFace]);
        this.applyElementRotation(var1, param7);
        this.applyModelRotation(var1, param6);
        this.fillVertex(param0, param1, var1, param5, param3);
    }

    private void fillVertex(int[] param0, int param1, Vector3f param2, TextureAtlasSprite param3, BlockFaceUV param4) {
        int var0 = param1 * 8;
        param0[var0] = Float.floatToRawIntBits(param2.x());
        param0[var0 + 1] = Float.floatToRawIntBits(param2.y());
        param0[var0 + 2] = Float.floatToRawIntBits(param2.z());
        param0[var0 + 3] = -1;
        param0[var0 + 4] = Float.floatToRawIntBits(param3.getU((double)param4.getU(param1)));
        param0[var0 + 4 + 1] = Float.floatToRawIntBits(param3.getV((double)param4.getV(param1)));
    }

    private void applyElementRotation(Vector3f param0, @Nullable BlockElementRotation param1) {
        if (param1 != null) {
            Vector3f var0;
            Vector3f var1;
            switch(param1.axis) {
                case X:
                    var0 = new Vector3f(1.0F, 0.0F, 0.0F);
                    var1 = new Vector3f(0.0F, 1.0F, 1.0F);
                    break;
                case Y:
                    var0 = new Vector3f(0.0F, 1.0F, 0.0F);
                    var1 = new Vector3f(1.0F, 0.0F, 1.0F);
                    break;
                case Z:
                    var0 = new Vector3f(0.0F, 0.0F, 1.0F);
                    var1 = new Vector3f(1.0F, 1.0F, 0.0F);
                    break;
                default:
                    throw new IllegalArgumentException("There are only 3 axes");
            }

            Quaternion var8 = new Quaternion(var0, param1.angle, true);
            if (param1.rescale) {
                if (Math.abs(param1.angle) == 22.5F) {
                    var1.mul(RESCALE_22_5);
                } else {
                    var1.mul(RESCALE_45);
                }

                var1.add(1.0F, 1.0F, 1.0F);
            } else {
                var1.set(1.0F, 1.0F, 1.0F);
            }

            this.rotateVertexBy(param0, param1.origin.copy(), new Matrix4f(var8), var1);
        }
    }

    public void applyModelRotation(Vector3f param0, Transformation param1) {
        if (param1 != Transformation.identity()) {
            this.rotateVertexBy(param0, new Vector3f(0.5F, 0.5F, 0.5F), param1.getMatrix(), new Vector3f(1.0F, 1.0F, 1.0F));
        }
    }

    private void rotateVertexBy(Vector3f param0, Vector3f param1, Matrix4f param2, Vector3f param3) {
        Vector4f var0 = new Vector4f(param0.x() - param1.x(), param0.y() - param1.y(), param0.z() - param1.z(), 1.0F);
        var0.transform(param2);
        var0.mul(param3);
        param0.set(var0.x() + param1.x(), var0.y() + param1.y(), var0.z() + param1.z());
    }

    public static Direction calculateFacing(int[] param0) {
        Vector3f var0 = new Vector3f(Float.intBitsToFloat(param0[0]), Float.intBitsToFloat(param0[1]), Float.intBitsToFloat(param0[2]));
        Vector3f var1 = new Vector3f(Float.intBitsToFloat(param0[8]), Float.intBitsToFloat(param0[9]), Float.intBitsToFloat(param0[10]));
        Vector3f var2 = new Vector3f(Float.intBitsToFloat(param0[16]), Float.intBitsToFloat(param0[17]), Float.intBitsToFloat(param0[18]));
        Vector3f var3 = var0.copy();
        var3.sub(var1);
        Vector3f var4 = var2.copy();
        var4.sub(var1);
        Vector3f var5 = var4.copy();
        var5.cross(var3);
        var5.normalize();
        Direction var6 = null;
        float var7 = 0.0F;

        for(Direction var8 : Direction.values()) {
            Vec3i var9 = var8.getNormal();
            Vector3f var10 = new Vector3f((float)var9.getX(), (float)var9.getY(), (float)var9.getZ());
            float var11 = var5.dot(var10);
            if (var11 >= 0.0F && var11 > var7) {
                var7 = var11;
                var6 = var8;
            }
        }

        return var6 == null ? Direction.UP : var6;
    }

    private void recalculateWinding(int[] param0, Direction param1) {
        int[] var0 = new int[param0.length];
        System.arraycopy(param0, 0, var0, 0, param0.length);
        float[] var1 = new float[Direction.values().length];
        var1[FaceInfo.Constants.MIN_X] = 999.0F;
        var1[FaceInfo.Constants.MIN_Y] = 999.0F;
        var1[FaceInfo.Constants.MIN_Z] = 999.0F;
        var1[FaceInfo.Constants.MAX_X] = -999.0F;
        var1[FaceInfo.Constants.MAX_Y] = -999.0F;
        var1[FaceInfo.Constants.MAX_Z] = -999.0F;

        for(int var2 = 0; var2 < 4; ++var2) {
            int var3 = 8 * var2;
            float var4 = Float.intBitsToFloat(var0[var3]);
            float var5 = Float.intBitsToFloat(var0[var3 + 1]);
            float var6 = Float.intBitsToFloat(var0[var3 + 2]);
            if (var4 < var1[FaceInfo.Constants.MIN_X]) {
                var1[FaceInfo.Constants.MIN_X] = var4;
            }

            if (var5 < var1[FaceInfo.Constants.MIN_Y]) {
                var1[FaceInfo.Constants.MIN_Y] = var5;
            }

            if (var6 < var1[FaceInfo.Constants.MIN_Z]) {
                var1[FaceInfo.Constants.MIN_Z] = var6;
            }

            if (var4 > var1[FaceInfo.Constants.MAX_X]) {
                var1[FaceInfo.Constants.MAX_X] = var4;
            }

            if (var5 > var1[FaceInfo.Constants.MAX_Y]) {
                var1[FaceInfo.Constants.MAX_Y] = var5;
            }

            if (var6 > var1[FaceInfo.Constants.MAX_Z]) {
                var1[FaceInfo.Constants.MAX_Z] = var6;
            }
        }

        FaceInfo var7 = FaceInfo.fromFacing(param1);

        for(int var8 = 0; var8 < 4; ++var8) {
            int var9 = 8 * var8;
            FaceInfo.VertexInfo var10 = var7.getVertexInfo(var8);
            float var11 = var1[var10.xFace];
            float var12 = var1[var10.yFace];
            float var13 = var1[var10.zFace];
            param0[var9] = Float.floatToRawIntBits(var11);
            param0[var9 + 1] = Float.floatToRawIntBits(var12);
            param0[var9 + 2] = Float.floatToRawIntBits(var13);

            for(int var14 = 0; var14 < 4; ++var14) {
                int var15 = 8 * var14;
                float var16 = Float.intBitsToFloat(var0[var15]);
                float var17 = Float.intBitsToFloat(var0[var15 + 1]);
                float var18 = Float.intBitsToFloat(var0[var15 + 2]);
                if (Mth.equal(var11, var16) && Mth.equal(var12, var17) && Mth.equal(var13, var18)) {
                    param0[var9 + 4] = var0[var15 + 4];
                    param0[var9 + 4 + 1] = var0[var15 + 4 + 1];
                }
            }
        }

    }
}

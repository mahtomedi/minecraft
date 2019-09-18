package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import net.minecraft.client.renderer.culling.FrustumData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Frustum extends FrustumData {
    private static final Frustum FRUSTUM = new Frustum();
    private final FloatBuffer _proj = MemoryTracker.createFloatBuffer(16);
    private final FloatBuffer _modl = MemoryTracker.createFloatBuffer(16);
    private final FloatBuffer _clip = MemoryTracker.createFloatBuffer(16);

    public static FrustumData getFrustum() {
        FRUSTUM.calculateFrustum();
        return FRUSTUM;
    }

    private void normalizePlane(float[] param0) {
        float var0 = (float)Math.sqrt((double)(param0[0] * param0[0] + param0[1] * param0[1] + param0[2] * param0[2]));
        param0[0] /= var0;
        param0[1] /= var0;
        param0[2] /= var0;
        param0[3] /= var0;
    }

    public void calculateFrustum() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_calculateFrustum);
        } else {
            this._calculateFrustum();
        }

    }

    private void _calculateFrustum() {
        ((Buffer)this._proj).clear();
        ((Buffer)this._modl).clear();
        ((Buffer)this._clip).clear();
        GlStateManager._getMatrix(2983, this._proj);
        GlStateManager._getMatrix(2982, this._modl);
        float[] var0 = this.projectionMatrix;
        float[] var1 = this.modelViewMatrix;
        ((Buffer)this._proj).flip().limit(16);
        this._proj.get(var0);
        ((Buffer)this._modl).flip().limit(16);
        this._modl.get(var1);
        this.clip[0] = var1[0] * var0[0] + var1[1] * var0[4] + var1[2] * var0[8] + var1[3] * var0[12];
        this.clip[1] = var1[0] * var0[1] + var1[1] * var0[5] + var1[2] * var0[9] + var1[3] * var0[13];
        this.clip[2] = var1[0] * var0[2] + var1[1] * var0[6] + var1[2] * var0[10] + var1[3] * var0[14];
        this.clip[3] = var1[0] * var0[3] + var1[1] * var0[7] + var1[2] * var0[11] + var1[3] * var0[15];
        this.clip[4] = var1[4] * var0[0] + var1[5] * var0[4] + var1[6] * var0[8] + var1[7] * var0[12];
        this.clip[5] = var1[4] * var0[1] + var1[5] * var0[5] + var1[6] * var0[9] + var1[7] * var0[13];
        this.clip[6] = var1[4] * var0[2] + var1[5] * var0[6] + var1[6] * var0[10] + var1[7] * var0[14];
        this.clip[7] = var1[4] * var0[3] + var1[5] * var0[7] + var1[6] * var0[11] + var1[7] * var0[15];
        this.clip[8] = var1[8] * var0[0] + var1[9] * var0[4] + var1[10] * var0[8] + var1[11] * var0[12];
        this.clip[9] = var1[8] * var0[1] + var1[9] * var0[5] + var1[10] * var0[9] + var1[11] * var0[13];
        this.clip[10] = var1[8] * var0[2] + var1[9] * var0[6] + var1[10] * var0[10] + var1[11] * var0[14];
        this.clip[11] = var1[8] * var0[3] + var1[9] * var0[7] + var1[10] * var0[11] + var1[11] * var0[15];
        this.clip[12] = var1[12] * var0[0] + var1[13] * var0[4] + var1[14] * var0[8] + var1[15] * var0[12];
        this.clip[13] = var1[12] * var0[1] + var1[13] * var0[5] + var1[14] * var0[9] + var1[15] * var0[13];
        this.clip[14] = var1[12] * var0[2] + var1[13] * var0[6] + var1[14] * var0[10] + var1[15] * var0[14];
        this.clip[15] = var1[12] * var0[3] + var1[13] * var0[7] + var1[14] * var0[11] + var1[15] * var0[15];
        float[] var2 = this.frustumData[0];
        var2[0] = this.clip[3] - this.clip[0];
        var2[1] = this.clip[7] - this.clip[4];
        var2[2] = this.clip[11] - this.clip[8];
        var2[3] = this.clip[15] - this.clip[12];
        this.normalizePlane(var2);
        float[] var3 = this.frustumData[1];
        var3[0] = this.clip[3] + this.clip[0];
        var3[1] = this.clip[7] + this.clip[4];
        var3[2] = this.clip[11] + this.clip[8];
        var3[3] = this.clip[15] + this.clip[12];
        this.normalizePlane(var3);
        float[] var4 = this.frustumData[2];
        var4[0] = this.clip[3] + this.clip[1];
        var4[1] = this.clip[7] + this.clip[5];
        var4[2] = this.clip[11] + this.clip[9];
        var4[3] = this.clip[15] + this.clip[13];
        this.normalizePlane(var4);
        float[] var5 = this.frustumData[3];
        var5[0] = this.clip[3] - this.clip[1];
        var5[1] = this.clip[7] - this.clip[5];
        var5[2] = this.clip[11] - this.clip[9];
        var5[3] = this.clip[15] - this.clip[13];
        this.normalizePlane(var5);
        float[] var6 = this.frustumData[4];
        var6[0] = this.clip[3] - this.clip[2];
        var6[1] = this.clip[7] - this.clip[6];
        var6[2] = this.clip[11] - this.clip[10];
        var6[3] = this.clip[15] - this.clip[14];
        this.normalizePlane(var6);
        float[] var7 = this.frustumData[5];
        var7[0] = this.clip[3] + this.clip[2];
        var7[1] = this.clip[7] + this.clip[6];
        var7[2] = this.clip[11] + this.clip[10];
        var7[3] = this.clip[15] + this.clip[14];
        this.normalizePlane(var7);
    }
}

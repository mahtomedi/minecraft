package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureAtlasSprite {
    private final ResourceLocation name;
    protected final int width;
    protected final int height;
    protected NativeImage[] mainImage;
    @Nullable
    protected int[] framesX;
    @Nullable
    protected int[] framesY;
    protected NativeImage[] activeFrame;
    private AnimationMetadataSection metadata;
    protected int x;
    protected int y;
    private float u0;
    private float u1;
    private float v0;
    private float v1;
    protected int frame;
    protected int subFrame;
    private static final float[] POW22 = Util.make(new float[256], param0 -> {
        for(int var0 = 0; var0 < param0.length; ++var0) {
            param0[var0] = (float)Math.pow((double)((float)var0 / 255.0F), 2.2);
        }

    });

    protected TextureAtlasSprite(ResourceLocation param0, int param1, int param2) {
        this.name = param0;
        this.width = param1;
        this.height = param2;
    }

    protected TextureAtlasSprite(ResourceLocation param0, PngInfo param1, @Nullable AnimationMetadataSection param2) {
        this.name = param0;
        if (param2 != null) {
            Pair<Integer, Integer> var0 = getFrameSize(param2.getFrameWidth(), param2.getFrameHeight(), param1.width, param1.height);
            this.width = var0.getFirst();
            this.height = var0.getSecond();
            if (!isDivisionInteger(param1.width, this.width) || !isDivisionInteger(param1.height, this.height)) {
                throw new IllegalArgumentException(
                    String.format("Image size %s,%s is not multiply of frame size %s,%s", this.width, this.height, param1.width, param1.height)
                );
            }
        } else {
            this.width = param1.width;
            this.height = param1.height;
        }

        this.metadata = param2;
    }

    private static Pair<Integer, Integer> getFrameSize(int param0, int param1, int param2, int param3) {
        if (param0 != -1) {
            return param1 != -1 ? Pair.of(param0, param1) : Pair.of(param0, param3);
        } else if (param1 != -1) {
            return Pair.of(param2, param1);
        } else {
            int var0 = Math.min(param2, param3);
            return Pair.of(var0, var0);
        }
    }

    private static boolean isDivisionInteger(int param0, int param1) {
        return param0 / param1 * param1 == param0;
    }

    private void generateMipLevels(int param0) {
        NativeImage[] var0 = new NativeImage[param0 + 1];
        var0[0] = this.mainImage[0];
        if (param0 > 0) {
            boolean var1 = false;

            label71:
            for(int var2 = 0; var2 < this.mainImage[0].getWidth(); ++var2) {
                for(int var3 = 0; var3 < this.mainImage[0].getHeight(); ++var3) {
                    if (this.mainImage[0].getPixelRGBA(var2, var3) >> 24 == 0) {
                        var1 = true;
                        break label71;
                    }
                }
            }

            for(int var4 = 1; var4 <= param0; ++var4) {
                if (this.mainImage.length > var4 && this.mainImage[var4] != null) {
                    var0[var4] = this.mainImage[var4];
                } else {
                    NativeImage var5 = var0[var4 - 1];
                    NativeImage var6 = new NativeImage(var5.getWidth() >> 1, var5.getHeight() >> 1, false);
                    int var7 = var6.getWidth();
                    int var8 = var6.getHeight();

                    for(int var9 = 0; var9 < var7; ++var9) {
                        for(int var10 = 0; var10 < var8; ++var10) {
                            var6.setPixelRGBA(
                                var9,
                                var10,
                                alphaBlend(
                                    var5.getPixelRGBA(var9 * 2 + 0, var10 * 2 + 0),
                                    var5.getPixelRGBA(var9 * 2 + 1, var10 * 2 + 0),
                                    var5.getPixelRGBA(var9 * 2 + 0, var10 * 2 + 1),
                                    var5.getPixelRGBA(var9 * 2 + 1, var10 * 2 + 1),
                                    var1
                                )
                            );
                        }
                    }

                    var0[var4] = var6;
                }
            }

            for(int var11 = param0 + 1; var11 < this.mainImage.length; ++var11) {
                if (this.mainImage[var11] != null) {
                    this.mainImage[var11].close();
                }
            }
        }

        this.mainImage = var0;
    }

    private static int alphaBlend(int param0, int param1, int param2, int param3, boolean param4) {
        if (param4) {
            float var0 = 0.0F;
            float var1 = 0.0F;
            float var2 = 0.0F;
            float var3 = 0.0F;
            if (param0 >> 24 != 0) {
                var0 += getPow22(param0 >> 24);
                var1 += getPow22(param0 >> 16);
                var2 += getPow22(param0 >> 8);
                var3 += getPow22(param0 >> 0);
            }

            if (param1 >> 24 != 0) {
                var0 += getPow22(param1 >> 24);
                var1 += getPow22(param1 >> 16);
                var2 += getPow22(param1 >> 8);
                var3 += getPow22(param1 >> 0);
            }

            if (param2 >> 24 != 0) {
                var0 += getPow22(param2 >> 24);
                var1 += getPow22(param2 >> 16);
                var2 += getPow22(param2 >> 8);
                var3 += getPow22(param2 >> 0);
            }

            if (param3 >> 24 != 0) {
                var0 += getPow22(param3 >> 24);
                var1 += getPow22(param3 >> 16);
                var2 += getPow22(param3 >> 8);
                var3 += getPow22(param3 >> 0);
            }

            var0 /= 4.0F;
            var1 /= 4.0F;
            var2 /= 4.0F;
            var3 /= 4.0F;
            int var4 = (int)(Math.pow((double)var0, 0.45454545454545453) * 255.0);
            int var5 = (int)(Math.pow((double)var1, 0.45454545454545453) * 255.0);
            int var6 = (int)(Math.pow((double)var2, 0.45454545454545453) * 255.0);
            int var7 = (int)(Math.pow((double)var3, 0.45454545454545453) * 255.0);
            if (var4 < 96) {
                var4 = 0;
            }

            return var4 << 24 | var5 << 16 | var6 << 8 | var7;
        } else {
            int var8 = gammaBlend(param0, param1, param2, param3, 24);
            int var9 = gammaBlend(param0, param1, param2, param3, 16);
            int var10 = gammaBlend(param0, param1, param2, param3, 8);
            int var11 = gammaBlend(param0, param1, param2, param3, 0);
            return var8 << 24 | var9 << 16 | var10 << 8 | var11;
        }
    }

    private static int gammaBlend(int param0, int param1, int param2, int param3, int param4) {
        float var0 = getPow22(param0 >> param4);
        float var1 = getPow22(param1 >> param4);
        float var2 = getPow22(param2 >> param4);
        float var3 = getPow22(param3 >> param4);
        float var4 = (float)((double)((float)Math.pow((double)(var0 + var1 + var2 + var3) * 0.25, 0.45454545454545453)));
        return (int)((double)var4 * 255.0);
    }

    private static float getPow22(int param0) {
        return POW22[param0 & 0xFF];
    }

    private void upload(int param0) {
        int var0 = 0;
        int var1 = 0;
        if (this.framesX != null) {
            var0 = this.framesX[param0] * this.width;
            var1 = this.framesY[param0] * this.height;
        }

        this.upload(var0, var1, this.mainImage);
    }

    private void upload(int param0, int param1, NativeImage[] param2) {
        for(int var0 = 0; var0 < this.mainImage.length; ++var0) {
            param2[var0]
                .upload(
                    var0,
                    this.x >> var0,
                    this.y >> var0,
                    param0 >> var0,
                    param1 >> var0,
                    this.width >> var0,
                    this.height >> var0,
                    this.mainImage.length > 1,
                    false
                );
        }

    }

    public void init(int param0, int param1, int param2, int param3) {
        this.x = param2;
        this.y = param3;
        this.u0 = (float)param2 / (float)param0;
        this.u1 = (float)(param2 + this.width) / (float)param0;
        this.v0 = (float)param3 / (float)param1;
        this.v1 = (float)(param3 + this.height) / (float)param1;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public float getU0() {
        return this.u0;
    }

    public float getU1() {
        return this.u1;
    }

    public float getU(double param0) {
        float var0 = this.u1 - this.u0;
        return this.u0 + var0 * (float)param0 / 16.0F;
    }

    public float getUOffset(float param0) {
        float var0 = this.u1 - this.u0;
        return (param0 - this.u0) / var0 * 16.0F;
    }

    public float getV0() {
        return this.v0;
    }

    public float getV1() {
        return this.v1;
    }

    public float getV(double param0) {
        float var0 = this.v1 - this.v0;
        return this.v0 + var0 * (float)param0 / 16.0F;
    }

    public float getVOffset(float param0) {
        float var0 = this.v1 - this.v0;
        return (param0 - this.v0) / var0 * 16.0F;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public void cycleFrames() {
        ++this.subFrame;
        if (this.subFrame >= this.metadata.getFrameTime(this.frame)) {
            int var0 = this.metadata.getFrameIndex(this.frame);
            int var1 = this.metadata.getFrameCount() == 0 ? this.getFrameCount() : this.metadata.getFrameCount();
            this.frame = (this.frame + 1) % var1;
            this.subFrame = 0;
            int var2 = this.metadata.getFrameIndex(this.frame);
            if (var0 != var2 && var2 >= 0 && var2 < this.getFrameCount()) {
                this.upload(var2);
            }
        } else if (this.metadata.isInterpolatedFrames()) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(this::uploadInterpolatedFrame);
            } else {
                this.uploadInterpolatedFrame();
            }
        }

    }

    private void uploadInterpolatedFrame() {
        double var0x = 1.0 - (double)this.subFrame / (double)this.metadata.getFrameTime(this.frame);
        int var1x = this.metadata.getFrameIndex(this.frame);
        int var2x = this.metadata.getFrameCount() == 0 ? this.getFrameCount() : this.metadata.getFrameCount();
        int var3 = this.metadata.getFrameIndex((this.frame + 1) % var2x);
        if (var1x != var3 && var3 >= 0 && var3 < this.getFrameCount()) {
            if (this.activeFrame == null || this.activeFrame.length != this.mainImage.length) {
                if (this.activeFrame != null) {
                    for(NativeImage var4 : this.activeFrame) {
                        if (var4 != null) {
                            var4.close();
                        }
                    }
                }

                this.activeFrame = new NativeImage[this.mainImage.length];
            }

            for(int var5 = 0; var5 < this.mainImage.length; ++var5) {
                int var6 = this.width >> var5;
                int var7 = this.height >> var5;
                if (this.activeFrame[var5] == null) {
                    this.activeFrame[var5] = new NativeImage(var6, var7, false);
                }

                for(int var8 = 0; var8 < var7; ++var8) {
                    for(int var9 = 0; var9 < var6; ++var9) {
                        int var10 = this.getPixel(var1x, var5, var9, var8);
                        int var11 = this.getPixel(var3, var5, var9, var8);
                        int var12 = this.mix(var0x, var10 >> 16 & 0xFF, var11 >> 16 & 0xFF);
                        int var13 = this.mix(var0x, var10 >> 8 & 0xFF, var11 >> 8 & 0xFF);
                        int var14 = this.mix(var0x, var10 & 0xFF, var11 & 0xFF);
                        this.activeFrame[var5].setPixelRGBA(var9, var8, var10 & 0xFF000000 | var12 << 16 | var13 << 8 | var14);
                    }
                }
            }

            this.upload(0, 0, this.activeFrame);
        }

    }

    private int mix(double param0, int param1, int param2) {
        return (int)(param0 * (double)param1 + (1.0 - param0) * (double)param2);
    }

    public int getFrameCount() {
        return this.framesX == null ? 0 : this.framesX.length;
    }

    public void loadData(Resource param0, int param1) throws IOException {
        NativeImage var0 = NativeImage.read(param0.getInputStream());
        this.mainImage = new NativeImage[param1];
        this.mainImage[0] = var0;
        int var1;
        if (this.metadata != null && this.metadata.getFrameWidth() != -1) {
            var1 = var0.getWidth() / this.metadata.getFrameWidth();
        } else {
            var1 = var0.getWidth() / this.width;
        }

        int var3;
        if (this.metadata != null && this.metadata.getFrameHeight() != -1) {
            var3 = var0.getHeight() / this.metadata.getFrameHeight();
        } else {
            var3 = var0.getHeight() / this.height;
        }

        if (this.metadata != null && this.metadata.getFrameCount() > 0) {
            int var5 = this.metadata.getUniqueFrameIndices().stream().max(Integer::compareTo).get() + 1;
            this.framesX = new int[var5];
            this.framesY = new int[var5];
            Arrays.fill(this.framesX, -1);
            Arrays.fill(this.framesY, -1);

            for(int var6 : this.metadata.getUniqueFrameIndices()) {
                if (var6 >= var1 * var3) {
                    throw new RuntimeException("invalid frameindex " + var6);
                }

                int var7 = var6 / var1;
                int var8 = var6 % var1;
                this.framesX[var6] = var8;
                this.framesY[var6] = var7;
            }
        } else {
            List<AnimationFrame> var9 = Lists.newArrayList();
            int var10 = var1 * var3;
            this.framesX = new int[var10];
            this.framesY = new int[var10];

            for(int var11 = 0; var11 < var3; ++var11) {
                for(int var12 = 0; var12 < var1; ++var12) {
                    int var13 = var11 * var1 + var12;
                    this.framesX[var13] = var12;
                    this.framesY[var13] = var11;
                    var9.add(new AnimationFrame(var13, -1));
                }
            }

            int var14 = 1;
            boolean var15 = false;
            if (this.metadata != null) {
                var14 = this.metadata.getDefaultFrameTime();
                var15 = this.metadata.isInterpolatedFrames();
            }

            this.metadata = new AnimationMetadataSection(var9, this.width, this.height, var14, var15);
        }

    }

    public void applyMipmapping(int param0) {
        try {
            this.generateMipLevels(param0);
        } catch (Throwable var5) {
            CrashReport var1 = CrashReport.forThrowable(var5, "Generating mipmaps for frame");
            CrashReportCategory var2 = var1.addCategory("Frame being iterated");
            var2.setDetail("Frame sizes", () -> {
                StringBuilder var0x = new StringBuilder();

                for(NativeImage var1x : this.mainImage) {
                    if (var0x.length() > 0) {
                        var0x.append(", ");
                    }

                    var0x.append(var1x == null ? "null" : var1x.getWidth() + "x" + var1x.getHeight());
                }

                return var0x.toString();
            });
            throw new ReportedException(var1);
        }
    }

    public void wipeFrameData() {
        if (this.mainImage != null) {
            for(NativeImage var0 : this.mainImage) {
                if (var0 != null) {
                    var0.close();
                }
            }
        }

        this.mainImage = null;
        if (this.activeFrame != null) {
            for(NativeImage var1 : this.activeFrame) {
                if (var1 != null) {
                    var1.close();
                }
            }
        }

        this.activeFrame = null;
    }

    public boolean isAnimation() {
        return this.metadata != null && this.metadata.getFrameCount() > 1;
    }

    @Override
    public String toString() {
        int var0 = this.framesX == null ? 0 : this.framesX.length;
        return "TextureAtlasSprite{name='"
            + this.name
            + '\''
            + ", frameCount="
            + var0
            + ", x="
            + this.x
            + ", y="
            + this.y
            + ", height="
            + this.height
            + ", width="
            + this.width
            + ", u0="
            + this.u0
            + ", u1="
            + this.u1
            + ", v0="
            + this.v0
            + ", v1="
            + this.v1
            + '}';
    }

    private int getPixel(int param0, int param1, int param2, int param3) {
        return this.mainImage[param1]
            .getPixelRGBA(param2 + (this.framesX[param0] * this.width >> param1), param3 + (this.framesY[param0] * this.height >> param1));
    }

    public boolean isTransparent(int param0, int param1, int param2) {
        return (this.mainImage[0].getPixelRGBA(param1 + this.framesX[param0] * this.width, param2 + this.framesY[param0] * this.height) >> 24 & 0xFF) == 0;
    }

    public void uploadFirstFrame() {
        this.upload(0);
    }
}

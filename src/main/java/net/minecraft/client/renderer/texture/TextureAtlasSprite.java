package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureAtlasSprite implements AutoCloseable {
    private final TextureAtlas atlas;
    private final TextureAtlasSprite.Info info;
    private final AnimationMetadataSection metadata;
    protected final NativeImage[] mainImage;
    private final int[] framesX;
    private final int[] framesY;
    @Nullable
    private final TextureAtlasSprite.InterpolationData interpolationData;
    private final int x;
    private final int y;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private int frame;
    private int subFrame;

    protected TextureAtlasSprite(
        TextureAtlas param0, TextureAtlasSprite.Info param1, int param2, int param3, int param4, int param5, int param6, NativeImage param7
    ) {
        this.atlas = param0;
        AnimationMetadataSection var0 = param1.metadata;
        int var1 = param1.width;
        int var2 = param1.height;
        this.x = param5;
        this.y = param6;
        this.u0 = (float)param5 / (float)param3;
        this.u1 = (float)(param5 + var1) / (float)param3;
        this.v0 = (float)param6 / (float)param4;
        this.v1 = (float)(param6 + var2) / (float)param4;
        int var3 = param7.getWidth() / var0.getFrameWidth(var1);
        int var4 = param7.getHeight() / var0.getFrameHeight(var2);
        if (var0.getFrameCount() > 0) {
            int var5 = var0.getUniqueFrameIndices().stream().max(Integer::compareTo).get() + 1;
            this.framesX = new int[var5];
            this.framesY = new int[var5];
            Arrays.fill(this.framesX, -1);
            Arrays.fill(this.framesY, -1);

            for(int var6 : var0.getUniqueFrameIndices()) {
                if (var6 >= var3 * var4) {
                    throw new RuntimeException("invalid frameindex " + var6);
                }

                int var7 = var6 / var3;
                int var8 = var6 % var3;
                this.framesX[var6] = var8;
                this.framesY[var6] = var7;
            }
        } else {
            List<AnimationFrame> var9 = Lists.newArrayList();
            int var10 = var3 * var4;
            this.framesX = new int[var10];
            this.framesY = new int[var10];

            for(int var11 = 0; var11 < var4; ++var11) {
                for(int var12 = 0; var12 < var3; ++var12) {
                    int var13 = var11 * var3 + var12;
                    this.framesX[var13] = var12;
                    this.framesY[var13] = var11;
                    var9.add(new AnimationFrame(var13, -1));
                }
            }

            var0 = new AnimationMetadataSection(var9, var1, var2, var0.getDefaultFrameTime(), var0.isInterpolatedFrames());
        }

        this.info = new TextureAtlasSprite.Info(param1.name, var1, var2, var0);
        this.metadata = var0;

        try {
            try {
                this.mainImage = MipmapGenerator.generateMipLevels(param7, param2);
            } catch (Throwable var191) {
                CrashReport var15 = CrashReport.forThrowable(var191, "Generating mipmaps for frame");
                CrashReportCategory var16 = var15.addCategory("Frame being iterated");
                var16.setDetail("First frame", () -> {
                    StringBuilder var0x = new StringBuilder();
                    if (var0x.length() > 0) {
                        var0x.append(", ");
                    }

                    var0x.append(param7.getWidth()).append("x").append(param7.getHeight());
                    return var0x.toString();
                });
                throw new ReportedException(var15);
            }
        } catch (Throwable var20) {
            CrashReport var18 = CrashReport.forThrowable(var20, "Applying mipmap");
            CrashReportCategory var19 = var18.addCategory("Sprite being mipmapped");
            var19.setDetail("Sprite name", () -> this.getName().toString());
            var19.setDetail("Sprite size", () -> this.getWidth() + " x " + this.getHeight());
            var19.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
            var19.setDetail("Mipmap levels", param2);
            throw new ReportedException(var18);
        }

        if (var0.isInterpolatedFrames()) {
            this.interpolationData = new TextureAtlasSprite.InterpolationData(param1, param2);
        } else {
            this.interpolationData = null;
        }

    }

    private void upload(int param0) {
        int var0 = this.framesX[param0] * this.info.width;
        int var1 = this.framesY[param0] * this.info.height;
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
                    this.info.width >> var0,
                    this.info.height >> var0,
                    this.mainImage.length > 1,
                    false
                );
        }

    }

    public int getWidth() {
        return this.info.width;
    }

    public int getHeight() {
        return this.info.height;
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

    public ResourceLocation getName() {
        return this.info.name;
    }

    public TextureAtlas atlas() {
        return this.atlas;
    }

    public int getFrameCount() {
        return this.framesX.length;
    }

    @Override
    public void close() {
        for(NativeImage var0 : this.mainImage) {
            if (var0 != null) {
                var0.close();
            }
        }

        if (this.interpolationData != null) {
            this.interpolationData.close();
        }

    }

    @Override
    public String toString() {
        int var0 = this.framesX.length;
        return "TextureAtlasSprite{name='"
            + this.info.name
            + '\''
            + ", frameCount="
            + var0
            + ", x="
            + this.x
            + ", y="
            + this.y
            + ", height="
            + this.info.height
            + ", width="
            + this.info.width
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

    public boolean isTransparent(int param0, int param1, int param2) {
        return (this.mainImage[0].getPixelRGBA(param1 + this.framesX[param0] * this.info.width, param2 + this.framesY[param0] * this.info.height) >> 24 & 0xFF)
            == 0;
    }

    public void uploadFirstFrame() {
        this.upload(0);
    }

    private float atlasSize() {
        float var0 = (float)this.info.width / (this.u1 - this.u0);
        float var1 = (float)this.info.height / (this.v1 - this.v0);
        return Math.max(var1, var0);
    }

    public float uvShrinkRatio() {
        return 4.0F / this.atlasSize();
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
        } else if (this.interpolationData != null) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> param0.uploadInterpolatedFrame());
            } else {
                this.interpolationData.uploadInterpolatedFrame();
            }
        }

    }

    public boolean isAnimation() {
        return this.metadata.getFrameCount() > 1;
    }

    public VertexConsumer wrap(VertexConsumer param0) {
        return new SpriteCoordinateExpander(param0, this);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Info {
        private final ResourceLocation name;
        private final int width;
        private final int height;
        private final AnimationMetadataSection metadata;

        public Info(ResourceLocation param0, int param1, int param2, AnimationMetadataSection param3) {
            this.name = param0;
            this.width = param1;
            this.height = param2;
            this.metadata = param3;
        }

        public ResourceLocation name() {
            return this.name;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }
    }

    @OnlyIn(Dist.CLIENT)
    final class InterpolationData implements AutoCloseable {
        private final NativeImage[] activeFrame;

        private InterpolationData(TextureAtlasSprite.Info param0, int param1) {
            this.activeFrame = new NativeImage[param1 + 1];

            for(int param2 = 0; param2 < this.activeFrame.length; ++param2) {
                int var0 = param0.width >> param2;
                int var1 = param0.height >> param2;
                if (this.activeFrame[param2] == null) {
                    this.activeFrame[param2] = new NativeImage(var0, var1, false);
                }
            }

        }

        private void uploadInterpolatedFrame() {
            double var0 = 1.0 - (double)TextureAtlasSprite.this.subFrame / (double)TextureAtlasSprite.this.metadata.getFrameTime(TextureAtlasSprite.this.frame);
            int var1 = TextureAtlasSprite.this.metadata.getFrameIndex(TextureAtlasSprite.this.frame);
            int var2 = TextureAtlasSprite.this.metadata.getFrameCount() == 0
                ? TextureAtlasSprite.this.getFrameCount()
                : TextureAtlasSprite.this.metadata.getFrameCount();
            int var3 = TextureAtlasSprite.this.metadata.getFrameIndex((TextureAtlasSprite.this.frame + 1) % var2);
            if (var1 != var3 && var3 >= 0 && var3 < TextureAtlasSprite.this.getFrameCount()) {
                for(int var4 = 0; var4 < this.activeFrame.length; ++var4) {
                    int var5 = TextureAtlasSprite.this.info.width >> var4;
                    int var6 = TextureAtlasSprite.this.info.height >> var4;

                    for(int var7 = 0; var7 < var6; ++var7) {
                        for(int var8 = 0; var8 < var5; ++var8) {
                            int var9 = this.getPixel(var1, var4, var8, var7);
                            int var10 = this.getPixel(var3, var4, var8, var7);
                            int var11 = this.mix(var0, var9 >> 16 & 0xFF, var10 >> 16 & 0xFF);
                            int var12 = this.mix(var0, var9 >> 8 & 0xFF, var10 >> 8 & 0xFF);
                            int var13 = this.mix(var0, var9 & 0xFF, var10 & 0xFF);
                            this.activeFrame[var4].setPixelRGBA(var8, var7, var9 & 0xFF000000 | var11 << 16 | var12 << 8 | var13);
                        }
                    }
                }

                TextureAtlasSprite.this.upload(0, 0, this.activeFrame);
            }

        }

        private int getPixel(int param0, int param1, int param2, int param3) {
            return TextureAtlasSprite.this.mainImage[param1]
                .getPixelRGBA(
                    param2 + (TextureAtlasSprite.this.framesX[param0] * TextureAtlasSprite.this.info.width >> param1),
                    param3 + (TextureAtlasSprite.this.framesY[param0] * TextureAtlasSprite.this.info.height >> param1)
                );
        }

        private int mix(double param0, int param1, int param2) {
            return (int)(param0 * (double)param1 + (1.0 - param0) * (double)param2);
        }

        @Override
        public void close() {
            for(NativeImage var0 : this.activeFrame) {
                if (var0 != null) {
                    var0.close();
                }
            }

        }
    }
}

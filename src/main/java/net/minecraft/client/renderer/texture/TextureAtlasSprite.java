package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureAtlasSprite implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final TextureAtlas atlas;
    private final ResourceLocation name;
    final int width;
    final int height;
    protected final NativeImage[] mainImage;
    @Nullable
    private final TextureAtlasSprite.AnimatedTexture animatedTexture;
    private final int x;
    private final int y;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;

    protected TextureAtlasSprite(
        TextureAtlas param0, TextureAtlasSprite.Info param1, int param2, int param3, int param4, int param5, int param6, NativeImage param7
    ) {
        this.atlas = param0;
        this.width = param1.width;
        this.height = param1.height;
        this.name = param1.name;
        this.x = param5;
        this.y = param6;
        this.u0 = (float)param5 / (float)param3;
        this.u1 = (float)(param5 + this.width) / (float)param3;
        this.v0 = (float)param6 / (float)param4;
        this.v1 = (float)(param6 + this.height) / (float)param4;
        this.animatedTexture = this.createTicker(param1, param7.getWidth(), param7.getHeight(), param2);

        try {
            try {
                this.mainImage = MipmapGenerator.generateMipLevels(param7, param2);
            } catch (Throwable var12) {
                CrashReport var1 = CrashReport.forThrowable(var12, "Generating mipmaps for frame");
                CrashReportCategory var2 = var1.addCategory("Frame being iterated");
                var2.setDetail("First frame", () -> {
                    StringBuilder var0x = new StringBuilder();
                    if (var0x.length() > 0) {
                        var0x.append(", ");
                    }

                    var0x.append(param7.getWidth()).append("x").append(param7.getHeight());
                    return var0x.toString();
                });
                throw new ReportedException(var1);
            }
        } catch (Throwable var13) {
            CrashReport var4 = CrashReport.forThrowable(var13, "Applying mipmap");
            CrashReportCategory var5 = var4.addCategory("Sprite being mipmapped");
            var5.setDetail("Sprite name", this.name::toString);
            var5.setDetail("Sprite size", () -> this.width + " x " + this.height);
            var5.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
            var5.setDetail("Mipmap levels", param2);
            throw new ReportedException(var4);
        }
    }

    private int getFrameCount() {
        return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
    }

    @Nullable
    private TextureAtlasSprite.AnimatedTexture createTicker(TextureAtlasSprite.Info param0, int param1, int param2, int param3) {
        AnimationMetadataSection var0 = param0.metadata;
        int var1 = param1 / var0.getFrameWidth(param0.width);
        int var2 = param2 / var0.getFrameHeight(param0.height);
        int var3 = var1 * var2;
        List<TextureAtlasSprite.FrameInfo> var4 = Lists.newArrayList();
        var0.forEachFrame((param1x, param2x) -> var4.add(new TextureAtlasSprite.FrameInfo(param1x, param2x)));
        if (var4.isEmpty()) {
            for(int var5 = 0; var5 < var3; ++var5) {
                var4.add(new TextureAtlasSprite.FrameInfo(var5, var0.getDefaultFrameTime()));
            }
        } else {
            int var6 = 0;
            IntSet var7 = new IntOpenHashSet();

            for(Iterator<TextureAtlasSprite.FrameInfo> var8 = var4.iterator(); var8.hasNext(); ++var6) {
                TextureAtlasSprite.FrameInfo var9 = var8.next();
                boolean var10 = true;
                if (var9.time <= 0) {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", this.name, var6, var9.time);
                    var10 = false;
                }

                if (var9.index < 0 || var9.index >= var3) {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", this.name, var6, var9.index);
                    var10 = false;
                }

                if (var10) {
                    var7.add(var9.index);
                } else {
                    var8.remove();
                }
            }

            int[] var11 = IntStream.range(0, var3).filter(param1x -> !var7.contains(param1x)).toArray();
            if (var11.length > 0) {
                LOGGER.warn("Unused frames in sprite {}: {}", this.name, Arrays.toString(var11));
            }
        }

        if (var4.size() <= 1) {
            return null;
        } else {
            TextureAtlasSprite.InterpolationData var12 = var0.isInterpolatedFrames() ? new TextureAtlasSprite.InterpolationData(param0, param3) : null;
            return new TextureAtlasSprite.AnimatedTexture(ImmutableList.copyOf(var4), var1, var12);
        }
    }

    void upload(int param0, int param1, NativeImage[] param2) {
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

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
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

    public TextureAtlas atlas() {
        return this.atlas;
    }

    public IntStream getUniqueFrames() {
        return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
    }

    @Override
    public void close() {
        for(NativeImage var0 : this.mainImage) {
            if (var0 != null) {
                var0.close();
            }
        }

        if (this.animatedTexture != null) {
            this.animatedTexture.close();
        }

    }

    @Override
    public String toString() {
        return "TextureAtlasSprite{name='"
            + this.name
            + "', frameCount="
            + this.getFrameCount()
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
            + "}";
    }

    public boolean isTransparent(int param0, int param1, int param2) {
        int var0 = param1;
        int var1 = param2;
        if (this.animatedTexture != null) {
            var0 = param1 + this.animatedTexture.getFrameX(param0) * this.width;
            var1 = param2 + this.animatedTexture.getFrameY(param0) * this.height;
        }

        return (this.mainImage[0].getPixelRGBA(var0, var1) >> 24 & 0xFF) == 0;
    }

    public void uploadFirstFrame() {
        if (this.animatedTexture != null) {
            this.animatedTexture.uploadFirstFrame();
        } else {
            this.upload(0, 0, this.mainImage);
        }

    }

    private float atlasSize() {
        float var0 = (float)this.width / (this.u1 - this.u0);
        float var1 = (float)this.height / (this.v1 - this.v0);
        return Math.max(var1, var0);
    }

    public float uvShrinkRatio() {
        return 4.0F / this.atlasSize();
    }

    @Nullable
    public Tickable getAnimationTicker() {
        return this.animatedTexture;
    }

    public VertexConsumer wrap(VertexConsumer param0) {
        return new SpriteCoordinateExpander(param0, this);
    }

    @OnlyIn(Dist.CLIENT)
    class AnimatedTexture implements Tickable, AutoCloseable {
        int frame;
        int subFrame;
        final List<TextureAtlasSprite.FrameInfo> frames;
        private final int frameRowSize;
        @Nullable
        private final TextureAtlasSprite.InterpolationData interpolationData;

        AnimatedTexture(List<TextureAtlasSprite.FrameInfo> param0, @Nullable int param1, TextureAtlasSprite.InterpolationData param2) {
            this.frames = param0;
            this.frameRowSize = param1;
            this.interpolationData = param2;
        }

        int getFrameX(int param0) {
            return param0 % this.frameRowSize;
        }

        int getFrameY(int param0) {
            return param0 / this.frameRowSize;
        }

        private void uploadFrame(int param0) {
            int var0 = this.getFrameX(param0) * TextureAtlasSprite.this.width;
            int var1 = this.getFrameY(param0) * TextureAtlasSprite.this.height;
            TextureAtlasSprite.this.upload(var0, var1, TextureAtlasSprite.this.mainImage);
        }

        @Override
        public void close() {
            if (this.interpolationData != null) {
                this.interpolationData.close();
            }

        }

        @Override
        public void tick() {
            ++this.subFrame;
            TextureAtlasSprite.FrameInfo var0 = this.frames.get(this.frame);
            if (this.subFrame >= var0.time) {
                int var1 = var0.index;
                this.frame = (this.frame + 1) % this.frames.size();
                this.subFrame = 0;
                int var2 = this.frames.get(this.frame).index;
                if (var1 != var2) {
                    this.uploadFrame(var2);
                }
            } else if (this.interpolationData != null) {
                if (!RenderSystem.isOnRenderThread()) {
                    RenderSystem.recordRenderCall(() -> this.interpolationData.uploadInterpolatedFrame(this));
                } else {
                    this.interpolationData.uploadInterpolatedFrame(this);
                }
            }

        }

        public void uploadFirstFrame() {
            this.uploadFrame(this.frames.get(0).index);
        }

        public IntStream getUniqueFrames() {
            return this.frames.stream().mapToInt(param0 -> param0.index).distinct();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class FrameInfo {
        final int index;
        final int time;

        FrameInfo(int param0, int param1) {
            this.index = param0;
            this.time = param1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Info {
        final ResourceLocation name;
        final int width;
        final int height;
        final AnimationMetadataSection metadata;

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

        InterpolationData(TextureAtlasSprite.Info param0, int param1) {
            this.activeFrame = new NativeImage[param1 + 1];

            for(int param2 = 0; param2 < this.activeFrame.length; ++param2) {
                int var0 = param0.width >> param2;
                int var1 = param0.height >> param2;
                if (this.activeFrame[param2] == null) {
                    this.activeFrame[param2] = new NativeImage(var0, var1, false);
                }
            }

        }

        void uploadInterpolatedFrame(TextureAtlasSprite.AnimatedTexture param0) {
            TextureAtlasSprite.FrameInfo var0 = param0.frames.get(param0.frame);
            double var1 = 1.0 - (double)param0.subFrame / (double)var0.time;
            int var2 = var0.index;
            int var3 = param0.frames.get((param0.frame + 1) % param0.frames.size()).index;
            if (var2 != var3) {
                for(int var4 = 0; var4 < this.activeFrame.length; ++var4) {
                    int var5 = TextureAtlasSprite.this.width >> var4;
                    int var6 = TextureAtlasSprite.this.height >> var4;

                    for(int var7 = 0; var7 < var6; ++var7) {
                        for(int var8 = 0; var8 < var5; ++var8) {
                            int var9 = this.getPixel(param0, var2, var4, var8, var7);
                            int var10 = this.getPixel(param0, var3, var4, var8, var7);
                            int var11 = this.mix(var1, var9 >> 16 & 0xFF, var10 >> 16 & 0xFF);
                            int var12 = this.mix(var1, var9 >> 8 & 0xFF, var10 >> 8 & 0xFF);
                            int var13 = this.mix(var1, var9 & 0xFF, var10 & 0xFF);
                            this.activeFrame[var4].setPixelRGBA(var8, var7, var9 & 0xFF000000 | var11 << 16 | var12 << 8 | var13);
                        }
                    }
                }

                TextureAtlasSprite.this.upload(0, 0, this.activeFrame);
            }

        }

        private int getPixel(TextureAtlasSprite.AnimatedTexture param0, int param1, int param2, int param3, int param4) {
            return TextureAtlasSprite.this.mainImage[param2]
                .getPixelRGBA(
                    param3 + (param0.getFrameX(param1) * TextureAtlasSprite.this.width >> param2),
                    param4 + (param0.getFrameY(param1) * TextureAtlasSprite.this.height >> param2)
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

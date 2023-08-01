package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SpriteContents implements Stitcher.Entry, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation name;
    final int width;
    final int height;
    private final NativeImage originalImage;
    NativeImage[] byMipLevel;
    @Nullable
    private final SpriteContents.AnimatedTexture animatedTexture;
    private final ResourceMetadata metadata;

    public SpriteContents(ResourceLocation param0, FrameSize param1, NativeImage param2, ResourceMetadata param3) {
        this.name = param0;
        this.width = param1.width();
        this.height = param1.height();
        this.metadata = param3;
        AnimationMetadataSection var0 = param3.getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
        this.animatedTexture = this.createAnimatedTexture(param1, param2.getWidth(), param2.getHeight(), var0);
        this.originalImage = param2;
        this.byMipLevel = new NativeImage[]{this.originalImage};
    }

    public void increaseMipLevel(int param0) {
        try {
            this.byMipLevel = MipmapGenerator.generateMipLevels(this.byMipLevel, param0);
        } catch (Throwable var6) {
            CrashReport var1 = CrashReport.forThrowable(var6, "Generating mipmaps for frame");
            CrashReportCategory var2 = var1.addCategory("Sprite being mipmapped");
            var2.setDetail("First frame", () -> {
                StringBuilder var0x = new StringBuilder();
                if (var0x.length() > 0) {
                    var0x.append(", ");
                }

                var0x.append(this.originalImage.getWidth()).append("x").append(this.originalImage.getHeight());
                return var0x.toString();
            });
            CrashReportCategory var3 = var1.addCategory("Frame being iterated");
            var3.setDetail("Sprite name", this.name);
            var3.setDetail("Sprite size", () -> this.width + " x " + this.height);
            var3.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
            var3.setDetail("Mipmap levels", param0);
            throw new ReportedException(var1);
        }
    }

    private int getFrameCount() {
        return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
    }

    @Nullable
    private SpriteContents.AnimatedTexture createAnimatedTexture(FrameSize param0, int param1, int param2, AnimationMetadataSection param3) {
        int var0 = param1 / param0.width();
        int var1 = param2 / param0.height();
        int var2 = var0 * var1;
        List<SpriteContents.FrameInfo> var3 = new ArrayList<>();
        param3.forEachFrame((param1x, param2x) -> var3.add(new SpriteContents.FrameInfo(param1x, param2x)));
        if (var3.isEmpty()) {
            for(int var4 = 0; var4 < var2; ++var4) {
                var3.add(new SpriteContents.FrameInfo(var4, param3.getDefaultFrameTime()));
            }
        } else {
            int var5 = 0;
            IntSet var6 = new IntOpenHashSet();

            for(Iterator<SpriteContents.FrameInfo> var7 = var3.iterator(); var7.hasNext(); ++var5) {
                SpriteContents.FrameInfo var8 = var7.next();
                boolean var9 = true;
                if (var8.time <= 0) {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", this.name, var5, var8.time);
                    var9 = false;
                }

                if (var8.index < 0 || var8.index >= var2) {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", this.name, var5, var8.index);
                    var9 = false;
                }

                if (var9) {
                    var6.add(var8.index);
                } else {
                    var7.remove();
                }
            }

            int[] var10 = IntStream.range(0, var2).filter(param1x -> !var6.contains(param1x)).toArray();
            if (var10.length > 0) {
                LOGGER.warn("Unused frames in sprite {}: {}", this.name, Arrays.toString(var10));
            }
        }

        return var3.size() <= 1 ? null : new SpriteContents.AnimatedTexture(ImmutableList.copyOf(var3), var0, param3.isInterpolatedFrames());
    }

    void upload(int param0, int param1, int param2, int param3, NativeImage[] param4) {
        for(int var0 = 0; var0 < this.byMipLevel.length; ++var0) {
            param4[var0]
                .upload(
                    var0,
                    param0 >> var0,
                    param1 >> var0,
                    param2 >> var0,
                    param3 >> var0,
                    this.width >> var0,
                    this.height >> var0,
                    this.byMipLevel.length > 1,
                    false
                );
        }

    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public ResourceLocation name() {
        return this.name;
    }

    public IntStream getUniqueFrames() {
        return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
    }

    @Nullable
    public SpriteTicker createTicker() {
        return this.animatedTexture != null ? this.animatedTexture.createTicker() : null;
    }

    public ResourceMetadata metadata() {
        return this.metadata;
    }

    @Override
    public void close() {
        for(NativeImage var0 : this.byMipLevel) {
            var0.close();
        }

    }

    @Override
    public String toString() {
        return "SpriteContents{name=" + this.name + ", frameCount=" + this.getFrameCount() + ", height=" + this.height + ", width=" + this.width + "}";
    }

    public boolean isTransparent(int param0, int param1, int param2) {
        int var0 = param1;
        int var1 = param2;
        if (this.animatedTexture != null) {
            var0 = param1 + this.animatedTexture.getFrameX(param0) * this.width;
            var1 = param2 + this.animatedTexture.getFrameY(param0) * this.height;
        }

        return (this.originalImage.getPixelRGBA(var0, var1) >> 24 & 0xFF) == 0;
    }

    public void uploadFirstFrame(int param0, int param1) {
        if (this.animatedTexture != null) {
            this.animatedTexture.uploadFirstFrame(param0, param1);
        } else {
            this.upload(param0, param1, 0, 0, this.byMipLevel);
        }

    }

    @OnlyIn(Dist.CLIENT)
    class AnimatedTexture {
        final List<SpriteContents.FrameInfo> frames;
        private final int frameRowSize;
        private final boolean interpolateFrames;

        AnimatedTexture(List<SpriteContents.FrameInfo> param0, int param1, boolean param2) {
            this.frames = param0;
            this.frameRowSize = param1;
            this.interpolateFrames = param2;
        }

        int getFrameX(int param0) {
            return param0 % this.frameRowSize;
        }

        int getFrameY(int param0) {
            return param0 / this.frameRowSize;
        }

        void uploadFrame(int param0, int param1, int param2) {
            int var0 = this.getFrameX(param2) * SpriteContents.this.width;
            int var1 = this.getFrameY(param2) * SpriteContents.this.height;
            SpriteContents.this.upload(param0, param1, var0, var1, SpriteContents.this.byMipLevel);
        }

        public SpriteTicker createTicker() {
            return SpriteContents.this.new Ticker(this, this.interpolateFrames ? SpriteContents.this.new InterpolationData() : null);
        }

        public void uploadFirstFrame(int param0, int param1) {
            this.uploadFrame(param0, param1, this.frames.get(0).index);
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
    final class InterpolationData implements AutoCloseable {
        private final NativeImage[] activeFrame = new NativeImage[SpriteContents.this.byMipLevel.length];

        InterpolationData() {
            for(int param0 = 0; param0 < this.activeFrame.length; ++param0) {
                int var0 = SpriteContents.this.width >> param0;
                int var1 = SpriteContents.this.height >> param0;
                this.activeFrame[param0] = new NativeImage(var0, var1, false);
            }

        }

        void uploadInterpolatedFrame(int param0, int param1, SpriteContents.Ticker param2) {
            SpriteContents.AnimatedTexture var0 = param2.animationInfo;
            List<SpriteContents.FrameInfo> var1 = var0.frames;
            SpriteContents.FrameInfo var2 = var1.get(param2.frame);
            double var3 = 1.0 - (double)param2.subFrame / (double)var2.time;
            int var4 = var2.index;
            int var5 = var1.get((param2.frame + 1) % var1.size()).index;
            if (var4 != var5) {
                for(int var6 = 0; var6 < this.activeFrame.length; ++var6) {
                    int var7 = SpriteContents.this.width >> var6;
                    int var8 = SpriteContents.this.height >> var6;

                    for(int var9 = 0; var9 < var8; ++var9) {
                        for(int var10 = 0; var10 < var7; ++var10) {
                            int var11 = this.getPixel(var0, var4, var6, var10, var9);
                            int var12 = this.getPixel(var0, var5, var6, var10, var9);
                            int var13 = this.mix(var3, var11 >> 16 & 0xFF, var12 >> 16 & 0xFF);
                            int var14 = this.mix(var3, var11 >> 8 & 0xFF, var12 >> 8 & 0xFF);
                            int var15 = this.mix(var3, var11 & 0xFF, var12 & 0xFF);
                            this.activeFrame[var6].setPixelRGBA(var10, var9, var11 & 0xFF000000 | var13 << 16 | var14 << 8 | var15);
                        }
                    }
                }

                SpriteContents.this.upload(param0, param1, 0, 0, this.activeFrame);
            }

        }

        private int getPixel(SpriteContents.AnimatedTexture param0, int param1, int param2, int param3, int param4) {
            return SpriteContents.this.byMipLevel[param2]
                .getPixelRGBA(
                    param3 + (param0.getFrameX(param1) * SpriteContents.this.width >> param2),
                    param4 + (param0.getFrameY(param1) * SpriteContents.this.height >> param2)
                );
        }

        private int mix(double param0, int param1, int param2) {
            return (int)(param0 * (double)param1 + (1.0 - param0) * (double)param2);
        }

        @Override
        public void close() {
            for(NativeImage var0 : this.activeFrame) {
                var0.close();
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    class Ticker implements SpriteTicker {
        int frame;
        int subFrame;
        final SpriteContents.AnimatedTexture animationInfo;
        @Nullable
        private final SpriteContents.InterpolationData interpolationData;

        Ticker(@Nullable SpriteContents.AnimatedTexture param0, SpriteContents.InterpolationData param1) {
            this.animationInfo = param0;
            this.interpolationData = param1;
        }

        @Override
        public void tickAndUpload(int param0, int param1) {
            ++this.subFrame;
            SpriteContents.FrameInfo var0 = this.animationInfo.frames.get(this.frame);
            if (this.subFrame >= var0.time) {
                int var1 = var0.index;
                this.frame = (this.frame + 1) % this.animationInfo.frames.size();
                this.subFrame = 0;
                int var2 = this.animationInfo.frames.get(this.frame).index;
                if (var1 != var2) {
                    this.animationInfo.uploadFrame(param0, param1, var2);
                }
            } else if (this.interpolationData != null) {
                if (!RenderSystem.isOnRenderThread()) {
                    RenderSystem.recordRenderCall(() -> this.interpolationData.uploadInterpolatedFrame(param0, param1, this));
                } else {
                    this.interpolationData.uploadInterpolatedFrame(param0, param1, this);
                }
            }

        }

        @Override
        public void close() {
            if (this.interpolationData != null) {
                this.interpolationData.close();
            }

        }
    }
}

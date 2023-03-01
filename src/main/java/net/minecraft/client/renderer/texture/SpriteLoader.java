package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SpriteLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;
    private final int minWidth;
    private final int minHeight;

    public SpriteLoader(ResourceLocation param0, int param1, int param2, int param3) {
        this.location = param0;
        this.maxSupportedTextureSize = param1;
        this.minWidth = param2;
        this.minHeight = param3;
    }

    public static SpriteLoader create(TextureAtlas param0) {
        return new SpriteLoader(param0.location(), param0.maxSupportedTextureSize(), param0.getWidth(), param0.getHeight());
    }

    public SpriteLoader.Preparations stitch(List<SpriteContents> param0, int param1, Executor param2) {
        int var0 = this.maxSupportedTextureSize;
        Stitcher<SpriteContents> var1 = new Stitcher<>(var0, var0, param1);
        int var2 = Integer.MAX_VALUE;
        int var3 = 1 << param1;

        for(SpriteContents var4 : param0) {
            var2 = Math.min(var2, Math.min(var4.width(), var4.height()));
            int var5 = Math.min(Integer.lowestOneBit(var4.width()), Integer.lowestOneBit(var4.height()));
            if (var5 < var3) {
                LOGGER.warn(
                    "Texture {} with size {}x{} limits mip level from {} to {}", var4.name(), var4.width(), var4.height(), Mth.log2(var3), Mth.log2(var5)
                );
                var3 = var5;
            }

            var1.registerSprite(var4);
        }

        int var6 = Math.min(var2, var3);
        int var7 = Mth.log2(var6);
        int var8;
        if (var7 < param1) {
            LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, param1, var7, var6);
            var8 = var7;
        } else {
            var8 = param1;
        }

        try {
            var1.stitch();
        } catch (StitcherException var161) {
            CrashReport var11 = CrashReport.forThrowable(var161, "Stitching");
            CrashReportCategory var12 = var11.addCategory("Stitcher");
            var12.setDetail(
                "Sprites",
                var161.getAllSprites()
                    .stream()
                    .map(param0x -> String.format(Locale.ROOT, "%s[%dx%d]", param0x.name(), param0x.width(), param0x.height()))
                    .collect(Collectors.joining(","))
            );
            var12.setDetail("Max Texture Size", var0);
            throw new ReportedException(var11);
        }

        int var13 = Math.max(var1.getWidth(), this.minWidth);
        int var14 = Math.max(var1.getHeight(), this.minHeight);
        Map<ResourceLocation, TextureAtlasSprite> var15 = this.getStitchedSprites(var1, var13, var14);
        TextureAtlasSprite var16 = var15.get(MissingTextureAtlasSprite.getLocation());
        CompletableFuture<Void> var17;
        if (var8 > 0) {
            var17 = CompletableFuture.runAsync(() -> var15.values().forEach(param1x -> param1x.contents().increaseMipLevel(var8)), param2);
        } else {
            var17 = CompletableFuture.completedFuture(null);
        }

        return new SpriteLoader.Preparations(var13, var14, var8, var16, var15, var17);
    }

    public static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(List<Supplier<SpriteContents>> param0, Executor param1) {
        List<CompletableFuture<SpriteContents>> var0 = param0.stream().map(param1x -> CompletableFuture.supplyAsync(param1x, param1)).toList();
        return Util.sequence(var0).thenApply(param0x -> param0x.stream().filter(Objects::nonNull).toList());
    }

    public CompletableFuture<SpriteLoader.Preparations> loadAndStitch(ResourceManager param0, ResourceLocation param1, int param2, Executor param3) {
        return CompletableFuture.<List<Supplier<SpriteContents>>>supplyAsync(() -> SpriteResourceLoader.load(param0, param1).list(param0), param3)
            .thenCompose(param1x -> runSpriteSuppliers(param1x, param3))
            .thenApply(param2x -> this.stitch(param2x, param2, param3));
    }

    @Nullable
    public static SpriteContents loadSprite(ResourceLocation param0, Resource param1) {
        AnimationMetadataSection var0;
        try {
            var0 = param1.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
        } catch (Exception var81) {
            LOGGER.error("Unable to parse metadata from {}", param0, var81);
            return null;
        }

        NativeImage var4;
        try (InputStream var3 = param1.open()) {
            var4 = NativeImage.read(var3);
        } catch (IOException var10) {
            LOGGER.error("Using missing texture, unable to load {}", param0, var10);
            return null;
        }

        FrameSize var8 = var0.calculateFrameSize(var4.getWidth(), var4.getHeight());
        if (Mth.isMultipleOf(var4.getWidth(), var8.width()) && Mth.isMultipleOf(var4.getHeight(), var8.height())) {
            return new SpriteContents(param0, var8, var4, var0);
        } else {
            LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", param0, var4.getWidth(), var4.getHeight(), var8.width(), var8.height());
            var4.close();
            return null;
        }
    }

    private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> param0, int param1, int param2) {
        Map<ResourceLocation, TextureAtlasSprite> var0 = new HashMap<>();
        param0.gatherSprites((param3, param4, param5) -> var0.put(param3.name(), new TextureAtlasSprite(this.location, param3, param1, param2, param4, param5)));
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static record Preparations(
        int width,
        int height,
        int mipLevel,
        TextureAtlasSprite missing,
        Map<ResourceLocation, TextureAtlasSprite> regions,
        CompletableFuture<Void> readyForUpload
    ) {
        public CompletableFuture<SpriteLoader.Preparations> waitForUpload() {
            return this.readyForUpload.thenApply(param0 -> this);
        }
    }
}

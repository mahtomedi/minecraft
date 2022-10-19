package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
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
    private static final FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");

    public SpriteLoader(ResourceLocation param0, int param1) {
        this.location = param0;
        this.maxSupportedTextureSize = param1;
    }

    public static SpriteLoader create(TextureAtlas param0) {
        return new SpriteLoader(param0.location(), param0.maxSupportedTextureSize());
    }

    public CompletableFuture<SpriteLoader.Preparations> stitch(Map<ResourceLocation, Resource> param0, int param1, Executor param2) {
        return this.loadSpriteContents(param0, param2)
            .thenApplyAsync(
                param2x -> {
                    int var0 = this.maxSupportedTextureSize;
                    Stitcher<SpriteContents> var1x = new Stitcher(var0, var0, param1);
                    int var2x = Integer.MAX_VALUE;
                    int var3x = 1 << param1;
        
                    for(SpriteContents var4 : param2x) {
                        var2x = Math.min(var2x, Math.min(var4.width(), var4.height()));
                        int var5 = Math.min(Integer.lowestOneBit(var4.width()), Integer.lowestOneBit(var4.height()));
                        if (var5 < var3x) {
                            LOGGER.warn(
                                "Texture {} with size {}x{} limits mip level from {} to {}",
                                var4.name(),
                                var4.width(),
                                var4.height(),
                                Mth.log2(var3x),
                                Mth.log2(var5)
                            );
                            var3x = var5;
                        }
        
                        var1x.registerSprite(var4);
                    }
        
                    int var6 = Math.min(var2x, var3x);
                    int var7 = Mth.log2(var6);
                    int var8;
                    if (var7 < param1) {
                        LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, param1, var7, var6);
                        var8 = var7;
                    } else {
                        var8 = param1;
                    }
        
                    try {
                        var1x.stitch();
                    } catch (StitcherException var141) {
                        CrashReport var11 = CrashReport.forThrowable(var141, "Stitching");
                        CrashReportCategory var12 = var11.addCategory("Stitcher");
                        var12.setDetail(
                            "Sprites",
                            var141.getAllSprites()
                                .stream()
                                .map(param0x -> String.format(Locale.ROOT, "%s[%dx%d]", param0x.name(), param0x.width(), param0x.height()))
                                .collect(Collectors.joining(","))
                        );
                        var12.setDetail("Max Texture Size", var0);
                        throw new ReportedException(var11);
                    }
        
                    Map<ResourceLocation, TextureAtlasSprite> var13 = this.getStitchedSprites(var1x);
                    TextureAtlasSprite var14 = var13.get(MissingTextureAtlasSprite.getLocation());
                    CompletableFuture<Void> var15;
                    if (var8 > 0) {
                        var15 = CompletableFuture.runAsync(() -> var13.values().forEach(param1x -> param1x.contents().increaseMipLevel(var8)), param2);
                    } else {
                        var15 = CompletableFuture.completedFuture(null);
                    }
        
                    return new SpriteLoader.Preparations(var1x.getWidth(), var1x.getHeight(), var8, var14, var13, var15);
                },
                param2
            );
    }

    private CompletableFuture<List<SpriteContents>> loadSpriteContents(Map<ResourceLocation, Resource> param0, Executor param1) {
        List<CompletableFuture<SpriteContents>> var0 = new ArrayList<>();
        var0.add(CompletableFuture.supplyAsync(MissingTextureAtlasSprite::create, param1));
        param0.forEach((param2, param3) -> var0.add(CompletableFuture.supplyAsync(() -> this.loadSprite(param2, param3), param1)));
        return Util.sequence(var0).thenApply(param0x -> param0x.stream().filter(Objects::nonNull).toList());
    }

    @Nullable
    private SpriteContents loadSprite(ResourceLocation param0, Resource param1) {
        AnimationMetadataSection var0;
        try {
            var0 = param1.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
        } catch (Exception var9) {
            LOGGER.error("Unable to parse metadata from {} : {}", this.location, var9);
            return null;
        }

        NativeImage var4;
        try (InputStream var3 = param1.open()) {
            var4 = NativeImage.read(var3);
        } catch (IOException var11) {
            LOGGER.error("Using missing texture, unable to load {} : {}", this.location, var11);
            return null;
        }

        FrameSize var8 = var0.calculateFrameSize(var4.getWidth(), var4.getHeight());
        if (Mth.isDivisionInteger(var4.getWidth(), var8.width()) && Mth.isDivisionInteger(var4.getHeight(), var8.height())) {
            return new SpriteContents(param0, var8, var4, var0);
        } else {
            LOGGER.error(
                "Image {} size {},{} is not multiple of frame size {},{}", this.location, var4.getWidth(), var4.getHeight(), var8.width(), var8.height()
            );
            var4.close();
            return null;
        }
    }

    private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> param0) {
        Map<ResourceLocation, TextureAtlasSprite> var0 = new HashMap<>();
        int var1 = param0.getWidth();
        int var2 = param0.getHeight();
        param0.gatherSprites((param3, param4, param5) -> var0.put(param3.name(), new TextureAtlasSprite(this.location, param3, var1, var2, param4, param5)));
        return var0;
    }

    public static void addSprite(ResourceManager param0, ResourceLocation param1, BiConsumer<ResourceLocation, Resource> param2) {
        ResourceLocation var0 = TEXTURE_ID_CONVERTER.idToFile(param1);
        Optional<Resource> var1 = param0.getResource(var0);
        if (var1.isPresent()) {
            param2.accept(param1, var1.get());
        } else {
            LOGGER.warn("Missing sprite: {}", var0);
        }

    }

    public static void listSprites(ResourceManager param0, String param1, BiConsumer<ResourceLocation, Resource> param2) {
        listSprites(param0, "textures/" + param1, param1 + "/", param2);
    }

    public static void listSprites(ResourceManager param0, String param1, String param2, BiConsumer<ResourceLocation, Resource> param3) {
        FileToIdConverter var0 = new FileToIdConverter(param1, ".png");
        var0.listMatchingResources(param0).forEach((param3x, param4) -> {
            ResourceLocation var0x = var0.fileToId(param3x).withPrefix(param2);
            param3.accept(var0x, param4);
        });
    }

    public static Map<ResourceLocation, Resource> listSprites(ResourceManager param0, String param1) {
        return listSprites(param0, "textures/" + param1, param1 + "/");
    }

    public static Map<ResourceLocation, Resource> listSprites(ResourceManager param0, String param1, String param2) {
        Map<ResourceLocation, Resource> var0 = new HashMap<>();
        listSprites(param0, param1, param2, var0::put);
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

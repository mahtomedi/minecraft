package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureAtlas extends AbstractTexture implements Tickable {
    private static final Logger LOGGER = LogManager.getLogger();
    @Deprecated
    public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
    @Deprecated
    public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
    private static final String FILE_EXTENSION = ".png";
    private final List<Tickable> animatedTextures = Lists.newArrayList();
    private final Set<ResourceLocation> sprites = Sets.newHashSet();
    private final Map<ResourceLocation, TextureAtlasSprite> texturesByName = Maps.newHashMap();
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;

    public TextureAtlas(ResourceLocation param0) {
        this.location = param0;
        this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
    }

    @Override
    public void load(ResourceManager param0) {
    }

    public void reload(TextureAtlas.Preparations param0) {
        this.sprites.clear();
        this.sprites.addAll(param0.sprites);
        LOGGER.info("Created: {}x{}x{} {}-atlas", param0.width, param0.height, param0.mipLevel, this.location);
        TextureUtil.prepareImage(this.getId(), param0.mipLevel, param0.width, param0.height);
        this.clearTextureData();

        for(TextureAtlasSprite var0 : param0.regions) {
            this.texturesByName.put(var0.getName(), var0);

            try {
                var0.uploadFirstFrame();
            } catch (Throwable var7) {
                CrashReport var2 = CrashReport.forThrowable(var7, "Stitching texture atlas");
                CrashReportCategory var3 = var2.addCategory("Texture being stitched together");
                var3.setDetail("Atlas path", this.location);
                var3.setDetail("Sprite", var0);
                throw new ReportedException(var2);
            }

            Tickable var4 = var0.getAnimationTicker();
            if (var4 != null) {
                this.animatedTextures.add(var4);
            }
        }

    }

    public TextureAtlas.Preparations prepareToStitch(ResourceManager param0, Stream<ResourceLocation> param1, ProfilerFiller param2, int param3) {
        param2.push("preparing");
        Set<ResourceLocation> var0 = param1.peek(param0x -> {
            if (param0x == null) {
                throw new IllegalArgumentException("Location cannot be null!");
            }
        }).collect(Collectors.toSet());
        int var1 = this.maxSupportedTextureSize;
        Stitcher var2 = new Stitcher(var1, var1, param3);
        int var3 = Integer.MAX_VALUE;
        int var4 = 1 << param3;
        param2.popPush("extracting_frames");

        for(TextureAtlasSprite.Info var5 : this.getBasicSpriteInfos(param0, var0)) {
            var3 = Math.min(var3, Math.min(var5.width(), var5.height()));
            int var6 = Math.min(Integer.lowestOneBit(var5.width()), Integer.lowestOneBit(var5.height()));
            if (var6 < var4) {
                LOGGER.warn(
                    "Texture {} with size {}x{} limits mip level from {} to {}", var5.name(), var5.width(), var5.height(), Mth.log2(var4), Mth.log2(var6)
                );
                var4 = var6;
            }

            var2.registerSprite(var5);
        }

        int var7 = Math.min(var3, var4);
        int var8 = Mth.log2(var7);
        int var9;
        if (var8 < param3) {
            LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, param3, var8, var7);
            var9 = var8;
        } else {
            var9 = param3;
        }

        param2.popPush("register");
        var2.registerSprite(MissingTextureAtlasSprite.info());
        param2.popPush("stitching");

        try {
            var2.stitch();
        } catch (StitcherException var16) {
            CrashReport var12 = CrashReport.forThrowable(var16, "Stitching");
            CrashReportCategory var13 = var12.addCategory("Stitcher");
            var13.setDetail(
                "Sprites",
                var16.getAllSprites()
                    .stream()
                    .map(param0x -> String.format("%s[%dx%d]", param0x.name(), param0x.width(), param0x.height()))
                    .collect(Collectors.joining(","))
            );
            var13.setDetail("Max Texture Size", var1);
            throw new ReportedException(var12);
        }

        param2.popPush("loading");
        List<TextureAtlasSprite> var14 = this.getLoadedSprites(param0, var2, var9);
        param2.pop();
        return new TextureAtlas.Preparations(var0, var2.getWidth(), var2.getHeight(), var9, var14);
    }

    private Collection<TextureAtlasSprite.Info> getBasicSpriteInfos(ResourceManager param0, Set<ResourceLocation> param1) {
        List<CompletableFuture<?>> var0 = Lists.newArrayList();
        Queue<TextureAtlasSprite.Info> var1 = new ConcurrentLinkedQueue<>();

        for(ResourceLocation var2 : param1) {
            if (!MissingTextureAtlasSprite.getLocation().equals(var2)) {
                var0.add(CompletableFuture.runAsync(() -> {
                    ResourceLocation var0x = this.getResourceLocation(var2);

                    TextureAtlasSprite.Info var5;
                    try (Resource var1x = param0.getResource(var0x)) {
                        PngInfo var2x = new PngInfo(var1x.toString(), var1x.getInputStream());
                        AnimationMetadataSection var3x = var1x.getMetadata(AnimationMetadataSection.SERIALIZER);
                        if (var3x == null) {
                            var3x = AnimationMetadataSection.EMPTY;
                        }

                        Pair<Integer, Integer> var4x = var3x.getFrameSize(var2x.width, var2x.height);
                        var5 = new TextureAtlasSprite.Info(var2, var4x.getFirst(), var4x.getSecond(), var3x);
                    } catch (RuntimeException var12) {
                        LOGGER.error("Unable to parse metadata from {} : {}", var0x, var12);
                        return;
                    } catch (IOException var13) {
                        LOGGER.error("Using missing texture, unable to load {} : {}", var0x, var13);
                        return;
                    }

                    var1.add(var5);
                }, Util.backgroundExecutor()));
            }
        }

        CompletableFuture.allOf(var0.toArray(new CompletableFuture[0])).join();
        return var1;
    }

    private List<TextureAtlasSprite> getLoadedSprites(ResourceManager param0, Stitcher param1, int param2) {
        Queue<TextureAtlasSprite> var0 = new ConcurrentLinkedQueue<>();
        List<CompletableFuture<?>> var1 = Lists.newArrayList();
        param1.gatherSprites((param4, param5, param6, param7, param8) -> {
            if (param4 == MissingTextureAtlasSprite.info()) {
                MissingTextureAtlasSprite var0x = MissingTextureAtlasSprite.newInstance(this, param2, param5, param6, param7, param8);
                var0.add(var0x);
            } else {
                var1.add(CompletableFuture.runAsync(() -> {
                    TextureAtlasSprite var0xx = this.load(param0, param4, param5, param6, param2, param7, param8);
                    if (var0xx != null) {
                        var0.add(var0xx);
                    }

                }, Util.backgroundExecutor()));
            }

        });
        CompletableFuture.allOf(var1.toArray(new CompletableFuture[0])).join();
        return Lists.newArrayList(var0);
    }

    @Nullable
    private TextureAtlasSprite load(ResourceManager param0, TextureAtlasSprite.Info param1, int param2, int param3, int param4, int param5, int param6) {
        ResourceLocation var0 = this.getResourceLocation(param1.name());

        try {
            TextureAtlasSprite var11;
            try (Resource var1 = param0.getResource(var0)) {
                NativeImage var2 = NativeImage.read(var1.getInputStream());
                var11 = new TextureAtlasSprite(this, param1, param4, param2, param3, param5, param6, var2);
            }

            return var11;
        } catch (RuntimeException var14) {
            LOGGER.error("Unable to parse metadata from {}", var0, var14);
            return null;
        } catch (IOException var15) {
            LOGGER.error("Using missing texture, unable to load {}", var0, var15);
            return null;
        }
    }

    private ResourceLocation getResourceLocation(ResourceLocation param0) {
        return new ResourceLocation(param0.getNamespace(), String.format("textures/%s%s", param0.getPath(), ".png"));
    }

    public void cycleAnimationFrames() {
        this.bind();

        for(Tickable var0 : this.animatedTextures) {
            var0.tick();
        }

    }

    @Override
    public void tick() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::cycleAnimationFrames);
        } else {
            this.cycleAnimationFrames();
        }

    }

    public TextureAtlasSprite getSprite(ResourceLocation param0) {
        TextureAtlasSprite var0 = this.texturesByName.get(param0);
        return var0 == null ? this.texturesByName.get(MissingTextureAtlasSprite.getLocation()) : var0;
    }

    public void clearTextureData() {
        for(TextureAtlasSprite var0 : this.texturesByName.values()) {
            var0.close();
        }

        this.texturesByName.clear();
        this.animatedTextures.clear();
    }

    public ResourceLocation location() {
        return this.location;
    }

    public void updateFilter(TextureAtlas.Preparations param0) {
        this.setFilter(false, param0.mipLevel > 0);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Preparations {
        final Set<ResourceLocation> sprites;
        final int width;
        final int height;
        final int mipLevel;
        final List<TextureAtlasSprite> regions;

        public Preparations(Set<ResourceLocation> param0, int param1, int param2, int param3, List<TextureAtlasSprite> param4) {
            this.sprites = param0;
            this.width = param1;
            this.height = param2;
            this.mipLevel = param3;
            this.regions = param4;
        }
    }
}

package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureAtlas extends AbstractTexture implements TickableTextureObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation LOCATION_BLOCKS = new ResourceLocation("textures/atlas/blocks.png");
    public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
    public static final ResourceLocation LOCATION_PAINTINGS = new ResourceLocation("textures/atlas/paintings.png");
    public static final ResourceLocation LOCATION_MOB_EFFECTS = new ResourceLocation("textures/atlas/mob_effects.png");
    private final List<TextureAtlasSprite> animatedTextures = Lists.newArrayList();
    private final Set<ResourceLocation> sprites = Sets.newHashSet();
    private final Map<ResourceLocation, TextureAtlasSprite> texturesByName = Maps.newHashMap();
    private final String path;
    private final int maxSupportedTextureSize;
    private int maxMipLevel;
    private final TextureAtlasSprite missingTextureSprite = MissingTextureAtlasSprite.newInstance();

    public TextureAtlas(String param0) {
        this.path = param0;
        this.maxSupportedTextureSize = Minecraft.maxSupportedTextureSize();
    }

    @Override
    public void load(ResourceManager param0) throws IOException {
    }

    public void reload(TextureAtlas.Preparations param0) {
        this.sprites.clear();
        this.sprites.addAll(param0.sprites);
        LOGGER.info("Created: {}x{} {}-atlas", param0.width, param0.height, this.path);
        TextureUtil.prepareImage(this.getId(), this.maxMipLevel, param0.width, param0.height);
        this.clearTextureData();

        for(TextureAtlasSprite var0 : param0.regions) {
            this.texturesByName.put(var0.getName(), var0);

            try {
                var0.uploadFirstFrame();
            } catch (Throwable var7) {
                CrashReport var2 = CrashReport.forThrowable(var7, "Stitching texture atlas");
                CrashReportCategory var3 = var2.addCategory("Texture being stitched together");
                var3.setDetail("Atlas path", this.path);
                var3.setDetail("Sprite", var0);
                throw new ReportedException(var2);
            }

            if (var0.isAnimation()) {
                this.animatedTextures.add(var0);
            }
        }

    }

    public TextureAtlas.Preparations prepareToStitch(ResourceManager param0, Iterable<ResourceLocation> param1, ProfilerFiller param2) {
        Set<ResourceLocation> var0 = Sets.newHashSet();
        param2.push("preparing");
        param1.forEach(param1x -> {
            if (param1x == null) {
                throw new IllegalArgumentException("Location cannot be null!");
            } else {
                var0.add(param1x);
            }
        });
        int var1 = this.maxSupportedTextureSize;
        Stitcher var2 = new Stitcher(var1, var1, this.maxMipLevel);
        int var3 = Integer.MAX_VALUE;
        int var4 = 1 << this.maxMipLevel;
        param2.popPush("extracting_frames");

        for(TextureAtlasSprite var5 : this.getBasicSpriteInfos(param0, var0)) {
            var3 = Math.min(var3, Math.min(var5.getWidth(), var5.getHeight()));
            int var6 = Math.min(Integer.lowestOneBit(var5.getWidth()), Integer.lowestOneBit(var5.getHeight()));
            if (var6 < var4) {
                LOGGER.warn(
                    "Texture {} with size {}x{} limits mip level from {} to {}",
                    var5.getName(),
                    var5.getWidth(),
                    var5.getHeight(),
                    Mth.log2(var4),
                    Mth.log2(var6)
                );
                var4 = var6;
            }

            var2.registerSprite(var5);
        }

        int var7 = Math.min(var3, var4);
        int var8 = Mth.log2(var7);
        if (var8 < this.maxMipLevel) {
            LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.path, this.maxMipLevel, var8, var7);
            this.maxMipLevel = var8;
        }

        param2.popPush("mipmapping");
        this.missingTextureSprite.applyMipmapping(this.maxMipLevel);
        param2.popPush("register");
        var2.registerSprite(this.missingTextureSprite);
        param2.popPush("stitching");

        try {
            var2.stitch();
        } catch (StitcherException var14) {
            CrashReport var10 = CrashReport.forThrowable(var14, "Stitching");
            CrashReportCategory var11 = var10.addCategory("Stitcher");
            var11.setDetail(
                "Sprites",
                var14.getAllSprites()
                    .stream()
                    .map(param0x -> String.format("%s[%dx%d]", param0x.getName(), param0x.getWidth(), param0x.getHeight()))
                    .collect(Collectors.joining(","))
            );
            var11.setDetail("Max Texture Size", var1);
            throw new ReportedException(var10);
        }

        param2.popPush("loading");
        List<TextureAtlasSprite> var12 = this.getLoadedSprites(param0, var2);
        param2.pop();
        return new TextureAtlas.Preparations(var0, var2.getWidth(), var2.getHeight(), var12);
    }

    private Collection<TextureAtlasSprite> getBasicSpriteInfos(ResourceManager param0, Set<ResourceLocation> param1) {
        List<CompletableFuture<?>> var0 = new ArrayList<>();
        ConcurrentLinkedQueue<TextureAtlasSprite> var1 = new ConcurrentLinkedQueue<>();

        for(ResourceLocation var2 : param1) {
            if (!this.missingTextureSprite.getName().equals(var2)) {
                var0.add(CompletableFuture.runAsync(() -> {
                    ResourceLocation var0x = this.getResourceLocation(var2);

                    TextureAtlasSprite var4;
                    try (Resource var1x = param0.getResource(var0x)) {
                        PngInfo var2x = new PngInfo(var1x.toString(), var1x.getInputStream());
                        AnimationMetadataSection var3x = var1x.getMetadata(AnimationMetadataSection.SERIALIZER);
                        var4 = new TextureAtlasSprite(var2, var2x, var3x);
                    } catch (RuntimeException var21) {
                        LOGGER.error("Unable to parse metadata from {} : {}", var0x, var21);
                        return;
                    } catch (IOException var22) {
                        LOGGER.error("Using missing texture, unable to load {} : {}", var0x, var22);
                        return;
                    }

                    var1.add(var4);
                }, Util.backgroundExecutor()));
            }
        }

        CompletableFuture.allOf(var0.toArray(new CompletableFuture[0])).join();
        return var1;
    }

    private List<TextureAtlasSprite> getLoadedSprites(ResourceManager param0, Stitcher param1) {
        ConcurrentLinkedQueue<TextureAtlasSprite> var0 = new ConcurrentLinkedQueue<>();
        List<CompletableFuture<?>> var1 = new ArrayList<>();

        for(TextureAtlasSprite var2 : param1.gatherSprites()) {
            if (var2 == this.missingTextureSprite) {
                var0.add(var2);
            } else {
                var1.add(CompletableFuture.runAsync(() -> {
                    if (this.load(param0, var2)) {
                        var0.add(var2);
                    }

                }, Util.backgroundExecutor()));
            }
        }

        CompletableFuture.allOf(var1.toArray(new CompletableFuture[0])).join();
        return new ArrayList<>(var0);
    }

    private boolean load(ResourceManager param0, TextureAtlasSprite param1) {
        ResourceLocation var0 = this.getResourceLocation(param1.getName());
        Resource var1 = null;

        label52: {
            boolean var16;
            try {
                var1 = param0.getResource(var0);
                param1.loadData(var1, this.maxMipLevel + 1);
                break label52;
            } catch (RuntimeException var13) {
                LOGGER.error("Unable to parse metadata from {}", var0, var13);
                var16 = false;
            } catch (IOException var14) {
                LOGGER.error("Using missing texture, unable to load {}", var0, var14);
                return false;
            } finally {
                IOUtils.closeQuietly((Closeable)var1);
            }

            return var16;
        }

        try {
            param1.applyMipmapping(this.maxMipLevel);
            return true;
        } catch (Throwable var12) {
            CrashReport var5 = CrashReport.forThrowable(var12, "Applying mipmap");
            CrashReportCategory var6 = var5.addCategory("Sprite being mipmapped");
            var6.setDetail("Sprite name", () -> param1.getName().toString());
            var6.setDetail("Sprite size", () -> param1.getWidth() + " x " + param1.getHeight());
            var6.setDetail("Sprite frames", () -> param1.getFrameCount() + " frames");
            var6.setDetail("Mipmap levels", this.maxMipLevel);
            throw new ReportedException(var5);
        }
    }

    private ResourceLocation getResourceLocation(ResourceLocation param0) {
        return new ResourceLocation(param0.getNamespace(), String.format("%s/%s%s", this.path, param0.getPath(), ".png"));
    }

    public TextureAtlasSprite getTexture(String param0) {
        return this.getSprite(new ResourceLocation(param0));
    }

    public void cycleAnimationFrames() {
        this.bind();

        for(TextureAtlasSprite var0 : this.animatedTextures) {
            var0.cycleFrames();
        }

    }

    @Override
    public void tick() {
        this.cycleAnimationFrames();
    }

    public void setMaxMipLevel(int param0) {
        this.maxMipLevel = param0;
    }

    public TextureAtlasSprite getSprite(ResourceLocation param0) {
        TextureAtlasSprite var0 = this.texturesByName.get(param0);
        return var0 == null ? this.missingTextureSprite : var0;
    }

    public void clearTextureData() {
        for(TextureAtlasSprite var0 : this.texturesByName.values()) {
            var0.wipeFrameData();
        }

        this.texturesByName.clear();
        this.animatedTextures.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Preparations {
        final Set<ResourceLocation> sprites;
        final int width;
        final int height;
        final List<TextureAtlasSprite> regions;

        public Preparations(Set<ResourceLocation> param0, int param1, int param2, List<TextureAtlasSprite> param3) {
            this.sprites = param0;
            this.width = param1;
            this.height = param2;
            this.regions = param3;
        }
    }
}

package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureAtlas extends AbstractTexture implements Tickable {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Deprecated
    public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
    @Deprecated
    public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
    private List<SpriteContents> sprites = List.of();
    private List<TextureAtlasSprite.Ticker> animatedTextures = List.of();
    private Map<ResourceLocation, TextureAtlasSprite> texturesByName = Map.of();
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;

    public TextureAtlas(ResourceLocation param0) {
        this.location = param0;
        this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
    }

    @Override
    public void load(ResourceManager param0) {
    }

    public void upload(SpriteLoader.Preparations param0) {
        LOGGER.info("Created: {}x{}x{} {}-atlas", param0.width(), param0.height(), param0.mipLevel(), this.location);
        TextureUtil.prepareImage(this.getId(), param0.mipLevel(), param0.width(), param0.height());
        this.clearTextureData();
        this.texturesByName = Map.copyOf(param0.regions());
        List<SpriteContents> var0 = new ArrayList<>();
        List<TextureAtlasSprite.Ticker> var1 = new ArrayList<>();

        for(TextureAtlasSprite var2 : param0.regions().values()) {
            var0.add(var2.contents());

            try {
                var2.uploadFirstFrame();
            } catch (Throwable var9) {
                CrashReport var4 = CrashReport.forThrowable(var9, "Stitching texture atlas");
                CrashReportCategory var5 = var4.addCategory("Texture being stitched together");
                var5.setDetail("Atlas path", this.location);
                var5.setDetail("Sprite", var2);
                throw new ReportedException(var4);
            }

            TextureAtlasSprite.Ticker var6 = var2.createTicker();
            if (var6 != null) {
                var1.add(var6);
            }
        }

        this.sprites = List.copyOf(var0);
        this.animatedTextures = List.copyOf(var1);
    }

    private void dumpContents(int param0, int param1, int param2) {
        String var0 = this.location.toDebugFileName();
        TextureUtil.writeAsPNG(var0, this.getId(), param0, param1, param2);
        dumpSpriteNames(var0, this.texturesByName);
    }

    private static void dumpSpriteNames(String param0, Map<ResourceLocation, TextureAtlasSprite> param1) {
        Path var0 = Path.of(param0 + ".txt");

        try (Writer var1 = Files.newBufferedWriter(var0)) {
            for(Entry<ResourceLocation, TextureAtlasSprite> var2 : param1.entrySet().stream().sorted(Entry.comparingByKey()).toList()) {
                TextureAtlasSprite var3 = var2.getValue();
                var1.write(
                    String.format("%s\tx=%d\ty=%d\tw=%d\th=%d%n", var2.getKey(), var3.getX(), var3.getY(), var3.contents().width(), var3.contents().height())
                );
            }
        } catch (IOException var9) {
            LOGGER.warn("Failed to write file {}", var0, var9);
        }

    }

    public void cycleAnimationFrames() {
        this.bind();

        for(TextureAtlasSprite.Ticker var0 : this.animatedTextures) {
            var0.tickAndUpload();
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
        this.sprites.forEach(SpriteContents::close);
        this.animatedTextures.forEach(TextureAtlasSprite.Ticker::close);
        this.sprites = List.of();
        this.animatedTextures = List.of();
        this.texturesByName = Map.of();
    }

    public ResourceLocation location() {
        return this.location;
    }

    public int maxSupportedTextureSize() {
        return this.maxSupportedTextureSize;
    }

    public void updateFilter(SpriteLoader.Preparations param0) {
        this.setFilter(false, param0.mipLevel() > 0);
    }
}

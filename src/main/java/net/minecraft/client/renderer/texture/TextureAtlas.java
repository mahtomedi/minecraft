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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
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
public class TextureAtlas extends AbstractTexture implements Dumpable, Tickable {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Deprecated
    public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
    @Deprecated
    public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
    private List<SpriteContents> sprites = List.of();
    private List<TextureAtlasSprite.Ticker> animatedTextures = List.of();
    private Map<ResourceLocation, TextureAtlasSprite> texturesByName = Map.of();
    @Nullable
    private TextureAtlasSprite missingSprite;
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;
    private int width;
    private int height;
    private int mipLevel;

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
        this.width = param0.width();
        this.height = param0.height();
        this.mipLevel = param0.mipLevel();
        this.clearTextureData();
        this.texturesByName = Map.copyOf(param0.regions());
        this.missingSprite = this.texturesByName.get(MissingTextureAtlasSprite.getLocation());
        if (this.missingSprite == null) {
            throw new IllegalStateException("Atlas '" + this.location + "' (" + this.texturesByName.size() + " sprites) has no missing texture sprite");
        } else {
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
    }

    @Override
    public void dumpContents(ResourceLocation param0, Path param1) throws IOException {
        String var0 = param0.toDebugFileName();
        TextureUtil.writeAsPNG(param1, var0, this.getId(), this.mipLevel, this.width, this.height);
        dumpSpriteNames(param1, var0, this.texturesByName);
    }

    private static void dumpSpriteNames(Path param0, String param1, Map<ResourceLocation, TextureAtlasSprite> param2) {
        Path var0 = param0.resolve(param1 + ".txt");

        try (Writer var1 = Files.newBufferedWriter(var0)) {
            for(Entry<ResourceLocation, TextureAtlasSprite> var2 : param2.entrySet().stream().sorted(Entry.comparingByKey()).toList()) {
                TextureAtlasSprite var3 = var2.getValue();
                var1.write(
                    String.format(
                        Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", var2.getKey(), var3.getX(), var3.getY(), var3.contents().width(), var3.contents().height()
                    )
                );
            }
        } catch (IOException var10) {
            LOGGER.warn("Failed to write file {}", var0, var10);
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
        TextureAtlasSprite var0 = this.texturesByName.getOrDefault(param0, this.missingSprite);
        if (var0 == null) {
            throw new IllegalStateException("Tried to lookup sprite, but atlas is not initialized");
        } else {
            return var0;
        }
    }

    public void clearTextureData() {
        this.sprites.forEach(SpriteContents::close);
        this.animatedTextures.forEach(TextureAtlasSprite.Ticker::close);
        this.sprites = List.of();
        this.animatedTextures = List.of();
        this.texturesByName = Map.of();
        this.missingSprite = null;
    }

    public ResourceLocation location() {
        return this.location;
    }

    public int maxSupportedTextureSize() {
        return this.maxSupportedTextureSize;
    }

    int getWidth() {
        return this.width;
    }

    int getHeight() {
        return this.height;
    }

    public void updateFilter(SpriteLoader.Preparations param0) {
        this.setFilter(false, param0.mipLevel() > 0);
    }
}

package net.minecraft.client.renderer.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureManager implements Tickable, PreparableReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
    private final Map<ResourceLocation, AbstractTexture> byPath = Maps.newHashMap();
    private final Set<Tickable> tickableTextures = Sets.newHashSet();
    private final Map<String, Integer> prefixRegister = Maps.newHashMap();
    private final ResourceManager resourceManager;

    public TextureManager(ResourceManager param0) {
        this.resourceManager = param0;
    }

    public void bind(ResourceLocation param0) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._bind(param0));
        } else {
            this._bind(param0);
        }

    }

    private void _bind(ResourceLocation param0) {
        AbstractTexture var0 = this.byPath.get(param0);
        if (var0 == null) {
            var0 = new SimpleTexture(param0);
            this.register(param0, var0);
        }

        var0.bind();
    }

    public boolean register(ResourceLocation param0, AbstractTexture param1) {
        boolean var0 = true;

        try {
            param1.load(this.resourceManager);
        } catch (IOException var8) {
            if (param0 != INTENTIONAL_MISSING_TEXTURE) {
                LOGGER.warn("Failed to load texture: {}", param0, var8);
            }

            param1 = MissingTextureAtlasSprite.getTexture();
            this.byPath.put(param0, param1);
            var0 = false;
        } catch (Throwable var9) {
            CrashReport var3 = CrashReport.forThrowable(var9, "Registering texture");
            CrashReportCategory var4 = var3.addCategory("Resource location being registered");
            var4.setDetail("Resource location", param0);
            var4.setDetail("Texture object class", () -> param1.getClass().getName());
            throw new ReportedException(var3);
        }

        this.byPath.put(param0, param1);
        if (var0 && param1 instanceof Tickable) {
            this.tickableTextures.add((Tickable)param1);
        }

        return var0;
    }

    @Nullable
    public AbstractTexture getTexture(ResourceLocation param0) {
        return this.byPath.get(param0);
    }

    public ResourceLocation register(String param0, DynamicTexture param1) {
        Integer var0 = this.prefixRegister.get(param0);
        if (var0 == null) {
            var0 = 1;
        } else {
            var0 = var0 + 1;
        }

        this.prefixRegister.put(param0, var0);
        ResourceLocation var1 = new ResourceLocation(String.format("dynamic/%s_%d", param0, var0));
        this.register(var1, param1);
        return var1;
    }

    public CompletableFuture<Void> preload(ResourceLocation param0, Executor param1) {
        if (!this.byPath.containsKey(param0)) {
            PreloadedTexture var0 = new PreloadedTexture(this.resourceManager, param0, param1);
            this.byPath.put(param0, var0);
            return var0.getFuture().thenRunAsync(() -> this.register(param0, var0), TextureManager::execute);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static void execute(Runnable param0x) {
        Minecraft.getInstance().execute(() -> RenderSystem.recordRenderCall(param0x::run));
    }

    @Override
    public void tick() {
        for(Tickable var0 : this.tickableTextures) {
            var0.tick();
        }

    }

    public void release(ResourceLocation param0) {
        AbstractTexture var0 = this.getTexture(param0);
        if (var0 != null) {
            TextureUtil.releaseTextureId(var0.getId());
        }

    }

    @Override
    public CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier param0,
        ResourceManager param1,
        ProfilerFiller param2,
        ProfilerFiller param3,
        Executor param4,
        Executor param5
    ) {
        return CompletableFuture.allOf(TitleScreen.preloadResources(this, param4), this.preload(AbstractWidget.WIDGETS_LOCATION, param4))
            .thenCompose(param0::wait)
            .thenAcceptAsync(param2x -> {
                MissingTextureAtlasSprite.getTexture();
                RealmsMainScreen.updateTeaserImages(this.resourceManager);
                Iterator<Entry<ResourceLocation, AbstractTexture>> var0 = this.byPath.entrySet().iterator();
    
                while(var0.hasNext()) {
                    Entry<ResourceLocation, AbstractTexture> var1x = var0.next();
                    ResourceLocation var2x = (ResourceLocation)var1x.getKey();
                    AbstractTexture var3x = (AbstractTexture)var1x.getValue();
                    if (var3x == MissingTextureAtlasSprite.getTexture() && !var2x.equals(MissingTextureAtlasSprite.getLocation())) {
                        var0.remove();
                    } else {
                        var3x.reset(this, param1, var2x, param5);
                    }
                }
    
            }, param0x -> RenderSystem.recordRenderCall(param0x::run));
    }
}

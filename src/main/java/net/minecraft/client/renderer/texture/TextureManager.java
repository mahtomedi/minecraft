package net.minecraft.client.renderer.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureManager implements PreparableReloadListener, Tickable, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
    private final Map<ResourceLocation, AbstractTexture> byPath = Maps.newHashMap();
    private final Set<Tickable> tickableTextures = Sets.newHashSet();
    private final Map<String, Integer> prefixRegister = Maps.newHashMap();
    private final ResourceManager resourceManager;

    public TextureManager(ResourceManager param0) {
        this.resourceManager = param0;
    }

    public void bindForSetup(ResourceLocation param0) {
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

    public void register(ResourceLocation param0, AbstractTexture param1) {
        param1 = this.loadTexture(param0, param1);
        AbstractTexture var0 = this.byPath.put(param0, param1);
        if (var0 != param1) {
            if (var0 != null && var0 != MissingTextureAtlasSprite.getTexture()) {
                this.tickableTextures.remove(var0);
                this.safeClose(param0, var0);
            }

            if (param1 instanceof Tickable) {
                this.tickableTextures.add((Tickable)param1);
            }
        }

    }

    private void safeClose(ResourceLocation param0, AbstractTexture param1) {
        if (param1 != MissingTextureAtlasSprite.getTexture()) {
            try {
                param1.close();
            } catch (Exception var4) {
                LOGGER.warn("Failed to close texture {}", param0, var4);
            }
        }

        param1.releaseId();
    }

    private AbstractTexture loadTexture(ResourceLocation param0, AbstractTexture param1) {
        try {
            param1.load(this.resourceManager);
            return param1;
        } catch (IOException var6) {
            if (param0 != INTENTIONAL_MISSING_TEXTURE) {
                LOGGER.warn("Failed to load texture: {}", param0, var6);
            }

            return MissingTextureAtlasSprite.getTexture();
        } catch (Throwable var7) {
            CrashReport var2 = CrashReport.forThrowable(var7, "Registering texture");
            CrashReportCategory var3 = var2.addCategory("Resource location being registered");
            var3.setDetail("Resource location", param0);
            var3.setDetail("Texture object class", () -> param1.getClass().getName());
            throw new ReportedException(var2);
        }
    }

    public AbstractTexture getTexture(ResourceLocation param0) {
        AbstractTexture var0 = this.byPath.get(param0);
        if (var0 == null) {
            var0 = new SimpleTexture(param0);
            this.register(param0, var0);
        }

        return var0;
    }

    public AbstractTexture getTexture(ResourceLocation param0, AbstractTexture param1) {
        return this.byPath.getOrDefault(param0, param1);
    }

    public ResourceLocation register(String param0, DynamicTexture param1) {
        Integer var0 = this.prefixRegister.get(param0);
        if (var0 == null) {
            var0 = 1;
        } else {
            var0 = var0 + 1;
        }

        this.prefixRegister.put(param0, var0);
        ResourceLocation var1 = new ResourceLocation(String.format(Locale.ROOT, "dynamic/%s_%d", param0, var0));
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
        AbstractTexture var0 = this.getTexture(param0, MissingTextureAtlasSprite.getTexture());
        if (var0 != MissingTextureAtlasSprite.getTexture()) {
            TextureUtil.releaseTextureId(var0.getId());
        }

    }

    @Override
    public void close() {
        this.byPath.forEach(this::safeClose);
        this.byPath.clear();
        this.tickableTextures.clear();
        this.prefixRegister.clear();
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

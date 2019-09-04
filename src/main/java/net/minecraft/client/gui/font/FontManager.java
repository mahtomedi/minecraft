package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FontManager implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ResourceLocation, Font> fonts = Maps.newHashMap();
    private final Set<GlyphProvider> providers = Sets.newHashSet();
    private final TextureManager textureManager;
    private boolean forceUnicode;
    private final PreparableReloadListener reloadListener = new SimplePreparableReloadListener<Map<ResourceLocation, List<GlyphProvider>>>() {
        protected Map<ResourceLocation, List<GlyphProvider>> prepare(ResourceManager param0, ProfilerFiller param1) {
            param1.startTick();
            Gson var0 = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            Map<ResourceLocation, List<GlyphProvider>> var1 = Maps.newHashMap();

            for(ResourceLocation var2 : param0.listResources("font", param0x -> param0x.endsWith(".json"))) {
                String var3 = var2.getPath();
                ResourceLocation var4 = new ResourceLocation(var2.getNamespace(), var3.substring("font/".length(), var3.length() - ".json".length()));
                List<GlyphProvider> var5 = var1.computeIfAbsent(var4, param0x -> Lists.newArrayList(new AllMissingGlyphProvider()));
                param1.push(var4::toString);

                try {
                    for(Resource var6 : param0.getResources(var2)) {
                        param1.push(var6::getSourceName);

                        try (
                            InputStream var7 = var6.getInputStream();
                            Reader var8 = new BufferedReader(new InputStreamReader(var7, StandardCharsets.UTF_8));
                        ) {
                            param1.push("reading");
                            JsonArray var9 = GsonHelper.getAsJsonArray(GsonHelper.fromJson(var0, var8, JsonObject.class), "providers");
                            param1.popPush("parsing");

                            for(int var10 = var9.size() - 1; var10 >= 0; --var10) {
                                JsonObject var11 = GsonHelper.convertToJsonObject(var9.get(var10), "providers[" + var10 + "]");

                                try {
                                    String var12 = GsonHelper.getAsString(var11, "type");
                                    GlyphProviderBuilderType var13 = GlyphProviderBuilderType.byName(var12);
                                    if (!FontManager.this.forceUnicode
                                        || var13 == GlyphProviderBuilderType.LEGACY_UNICODE
                                        || !var4.equals(Minecraft.DEFAULT_FONT)) {
                                        param1.push(var12);
                                        var5.add(var13.create(var11).create(param0));
                                        param1.pop();
                                    }
                                } catch (RuntimeException var48) {
                                    FontManager.LOGGER
                                        .warn(
                                            "Unable to read definition '{}' in fonts.json in resourcepack: '{}': {}",
                                            var4,
                                            var6.getSourceName(),
                                            var48.getMessage()
                                        );
                                }
                            }

                            param1.pop();
                        } catch (RuntimeException var53) {
                            FontManager.LOGGER
                                .warn("Unable to load font '{}' in fonts.json in resourcepack: '{}': {}", var4, var6.getSourceName(), var53.getMessage());
                        }

                        param1.pop();
                    }
                } catch (IOException var54) {
                    FontManager.LOGGER.warn("Unable to load font '{}' in fonts.json: {}", var4, var54.getMessage());
                }

                param1.push("caching");

                for(char var17 = 0; var17 < '\uffff'; ++var17) {
                    if (var17 != ' ') {
                        for(GlyphProvider var18 : Lists.reverse(var5)) {
                            if (var18.getGlyph(var17) != null) {
                                break;
                            }
                        }
                    }
                }

                param1.pop();
                param1.pop();
            }

            param1.endTick();
            return var1;
        }

        protected void apply(Map<ResourceLocation, List<GlyphProvider>> param0, ResourceManager param1, ProfilerFiller param2) {
            param2.startTick();
            param2.push("reloading");
            Stream.concat(FontManager.this.fonts.keySet().stream(), param0.keySet().stream())
                .distinct()
                .forEach(
                    param1x -> {
                        List<GlyphProvider> var0 = param0.getOrDefault(param1x, Collections.emptyList());
                        Collections.reverse(var0);
                        FontManager.this.fonts
                            .computeIfAbsent(
                                param1x, param0x -> new Font(FontManager.this.textureManager, new FontSet(FontManager.this.textureManager, param0x))
                            )
                            .reload(var0);
                    }
                );
            param0.values().forEach(FontManager.this.providers::addAll);
            param2.pop();
            param2.endTick();
        }

        @Override
        public String getName() {
            return "FontManager";
        }
    };

    public FontManager(TextureManager param0, boolean param1) {
        this.textureManager = param0;
        this.forceUnicode = param1;
    }

    @Nullable
    public Font get(ResourceLocation param0) {
        return this.fonts.computeIfAbsent(param0, param0x -> {
            Font var0 = new Font(this.textureManager, new FontSet(this.textureManager, param0x));
            var0.reload(Lists.newArrayList(new AllMissingGlyphProvider()));
            return var0;
        });
    }

    public void setForceUnicode(boolean param0, Executor param1, Executor param2) {
        if (param0 != this.forceUnicode) {
            this.forceUnicode = param0;
            ResourceManager var0 = Minecraft.getInstance().getResourceManager();
            PreparableReloadListener.PreparationBarrier var1 = new PreparableReloadListener.PreparationBarrier() {
                @Override
                public <T> CompletableFuture<T> wait(T param0) {
                    return CompletableFuture.completedFuture(param0);
                }
            };
            this.reloadListener.reload(var1, var0, InactiveProfiler.INACTIVE, InactiveProfiler.INACTIVE, param1, param2);
        }
    }

    public PreparableReloadListener getReloadListener() {
        return this.reloadListener;
    }

    @Override
    public void close() {
        this.fonts.values().forEach(Font::close);
        this.providers.forEach(GlyphProvider::close);
    }
}

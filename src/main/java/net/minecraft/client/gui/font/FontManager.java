package net.minecraft.client.gui.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FontManager implements AutoCloseable {
    static final Logger LOGGER = LogManager.getLogger();
    private static final String FONTS_PATH = "fonts.json";
    public static final ResourceLocation MISSING_FONT = new ResourceLocation("minecraft", "missing");
    private final FontSet missingFontSet;
    final Map<ResourceLocation, FontSet> fontSets = Maps.newHashMap();
    final TextureManager textureManager;
    private Map<ResourceLocation, ResourceLocation> renames = ImmutableMap.of();
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
                                    param1.push(var12);
                                    GlyphProvider var14 = var13.create(var11).create(param0);
                                    if (var14 != null) {
                                        var5.add(var14);
                                    }

                                    param1.pop();
                                } catch (RuntimeException var22) {
                                    FontManager.LOGGER
                                        .warn(
                                            "Unable to read definition '{}' in {} in resourcepack: '{}': {}",
                                            var4,
                                            "fonts.json",
                                            var6.getSourceName(),
                                            var22.getMessage()
                                        );
                                }
                            }

                            param1.pop();
                        } catch (RuntimeException var25) {
                            FontManager.LOGGER
                                .warn("Unable to load font '{}' in {} in resourcepack: '{}': {}", var4, "fonts.json", var6.getSourceName(), var25.getMessage());
                        }

                        param1.pop();
                    }
                } catch (IOException var26) {
                    FontManager.LOGGER.warn("Unable to load font '{}' in {}: {}", var4, "fonts.json", var26.getMessage());
                }

                param1.push("caching");
                IntSet var18 = new IntOpenHashSet();

                for(GlyphProvider var19 : var5) {
                    var18.addAll(var19.getSupportedGlyphs());
                }

                var18.forEach(param1x -> {
                    if (param1x != 32) {
                        for(GlyphProvider var0x : Lists.reverse(var5)) {
                            if (var0x.getGlyph(param1x) != null) {
                                break;
                            }
                        }

                    }
                });
                param1.pop();
                param1.pop();
            }

            param1.endTick();
            return var1;
        }

        protected void apply(Map<ResourceLocation, List<GlyphProvider>> param0, ResourceManager param1, ProfilerFiller param2) {
            param2.startTick();
            param2.push("closing");
            FontManager.this.fontSets.values().forEach(FontSet::close);
            FontManager.this.fontSets.clear();
            param2.popPush("reloading");
            param0.forEach((param0x, param1x) -> {
                FontSet var0 = new FontSet(FontManager.this.textureManager, param0x);
                var0.reload(Lists.reverse(param1x));
                FontManager.this.fontSets.put(param0x, var0);
            });
            param2.pop();
            param2.endTick();
        }

        @Override
        public String getName() {
            return "FontManager";
        }
    };

    public FontManager(TextureManager param0) {
        this.textureManager = param0;
        this.missingFontSet = Util.make(new FontSet(param0, MISSING_FONT), param0x -> param0x.reload(Lists.newArrayList(new AllMissingGlyphProvider())));
    }

    public void setRenames(Map<ResourceLocation, ResourceLocation> param0) {
        this.renames = param0;
    }

    public Font createFont() {
        return new Font(param0 -> this.fontSets.getOrDefault(this.renames.getOrDefault(param0, param0), this.missingFontSet));
    }

    public PreparableReloadListener getReloadListener() {
        return this.reloadListener;
    }

    @Override
    public void close() {
        this.fontSets.values().forEach(FontSet::close);
        this.missingFontSet.close();
    }
}

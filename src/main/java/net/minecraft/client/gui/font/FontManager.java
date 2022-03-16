package net.minecraft.client.gui.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceThunk;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FontManager implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
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

            for(Entry<ResourceLocation, List<ResourceThunk>> var2 : param0.listResourceStacks("font", param0x -> param0x.getPath().endsWith(".json"))
                .entrySet()) {
                ResourceLocation var3 = var2.getKey();
                String var4 = var3.getPath();
                ResourceLocation var5 = new ResourceLocation(var3.getNamespace(), var4.substring("font/".length(), var4.length() - ".json".length()));
                List<GlyphProvider> var6 = var1.computeIfAbsent(var5, param0x -> Lists.newArrayList(new AllMissingGlyphProvider()));
                param1.push(var5::toString);

                for(ResourceThunk var7 : var2.getValue()) {
                    param1.push(var7.sourcePackId());

                    try (
                        Resource var8 = var7.open();
                        InputStream var9 = var8.getInputStream();
                        Reader var10 = new BufferedReader(new InputStreamReader(var9, StandardCharsets.UTF_8));
                    ) {
                        try {
                            param1.push("reading");
                            JsonArray var11 = GsonHelper.getAsJsonArray(GsonHelper.fromJson(var0, var10, JsonObject.class), "providers");
                            param1.popPush("parsing");

                            for(int var12 = var11.size() - 1; var12 >= 0; --var12) {
                                JsonObject var13 = GsonHelper.convertToJsonObject(var11.get(var12), "providers[" + var12 + "]");
                                String var14 = GsonHelper.getAsString(var13, "type");
                                GlyphProviderBuilderType var15 = GlyphProviderBuilderType.byName(var14);

                                try {
                                    param1.push(var14);
                                    GlyphProvider var16 = var15.create(var13).create(param0);
                                    if (var16 != null) {
                                        var6.add(var16);
                                    }
                                } finally {
                                    param1.pop();
                                }
                            }
                        } finally {
                            param1.pop();
                        }
                    } catch (Exception var50) {
                        FontManager.LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", var5, "fonts.json", var7.sourcePackId(), var50);
                    }

                    param1.pop();
                }

                param1.push("caching");
                IntSet var18 = new IntOpenHashSet();

                for(GlyphProvider var19 : var6) {
                    var18.addAll(var19.getSupportedGlyphs());
                }

                var18.forEach(param1x -> {
                    if (param1x != 32) {
                        for(GlyphProvider var0x : Lists.reverse(var6)) {
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

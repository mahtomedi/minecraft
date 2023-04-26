package net.minecraft.client.gui.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FontManager implements PreparableReloadListener, AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String FONTS_PATH = "fonts.json";
    public static final ResourceLocation MISSING_FONT = new ResourceLocation("minecraft", "missing");
    private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final FontSet missingFontSet;
    private final List<GlyphProvider> providersToClose = new ArrayList<>();
    private final Map<ResourceLocation, FontSet> fontSets = new HashMap<>();
    private final TextureManager textureManager;
    private Map<ResourceLocation, ResourceLocation> renames = ImmutableMap.of();

    public FontManager(TextureManager param0) {
        this.textureManager = param0;
        this.missingFontSet = Util.make(new FontSet(param0, MISSING_FONT), param0x -> param0x.reload(Lists.newArrayList(new AllMissingGlyphProvider())));
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
        param2.startTick();
        param2.endTick();
        return this.prepare(param1, param4).thenCompose(param0::wait).thenAcceptAsync(param1x -> this.apply(param1x, param3), param5);
    }

    private CompletableFuture<FontManager.Preparation> prepare(ResourceManager param0, Executor param1) {
        List<CompletableFuture<FontManager.UnresolvedBuilderBundle>> var0 = new ArrayList<>();

        for(Entry<ResourceLocation, List<Resource>> var1 : FONT_DEFINITIONS.listMatchingResourceStacks(param0).entrySet()) {
            ResourceLocation var2 = FONT_DEFINITIONS.fileToId(var1.getKey());
            var0.add(CompletableFuture.supplyAsync(() -> {
                List<Pair<FontManager.BuilderId, GlyphProviderBuilder>> var0x = loadResourceStack(var1.getValue(), var2);
                FontManager.UnresolvedBuilderBundle var1x = new FontManager.UnresolvedBuilderBundle(var2);

                for(Pair<FontManager.BuilderId, GlyphProviderBuilder> var2x : var0x) {
                    FontManager.BuilderId var3x = var2x.getFirst();
                    var2x.getSecond().build().ifLeft(param4 -> {
                        CompletableFuture<Optional<GlyphProvider>> var0xx = this.safeLoad(var3x, param4, param0, param1);
                        var1x.add(var3x, var0xx);
                    }).ifRight(param2x -> var1x.add(var3x, param2x));
                }

                return var1x;
            }, param1));
        }

        return Util.sequence(var0)
            .thenCompose(
                param1x -> {
                    List<CompletableFuture<Optional<GlyphProvider>>> var0x = param1x.stream()
                        .flatMap(FontManager.UnresolvedBuilderBundle::listBuilders)
                        .collect(Collectors.toCollection(ArrayList::new));
                    GlyphProvider var1x = new AllMissingGlyphProvider();
                    var0x.add(CompletableFuture.completedFuture(Optional.of(var1x)));
                    return Util.sequence(var0x)
                        .thenCompose(
                            param3 -> {
                                Map<ResourceLocation, List<GlyphProvider>> var0xx = this.resolveProviders(param1x);
                                CompletableFuture<?>[] var1xx = var0xx.values()
                                    .stream()
                                    .map(param2x -> CompletableFuture.runAsync(() -> this.finalizeProviderLoading(param2x, var1x), param1))
                                    .toArray(param0x -> new CompletableFuture[param0x]);
                                return CompletableFuture.allOf(var1xx).thenApply(param2x -> {
                                    List<GlyphProvider> var0xxx = param3.stream().flatMap(Optional::stream).toList();
                                    return new FontManager.Preparation(var0xx, var0xxx);
                                });
                            }
                        );
                }
            );
    }

    private CompletableFuture<Optional<GlyphProvider>> safeLoad(
        FontManager.BuilderId param0, GlyphProviderBuilder.Loader param1, ResourceManager param2, Executor param3
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.of(param1.load(param2));
            } catch (Exception var4x) {
                LOGGER.warn("Failed to load builder {}, rejecting", param0, var4x);
                return Optional.empty();
            }
        }, param3);
    }

    private Map<ResourceLocation, List<GlyphProvider>> resolveProviders(List<FontManager.UnresolvedBuilderBundle> param0) {
        Map<ResourceLocation, List<GlyphProvider>> var0 = new HashMap<>();
        DependencySorter<ResourceLocation, FontManager.UnresolvedBuilderBundle> var1 = new DependencySorter<>();
        param0.forEach(param1 -> var1.addEntry(param1.fontId, param1));
        var1.orderByDependencies((param1, param2) -> param2.resolve(var0::get).ifPresent(param2x -> var0.put(param1, param2x)));
        return var0;
    }

    private void finalizeProviderLoading(List<GlyphProvider> param0, GlyphProvider param1) {
        param0.add(0, param1);
        IntSet var0 = new IntOpenHashSet();

        for(GlyphProvider var1 : param0) {
            var0.addAll(var1.getSupportedGlyphs());
        }

        var0.forEach(param1x -> {
            if (param1x != 32) {
                for(GlyphProvider var0x : Lists.reverse(param0)) {
                    if (var0x.getGlyph(param1x) != null) {
                        break;
                    }
                }

            }
        });
    }

    private void apply(FontManager.Preparation param0, ProfilerFiller param1) {
        param1.startTick();
        param1.push("closing");
        this.fontSets.values().forEach(FontSet::close);
        this.fontSets.clear();
        this.providersToClose.forEach(GlyphProvider::close);
        this.providersToClose.clear();
        param1.popPush("reloading");
        param0.providers().forEach((param0x, param1x) -> {
            FontSet var0 = new FontSet(this.textureManager, param0x);
            var0.reload(Lists.reverse(param1x));
            this.fontSets.put(param0x, var0);
        });
        this.providersToClose.addAll(param0.allProviders);
        param1.pop();
        param1.endTick();
        if (!this.fontSets.containsKey(this.getActualId(Minecraft.DEFAULT_FONT))) {
            throw new IllegalStateException("Default font failed to load");
        }
    }

    private static List<Pair<FontManager.BuilderId, GlyphProviderBuilder>> loadResourceStack(List<Resource> param0, ResourceLocation param1) {
        List<Pair<FontManager.BuilderId, GlyphProviderBuilder>> var0 = new ArrayList<>();

        for(Resource var1 : param0) {
            try (Reader var2 = var1.openAsReader()) {
                JsonArray var3 = GsonHelper.getAsJsonArray(GsonHelper.fromJson(GSON, var2, JsonObject.class), "providers");

                for(int var4 = var3.size() - 1; var4 >= 0; --var4) {
                    JsonObject var5 = GsonHelper.convertToJsonObject(var3.get(var4), "providers[" + var4 + "]");
                    String var6 = GsonHelper.getAsString(var5, "type");
                    GlyphProviderBuilderType var7 = GlyphProviderBuilderType.byName(var6);
                    FontManager.BuilderId var8 = new FontManager.BuilderId(param1, var1.sourcePackId(), var4);
                    var0.add(Pair.of(var8, var7.create(var5)));
                }
            } catch (Exception var14) {
                LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", param1, "fonts.json", var1.sourcePackId(), var14);
            }
        }

        return var0;
    }

    public void setRenames(Map<ResourceLocation, ResourceLocation> param0) {
        this.renames = param0;
    }

    private ResourceLocation getActualId(ResourceLocation param0) {
        return this.renames.getOrDefault(param0, param0);
    }

    public Font createFont() {
        return new Font(param0 -> this.fontSets.getOrDefault(this.getActualId(param0), this.missingFontSet), false);
    }

    public Font createFontFilterFishy() {
        return new Font(param0 -> this.fontSets.getOrDefault(this.getActualId(param0), this.missingFontSet), true);
    }

    @Override
    public void close() {
        this.fontSets.values().forEach(FontSet::close);
        this.providersToClose.forEach(GlyphProvider::close);
        this.missingFontSet.close();
    }

    @OnlyIn(Dist.CLIENT)
    static record BuilderId(ResourceLocation fontId, String pack, int index) {
        @Override
        public String toString() {
            return "(" + this.fontId + ": builder #" + this.index + " from pack " + this.pack + ")";
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record BuilderResult(FontManager.BuilderId id, Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> result) {
        public Optional<List<GlyphProvider>> resolve(Function<ResourceLocation, List<GlyphProvider>> param0) {
            return this.result
                .map(
                    param0x -> param0x.join().map(List::of),
                    param1 -> {
                        List<GlyphProvider> var0 = param0.apply(param1);
                        if (var0 == null) {
                            FontManager.LOGGER
                                .warn(
                                    "Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle",
                                    param1,
                                    this.id
                                );
                            return Optional.empty();
                        } else {
                            return Optional.of(var0);
                        }
                    }
                );
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record Preparation(Map<ResourceLocation, List<GlyphProvider>> providers, List<GlyphProvider> allProviders) {
    }

    @OnlyIn(Dist.CLIENT)
    static record UnresolvedBuilderBundle(ResourceLocation fontId, List<FontManager.BuilderResult> builders, Set<ResourceLocation> dependencies)
        implements DependencySorter.Entry<ResourceLocation> {
        public UnresolvedBuilderBundle(ResourceLocation param0) {
            this(param0, new ArrayList<>(), new HashSet<>());
        }

        public void add(FontManager.BuilderId param0, GlyphProviderBuilder.Reference param1) {
            this.builders.add(new FontManager.BuilderResult(param0, Either.right(param1.id())));
            this.dependencies.add(param1.id());
        }

        public void add(FontManager.BuilderId param0, CompletableFuture<Optional<GlyphProvider>> param1) {
            this.builders.add(new FontManager.BuilderResult(param0, Either.left(param1)));
        }

        private Stream<CompletableFuture<Optional<GlyphProvider>>> listBuilders() {
            return this.builders.stream().flatMap(param0 -> param0.result.left().stream());
        }

        public Optional<List<GlyphProvider>> resolve(Function<ResourceLocation, List<GlyphProvider>> param0) {
            List<GlyphProvider> var0 = new ArrayList<>();

            for(FontManager.BuilderResult var1 : this.builders) {
                Optional<List<GlyphProvider>> var2 = var1.resolve(param0);
                if (!var2.isPresent()) {
                    return Optional.empty();
                }

                var0.addAll(var2.get());
            }

            return Optional.of(var0);
        }

        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> param0) {
            this.dependencies.forEach(param0);
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> param0) {
        }
    }
}

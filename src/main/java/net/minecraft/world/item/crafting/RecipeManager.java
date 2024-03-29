package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> recipes = ImmutableMap.of();
    private Map<ResourceLocation, RecipeHolder<?>> byName = ImmutableMap.of();
    private boolean hasErrors;

    public RecipeManager() {
        super(GSON, "recipes");
    }

    protected void apply(Map<ResourceLocation, JsonElement> param0, ResourceManager param1, ProfilerFiller param2) {
        this.hasErrors = false;
        Map<RecipeType<?>, Builder<ResourceLocation, RecipeHolder<?>>> var0 = Maps.newHashMap();
        Builder<ResourceLocation, RecipeHolder<?>> var1 = ImmutableMap.builder();

        for(Entry<ResourceLocation, JsonElement> var2 : param0.entrySet()) {
            ResourceLocation var3 = var2.getKey();

            try {
                RecipeHolder<?> var4 = fromJson(var3, GsonHelper.convertToJsonObject(var2.getValue(), "top element"));
                var0.computeIfAbsent(var4.value().getType(), param0x -> ImmutableMap.builder()).put(var3, var4);
                var1.put(var3, var4);
            } catch (IllegalArgumentException | JsonParseException var10) {
                LOGGER.error("Parsing error loading recipe {}", var3, var10);
            }
        }

        this.recipes = var0.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, param0x -> param0x.getValue().build()));
        this.byName = var1.build();
        LOGGER.info("Loaded {} recipes", var0.size());
    }

    public boolean hadErrorsLoading() {
        return this.hasErrors;
    }

    public <C extends Container, T extends Recipe<C>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> param0, C param1, Level param2) {
        return this.byType(param0).values().stream().filter(param2x -> param2x.value().matches(param1, param2)).findFirst();
    }

    public <C extends Container, T extends Recipe<C>> Optional<Pair<ResourceLocation, RecipeHolder<T>>> getRecipeFor(
        RecipeType<T> param0, C param1, Level param2, @Nullable ResourceLocation param3
    ) {
        Map<ResourceLocation, RecipeHolder<T>> var0 = this.byType(param0);
        if (param3 != null) {
            RecipeHolder<T> var1 = var0.get(param3);
            if (var1 != null && var1.value().matches(param1, param2)) {
                return Optional.of(Pair.of(param3, var1));
            }
        }

        return var0.entrySet()
            .stream()
            .filter(param2x -> param2x.getValue().value().matches(param1, param2))
            .findFirst()
            .map(param0x -> Pair.of(param0x.getKey(), param0x.getValue()));
    }

    public <C extends Container, T extends Recipe<C>> List<RecipeHolder<T>> getAllRecipesFor(RecipeType<T> param0) {
        return List.copyOf(this.byType(param0).values());
    }

    public <C extends Container, T extends Recipe<C>> List<RecipeHolder<T>> getRecipesFor(RecipeType<T> param0, C param1, Level param2) {
        return this.byType(param0)
            .values()
            .stream()
            .filter(param2x -> param2x.value().matches(param1, param2))
            .sorted(Comparator.comparing(param1x -> param1x.value().getResultItem(param2.registryAccess()).getDescriptionId()))
            .collect(Collectors.toList());
    }

    private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, RecipeHolder<T>> byType(RecipeType<T> param0) {
        return this.recipes.getOrDefault(param0, Collections.emptyMap());
    }

    public <C extends Container, T extends Recipe<C>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> param0, C param1, Level param2) {
        Optional<RecipeHolder<T>> var0 = this.getRecipeFor(param0, param1, param2);
        if (var0.isPresent()) {
            return var0.get().value().getRemainingItems(param1);
        } else {
            NonNullList<ItemStack> var1 = NonNullList.withSize(param1.getContainerSize(), ItemStack.EMPTY);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                var1.set(var2, param1.getItem(var2));
            }

            return var1;
        }
    }

    public Optional<RecipeHolder<?>> byKey(ResourceLocation param0) {
        return Optional.ofNullable(this.byName.get(param0));
    }

    public Collection<RecipeHolder<?>> getRecipes() {
        return this.recipes.values().stream().flatMap(param0 -> param0.values().stream()).collect(Collectors.toSet());
    }

    public Stream<ResourceLocation> getRecipeIds() {
        return this.recipes.values().stream().flatMap(param0 -> param0.keySet().stream());
    }

    protected static RecipeHolder<?> fromJson(ResourceLocation param0, JsonObject param1) {
        Recipe<?> var0 = Util.getOrThrow(Recipe.CODEC.parse(JsonOps.INSTANCE, param1), JsonParseException::new);
        return new RecipeHolder<>(param0, var0);
    }

    public void replaceRecipes(Iterable<RecipeHolder<?>> param0) {
        this.hasErrors = false;
        Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> var0 = Maps.newHashMap();
        Builder<ResourceLocation, RecipeHolder<?>> var1 = ImmutableMap.builder();
        param0.forEach(param2 -> {
            Map<ResourceLocation, RecipeHolder<?>> var0x = var0.computeIfAbsent(param2.value().getType(), param0x -> Maps.newHashMap());
            ResourceLocation var1x = param2.id();
            RecipeHolder<?> var2x = var0x.put(var1x, param2);
            var1.put(var1x, param2);
            if (var2x != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + var1x);
            }
        });
        this.recipes = ImmutableMap.copyOf(var0);
        this.byName = var1.build();
    }

    public static <C extends Container, T extends Recipe<C>> RecipeManager.CachedCheck<C, T> createCheck(final RecipeType<T> param0) {
        return new RecipeManager.CachedCheck<C, T>() {
            @Nullable
            private ResourceLocation lastRecipe;

            @Override
            public Optional<RecipeHolder<T>> getRecipeFor(C param0x, Level param1) {
                RecipeManager var0 = param1.getRecipeManager();
                Optional<Pair<ResourceLocation, RecipeHolder<T>>> var1 = var0.getRecipeFor(param0, param0, param1, this.lastRecipe);
                if (var1.isPresent()) {
                    Pair<ResourceLocation, RecipeHolder<T>> var2 = var1.get();
                    this.lastRecipe = var2.getFirst();
                    return Optional.of(var2.getSecond());
                } else {
                    return Optional.empty();
                }
            }
        };
    }

    public interface CachedCheck<C extends Container, T extends Recipe<C>> {
        Optional<RecipeHolder<T>> getRecipeFor(C var1, Level var2);
    }
}

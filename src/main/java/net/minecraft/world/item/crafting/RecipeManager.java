package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = ImmutableMap.of();
    private Map<ResourceLocation, Recipe<?>> byName = ImmutableMap.of();
    private boolean hasErrors;

    public RecipeManager() {
        super(GSON, "recipes");
    }

    protected void apply(Map<ResourceLocation, JsonElement> param0, ResourceManager param1, ProfilerFiller param2) {
        this.hasErrors = false;
        Map<RecipeType<?>, Builder<ResourceLocation, Recipe<?>>> var0 = Maps.newHashMap();
        Builder<ResourceLocation, Recipe<?>> var1 = ImmutableMap.builder();

        for(Entry<ResourceLocation, JsonElement> var2 : param0.entrySet()) {
            ResourceLocation var3 = var2.getKey();

            try {
                Recipe<?> var4 = fromJson(var3, GsonHelper.convertToJsonObject(var2.getValue(), "top element"));
                var0.computeIfAbsent(var4.getType(), param0x -> ImmutableMap.builder()).put(var3, var4);
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

    public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> param0, C param1, Level param2) {
        return this.byType(param0).values().stream().flatMap(param3 -> Util.toStream(param0.tryMatch(param3, param2, param1))).findFirst();
    }

    public <C extends Container, T extends Recipe<C>> List<T> getAllRecipesFor(RecipeType<T> param0) {
        return this.byType(param0).values().stream().map((Function<? super Recipe, ? extends Recipe>)(param0x -> param0x)).collect(Collectors.toList());
    }

    public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> param0, C param1, Level param2) {
        return this.byType(param0)
            .values()
            .stream()
            .flatMap(param3 -> Util.toStream(param0.tryMatch(param3, param2, param1)))
            .sorted(Comparator.comparing(param0x -> param0x.getResultItem().getDescriptionId()))
            .collect(Collectors.toList());
    }

    private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> byType(RecipeType<T> param0) {
        return this.recipes.getOrDefault(param0, Collections.emptyMap());
    }

    public <C extends Container, T extends Recipe<C>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> param0, C param1, Level param2) {
        Optional<T> var0 = this.getRecipeFor(param0, param1, param2);
        if (var0.isPresent()) {
            return var0.get().getRemainingItems(param1);
        } else {
            NonNullList<ItemStack> var1 = NonNullList.withSize(param1.getContainerSize(), ItemStack.EMPTY);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                var1.set(var2, param1.getItem(var2));
            }

            return var1;
        }
    }

    public Optional<? extends Recipe<?>> byKey(ResourceLocation param0) {
        return Optional.ofNullable(this.byName.get(param0));
    }

    public Collection<Recipe<?>> getRecipes() {
        return this.recipes.values().stream().flatMap(param0 -> param0.values().stream()).collect(Collectors.toSet());
    }

    public Stream<ResourceLocation> getRecipeIds() {
        return this.recipes.values().stream().flatMap(param0 -> param0.keySet().stream());
    }

    public static Recipe<?> fromJson(ResourceLocation param0, JsonObject param1) {
        String var0 = GsonHelper.getAsString(param1, "type");
        return Registry.RECIPE_SERIALIZER
            .getOptional(new ResourceLocation(var0))
            .orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported recipe type '" + var0 + "'"))
            .fromJson(param0, param1);
    }

    public void replaceRecipes(Iterable<Recipe<?>> param0) {
        this.hasErrors = false;
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> var0 = Maps.newHashMap();
        Builder<ResourceLocation, Recipe<?>> var1 = ImmutableMap.builder();
        param0.forEach(param2 -> {
            Map<ResourceLocation, Recipe<?>> var0x = var0.computeIfAbsent(param2.getType(), param0x -> Maps.newHashMap());
            ResourceLocation var1x = param2.getId();
            Recipe<?> var2x = var0x.put(var1x, param2);
            var1.put(var1x, param2);
            if (var2x != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + var1x);
            }
        });
        this.recipes = ImmutableMap.copyOf(var0);
        this.byName = var1.build();
    }
}

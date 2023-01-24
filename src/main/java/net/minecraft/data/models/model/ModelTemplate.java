package net.minecraft.data.models.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class ModelTemplate {
    private final Optional<ResourceLocation> model;
    private final Set<TextureSlot> requiredSlots;
    private final Optional<String> suffix;

    public ModelTemplate(Optional<ResourceLocation> param0, Optional<String> param1, TextureSlot... param2) {
        this.model = param0;
        this.suffix = param1;
        this.requiredSlots = ImmutableSet.copyOf(param2);
    }

    public ResourceLocation create(Block param0, TextureMapping param1, BiConsumer<ResourceLocation, Supplier<JsonElement>> param2) {
        return this.create(ModelLocationUtils.getModelLocation(param0, this.suffix.orElse("")), param1, param2);
    }

    public ResourceLocation createWithSuffix(Block param0, String param1, TextureMapping param2, BiConsumer<ResourceLocation, Supplier<JsonElement>> param3) {
        return this.create(ModelLocationUtils.getModelLocation(param0, param1 + (String)this.suffix.orElse("")), param2, param3);
    }

    public ResourceLocation createWithOverride(Block param0, String param1, TextureMapping param2, BiConsumer<ResourceLocation, Supplier<JsonElement>> param3) {
        return this.create(ModelLocationUtils.getModelLocation(param0, param1), param2, param3);
    }

    public ResourceLocation create(ResourceLocation param0, TextureMapping param1, BiConsumer<ResourceLocation, Supplier<JsonElement>> param2) {
        return this.create(param0, param1, param2, this::createBaseTemplate);
    }

    public ResourceLocation create(
        ResourceLocation param0, TextureMapping param1, BiConsumer<ResourceLocation, Supplier<JsonElement>> param2, ModelTemplate.JsonFactory param3
    ) {
        Map<TextureSlot, ResourceLocation> var0 = this.createMap(param1);
        param2.accept(param0, () -> param3.create(param0, var0));
        return param0;
    }

    public JsonObject createBaseTemplate(ResourceLocation param0x, Map<TextureSlot, ResourceLocation> param1x) {
        JsonObject var0 = new JsonObject();
        this.model.ifPresent(param1xx -> var0.addProperty("parent", param1xx.toString()));
        if (!param1x.isEmpty()) {
            JsonObject var1 = new JsonObject();
            param1x.forEach((param1xx, param2x) -> var1.addProperty(param1xx.getId(), param2x.toString()));
            var0.add("textures", var1);
        }

        return var0;
    }

    private Map<TextureSlot, ResourceLocation> createMap(TextureMapping param0) {
        return Streams.concat(this.requiredSlots.stream(), param0.getForced()).collect(ImmutableMap.toImmutableMap(Function.identity(), param0::get));
    }

    public interface JsonFactory {
        JsonObject create(ResourceLocation var1, Map<TextureSlot, ResourceLocation> var2);
    }
}

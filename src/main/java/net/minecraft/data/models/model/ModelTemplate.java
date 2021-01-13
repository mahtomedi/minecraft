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
    private Optional<String> suffix;

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
        Map<TextureSlot, ResourceLocation> var0 = this.createMap(param1);
        param2.accept(param0, () -> {
            JsonObject var0x = new JsonObject();
            this.model.ifPresent(param1x -> var0x.addProperty("parent", param1x.toString()));
            if (!var0.isEmpty()) {
                JsonObject var1x = new JsonObject();
                var0.forEach((param1x, param2x) -> var1x.addProperty(param1x.getId(), param2x.toString()));
                var0x.add("textures", var1x);
            }

            return var0x;
        });
        return param0;
    }

    private Map<TextureSlot, ResourceLocation> createMap(TextureMapping param0) {
        return Streams.concat(this.requiredSlots.stream(), param0.getForced()).collect(ImmutableMap.toImmutableMap(Function.identity(), param0::get));
    }
}

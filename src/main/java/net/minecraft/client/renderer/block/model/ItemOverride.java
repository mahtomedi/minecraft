package net.minecraft.client.renderer.block.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemOverride {
    private final ResourceLocation model;
    private final List<ItemOverride.Predicate> predicates;

    public ItemOverride(ResourceLocation param0, List<ItemOverride.Predicate> param1) {
        this.model = param0;
        this.predicates = ImmutableList.copyOf(param1);
    }

    public ResourceLocation getModel() {
        return this.model;
    }

    public Stream<ItemOverride.Predicate> getPredicates() {
        return this.predicates.stream();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<ItemOverride> {
        protected Deserializer() {
        }

        public ItemOverride deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(var0, "model"));
            List<ItemOverride.Predicate> var2 = this.getPredicates(var0);
            return new ItemOverride(var1, var2);
        }

        protected List<ItemOverride.Predicate> getPredicates(JsonObject param0) {
            Map<ResourceLocation, Float> var0 = Maps.newLinkedHashMap();
            JsonObject var1 = GsonHelper.getAsJsonObject(param0, "predicate");

            for(Entry<String, JsonElement> var2 : var1.entrySet()) {
                var0.put(new ResourceLocation(var2.getKey()), GsonHelper.convertToFloat(var2.getValue(), var2.getKey()));
            }

            return var0.entrySet()
                .stream()
                .map(param0x -> new ItemOverride.Predicate(param0x.getKey(), param0x.getValue()))
                .collect(ImmutableList.toImmutableList());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Predicate {
        private final ResourceLocation property;
        private final float value;

        public Predicate(ResourceLocation param0, float param1) {
            this.property = param0;
            this.value = param1;
        }

        public ResourceLocation getProperty() {
            return this.property;
        }

        public float getValue() {
            return this.value;
        }
    }
}

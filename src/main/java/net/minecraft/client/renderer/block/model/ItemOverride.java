package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemPropertyFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemOverride {
    private final ResourceLocation model;
    private final Map<ResourceLocation, Float> predicates;

    public ItemOverride(ResourceLocation param0, Map<ResourceLocation, Float> param1) {
        this.model = param0;
        this.predicates = param1;
    }

    public ResourceLocation getModel() {
        return this.model;
    }

    boolean test(ItemStack param0, @Nullable Level param1, @Nullable LivingEntity param2) {
        Item var0 = param0.getItem();

        for(Entry<ResourceLocation, Float> var1 : this.predicates.entrySet()) {
            ItemPropertyFunction var2 = var0.getProperty(var1.getKey());
            if (var2 == null || var2.call(param0, param1, param2) < var1.getValue()) {
                return false;
            }
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<ItemOverride> {
        protected Deserializer() {
        }

        public ItemOverride deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(var0, "model"));
            Map<ResourceLocation, Float> var2 = this.getPredicates(var0);
            return new ItemOverride(var1, var2);
        }

        protected Map<ResourceLocation, Float> getPredicates(JsonObject param0) {
            Map<ResourceLocation, Float> var0 = Maps.newLinkedHashMap();
            JsonObject var1 = GsonHelper.getAsJsonObject(param0, "predicate");

            for(Entry<String, JsonElement> var2 : var1.entrySet()) {
                var0.put(new ResourceLocation(var2.getKey()), GsonHelper.convertToFloat(var2.getValue(), var2.getKey()));
            }

            return var0;
        }
    }
}

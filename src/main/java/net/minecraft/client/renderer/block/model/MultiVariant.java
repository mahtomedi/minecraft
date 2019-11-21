package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiVariant implements UnbakedModel {
    private final List<Variant> variants;

    public MultiVariant(List<Variant> param0) {
        this.variants = param0;
    }

    public List<Variant> getVariants() {
        return this.variants;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 instanceof MultiVariant) {
            MultiVariant var0 = (MultiVariant)param0;
            return this.variants.equals(var0.variants);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.variants.hashCode();
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.getVariants().stream().map(Variant::getModelLocation).collect(Collectors.toSet());
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> param0, Set<Pair<String, String>> param1) {
        return this.getVariants()
            .stream()
            .map(Variant::getModelLocation)
            .distinct()
            .flatMap(param2 -> param0.apply(param2).getMaterials(param0, param1).stream())
            .collect(Collectors.toSet());
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBakery param0, Function<Material, TextureAtlasSprite> param1, ModelState param2, ResourceLocation param3) {
        if (this.getVariants().isEmpty()) {
            return null;
        } else {
            WeightedBakedModel.Builder var0 = new WeightedBakedModel.Builder();

            for(Variant var1 : this.getVariants()) {
                BakedModel var2 = param0.bake(var1.getModelLocation(), var1);
                var0.add(var2, var1.getWeight());
            }

            return var0.build();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<MultiVariant> {
        public MultiVariant deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            List<Variant> var0 = Lists.newArrayList();
            if (param0.isJsonArray()) {
                JsonArray var1 = param0.getAsJsonArray();
                if (var1.size() == 0) {
                    throw new JsonParseException("Empty variant array");
                }

                for(JsonElement var2 : var1) {
                    var0.add(param2.deserialize(var2, Variant.class));
                }
            } else {
                var0.add(param2.deserialize(param0, Variant.class));
            }

            return new MultiVariant(var0);
        }
    }
}

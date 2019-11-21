package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiPart implements UnbakedModel {
    private final StateDefinition<Block, BlockState> definition;
    private final List<Selector> selectors;

    public MultiPart(StateDefinition<Block, BlockState> param0, List<Selector> param1) {
        this.definition = param0;
        this.selectors = param1;
    }

    public List<Selector> getSelectors() {
        return this.selectors;
    }

    public Set<MultiVariant> getMultiVariants() {
        Set<MultiVariant> var0 = Sets.newHashSet();

        for(Selector var1 : this.selectors) {
            var0.add(var1.getVariant());
        }

        return var0;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof MultiPart)) {
            return false;
        } else {
            MultiPart var0 = (MultiPart)param0;
            return Objects.equals(this.definition, var0.definition) && Objects.equals(this.selectors, var0.selectors);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.definition, this.selectors);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.getSelectors().stream().flatMap(param0 -> param0.getVariant().getDependencies().stream()).collect(Collectors.toSet());
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> param0, Set<Pair<String, String>> param1) {
        return this.getSelectors().stream().flatMap(param2 -> param2.getVariant().getMaterials(param0, param1).stream()).collect(Collectors.toSet());
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBakery param0, Function<Material, TextureAtlasSprite> param1, ModelState param2, ResourceLocation param3) {
        MultiPartBakedModel.Builder var0 = new MultiPartBakedModel.Builder();

        for(Selector var1 : this.getSelectors()) {
            BakedModel var2 = var1.getVariant().bake(param0, param1, param2, param3);
            if (var2 != null) {
                var0.add(var1.getPredicate(this.definition), var2);
            }
        }

        return var0.build();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<MultiPart> {
        private final BlockModelDefinition.Context context;

        public Deserializer(BlockModelDefinition.Context param0) {
            this.context = param0;
        }

        public MultiPart deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            return new MultiPart(this.context.getDefinition(), this.getSelectors(param2, param0.getAsJsonArray()));
        }

        private List<Selector> getSelectors(JsonDeserializationContext param0, JsonArray param1) {
            List<Selector> var0 = Lists.newArrayList();

            for(JsonElement var1 : param1) {
                var0.add(param0.deserialize(var1, Selector.class));
            }

            return var0;
        }
    }
}

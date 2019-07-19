package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockModelDefinition {
    private final Map<String, MultiVariant> variants = Maps.newLinkedHashMap();
    private MultiPart multiPart;

    public static BlockModelDefinition fromStream(BlockModelDefinition.Context param0, Reader param1) {
        return GsonHelper.fromJson(param0.gson, param1, BlockModelDefinition.class);
    }

    public BlockModelDefinition(Map<String, MultiVariant> param0, MultiPart param1) {
        this.multiPart = param1;
        this.variants.putAll(param0);
    }

    public BlockModelDefinition(List<BlockModelDefinition> param0) {
        BlockModelDefinition var0 = null;

        for(BlockModelDefinition var1 : param0) {
            if (var1.isMultiPart()) {
                this.variants.clear();
                var0 = var1;
            }

            this.variants.putAll(var1.variants);
        }

        if (var0 != null) {
            this.multiPart = var0.multiPart;
        }

    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof BlockModelDefinition) {
                BlockModelDefinition var0 = (BlockModelDefinition)param0;
                if (this.variants.equals(var0.variants)) {
                    return this.isMultiPart() ? this.multiPart.equals(var0.multiPart) : !var0.isMultiPart();
                }
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * this.variants.hashCode() + (this.isMultiPart() ? this.multiPart.hashCode() : 0);
    }

    public Map<String, MultiVariant> getVariants() {
        return this.variants;
    }

    public boolean isMultiPart() {
        return this.multiPart != null;
    }

    public MultiPart getMultiPart() {
        return this.multiPart;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Context {
        protected final Gson gson = new GsonBuilder()
            .registerTypeAdapter(BlockModelDefinition.class, new BlockModelDefinition.Deserializer())
            .registerTypeAdapter(Variant.class, new Variant.Deserializer())
            .registerTypeAdapter(MultiVariant.class, new MultiVariant.Deserializer())
            .registerTypeAdapter(MultiPart.class, new MultiPart.Deserializer(this))
            .registerTypeAdapter(Selector.class, new Selector.Deserializer())
            .create();
        private StateDefinition<Block, BlockState> definition;

        public StateDefinition<Block, BlockState> getDefinition() {
            return this.definition;
        }

        public void setDefinition(StateDefinition<Block, BlockState> param0) {
            this.definition = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<BlockModelDefinition> {
        public BlockModelDefinition deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            Map<String, MultiVariant> var1 = this.getVariants(param2, var0);
            MultiPart var2 = this.getMultiPart(param2, var0);
            if (!var1.isEmpty() || var2 != null && !var2.getMultiVariants().isEmpty()) {
                return new BlockModelDefinition(var1, var2);
            } else {
                throw new JsonParseException("Neither 'variants' nor 'multipart' found");
            }
        }

        protected Map<String, MultiVariant> getVariants(JsonDeserializationContext param0, JsonObject param1) {
            Map<String, MultiVariant> var0 = Maps.newHashMap();
            if (param1.has("variants")) {
                JsonObject var1 = GsonHelper.getAsJsonObject(param1, "variants");

                for(Entry<String, JsonElement> var2 : var1.entrySet()) {
                    var0.put(var2.getKey(), param0.deserialize(var2.getValue(), MultiVariant.class));
                }
            }

            return var0;
        }

        @Nullable
        protected MultiPart getMultiPart(JsonDeserializationContext param0, JsonObject param1) {
            if (!param1.has("multipart")) {
                return null;
            } else {
                JsonArray var0 = GsonHelper.getAsJsonArray(param1, "multipart");
                return param0.deserialize(var0, MultiPart.class);
            }
        }
    }
}

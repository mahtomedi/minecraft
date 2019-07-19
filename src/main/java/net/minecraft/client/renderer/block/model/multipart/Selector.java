package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Selector {
    private final Condition condition;
    private final MultiVariant variant;

    public Selector(Condition param0, MultiVariant param1) {
        if (param0 == null) {
            throw new IllegalArgumentException("Missing condition for selector");
        } else if (param1 == null) {
            throw new IllegalArgumentException("Missing variant for selector");
        } else {
            this.condition = param0;
            this.variant = param1;
        }
    }

    public MultiVariant getVariant() {
        return this.variant;
    }

    public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> param0) {
        return this.condition.getPredicate(param0);
    }

    @Override
    public boolean equals(Object param0) {
        return this == param0;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<Selector> {
        public Selector deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            return new Selector(this.getSelector(var0), param2.deserialize(var0.get("apply"), MultiVariant.class));
        }

        private Condition getSelector(JsonObject param0) {
            return param0.has("when") ? getCondition(GsonHelper.getAsJsonObject(param0, "when")) : Condition.TRUE;
        }

        @VisibleForTesting
        static Condition getCondition(JsonObject param0) {
            Set<Entry<String, JsonElement>> var0 = param0.entrySet();
            if (var0.isEmpty()) {
                throw new JsonParseException("No elements found in selector");
            } else if (var0.size() == 1) {
                if (param0.has("OR")) {
                    List<Condition> var1 = Streams.stream(GsonHelper.getAsJsonArray(param0, "OR"))
                        .map(param0x -> getCondition(param0x.getAsJsonObject()))
                        .collect(Collectors.toList());
                    return new OrCondition(var1);
                } else if (param0.has("AND")) {
                    List<Condition> var2 = Streams.stream(GsonHelper.getAsJsonArray(param0, "AND"))
                        .map(param0x -> getCondition(param0x.getAsJsonObject()))
                        .collect(Collectors.toList());
                    return new AndCondition(var2);
                } else {
                    return getKeyValueCondition(var0.iterator().next());
                }
            } else {
                return new AndCondition(var0.stream().map(Selector.Deserializer::getKeyValueCondition).collect(Collectors.toList()));
            }
        }

        private static Condition getKeyValueCondition(Entry<String, JsonElement> param0x) {
            return new KeyValueCondition(param0x.getKey(), param0x.getValue().getAsString());
        }
    }
}

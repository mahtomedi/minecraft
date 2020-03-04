package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class MultiVariantGenerator implements BlockStateGenerator {
    private final Block block;
    private final List<Variant> baseVariants;
    private final Set<Property<?>> seenProperties = Sets.newHashSet();
    private final List<PropertyDispatch> declaredPropertySets = Lists.newArrayList();

    private MultiVariantGenerator(Block param0, List<Variant> param1) {
        this.block = param0;
        this.baseVariants = param1;
    }

    public MultiVariantGenerator with(PropertyDispatch param0) {
        param0.getDefinedProperties().forEach(param0x -> {
            if (this.block.getStateDefinition().getProperty(param0x.getName()) != param0x) {
                throw new IllegalStateException("Property " + param0x + " is not defined for block " + this.block);
            } else if (!this.seenProperties.add(param0x)) {
                throw new IllegalStateException("Values of property " + param0x + " already defined for block " + this.block);
            }
        });
        this.declaredPropertySets.add(param0);
        return this;
    }

    public JsonElement get() {
        Stream<Pair<Selector, List<Variant>>> var0 = Stream.of(Pair.of(Selector.empty(), this.baseVariants));

        for(PropertyDispatch var1 : this.declaredPropertySets) {
            Map<Selector, List<Variant>> var2 = var1.getEntries();
            var0 = var0.flatMap(param1 -> var2.entrySet().stream().map(param1x -> {
                    Selector var0x = ((Selector)param1.getFirst()).extend(param1x.getKey());
                    List<Variant> var1x = mergeVariants((List<Variant>)param1.getSecond(), param1x.getValue());
                    return Pair.of(var0x, var1x);
                }));
        }

        Map<String, JsonElement> var3 = new TreeMap<>();
        var0.forEach(param1 -> param1.getFirst().getKey());
        JsonObject var4 = new JsonObject();
        var4.add("variants", Util.make(new JsonObject(), param1 -> var3.forEach(param1::add)));
        return var4;
    }

    private static List<Variant> mergeVariants(List<Variant> param0, List<Variant> param1) {
        Builder<Variant> var0 = ImmutableList.builder();
        param0.forEach(param2 -> param1.forEach(param2x -> var0.add(Variant.merge(param2, param2x))));
        return var0.build();
    }

    @Override
    public Block getBlock() {
        return this.block;
    }

    public static MultiVariantGenerator multiVariant(Block param0) {
        return new MultiVariantGenerator(param0, ImmutableList.of(Variant.variant()));
    }

    public static MultiVariantGenerator multiVariant(Block param0, Variant param1) {
        return new MultiVariantGenerator(param0, ImmutableList.of(param1));
    }

    public static MultiVariantGenerator multiVariant(Block param0, Variant... param1) {
        return new MultiVariantGenerator(param0, ImmutableList.copyOf(param1));
    }
}

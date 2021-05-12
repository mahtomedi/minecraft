package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class MultiPartGenerator implements BlockStateGenerator {
    private final Block block;
    private final List<MultiPartGenerator.Entry> parts = Lists.newArrayList();

    private MultiPartGenerator(Block param0) {
        this.block = param0;
    }

    @Override
    public Block getBlock() {
        return this.block;
    }

    public static MultiPartGenerator multiPart(Block param0) {
        return new MultiPartGenerator(param0);
    }

    public MultiPartGenerator with(List<Variant> param0) {
        this.parts.add(new MultiPartGenerator.Entry(param0));
        return this;
    }

    public MultiPartGenerator with(Variant param0) {
        return this.with(ImmutableList.of(param0));
    }

    public MultiPartGenerator with(Condition param0, List<Variant> param1) {
        this.parts.add(new MultiPartGenerator.ConditionalEntry(param0, param1));
        return this;
    }

    public MultiPartGenerator with(Condition param0, Variant... param1) {
        return this.with(param0, ImmutableList.copyOf(param1));
    }

    public MultiPartGenerator with(Condition param0, Variant param1) {
        return this.with(param0, ImmutableList.of(param1));
    }

    public JsonElement get() {
        StateDefinition<Block, BlockState> var0 = this.block.getStateDefinition();
        this.parts.forEach(param1 -> param1.validate(var0));
        JsonArray var1 = new JsonArray();
        this.parts.stream().map(MultiPartGenerator.Entry::get).forEach(var1::add);
        JsonObject var2 = new JsonObject();
        var2.add("multipart", var1);
        return var2;
    }

    static class ConditionalEntry extends MultiPartGenerator.Entry {
        private final Condition condition;

        ConditionalEntry(Condition param0, List<Variant> param1) {
            super(param1);
            this.condition = param0;
        }

        @Override
        public void validate(StateDefinition<?, ?> param0) {
            this.condition.validate(param0);
        }

        @Override
        public void decorate(JsonObject param0) {
            param0.add("when", this.condition.get());
        }
    }

    static class Entry implements Supplier<JsonElement> {
        private final List<Variant> variants;

        Entry(List<Variant> param0) {
            this.variants = param0;
        }

        public void validate(StateDefinition<?, ?> param0) {
        }

        public void decorate(JsonObject param0) {
        }

        public JsonElement get() {
            JsonObject var0 = new JsonObject();
            this.decorate(var0);
            var0.add("apply", Variant.convertList(this.variants));
            return var0;
        }
    }
}

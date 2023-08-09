package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public class SequenceFunction implements LootItemFunction {
    public static final Codec<SequenceFunction> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(LootItemFunctions.CODEC.listOf().fieldOf("functions").forGetter(param0x -> param0x.functions))
                .apply(param0, SequenceFunction::new)
    );
    public static final Codec<SequenceFunction> INLINE_CODEC = LootItemFunctions.CODEC.listOf().xmap(SequenceFunction::new, param0 -> param0.functions);
    private final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    private SequenceFunction(List<LootItemFunction> param0) {
        this.functions = param0;
        this.compositeFunction = LootItemFunctions.compose(param0);
    }

    public static SequenceFunction of(List<LootItemFunction> param0) {
        return new SequenceFunction(List.copyOf(param0));
    }

    public ItemStack apply(ItemStack param0, LootContext param1) {
        return this.compositeFunction.apply(param0, param1);
    }

    @Override
    public void validate(ValidationContext param0) {
        LootItemFunction.super.validate(param0);

        for(int var0 = 0; var0 < this.functions.size(); ++var0) {
            this.functions.get(var0).validate(param0.forChild(".function[" + var0 + "]"));
        }

    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SEQUENCE;
    }
}

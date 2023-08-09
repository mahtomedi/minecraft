package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetItemCountFunction extends LootItemConditionalFunction {
    public static final Codec<SetItemCountFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        NumberProviders.CODEC.fieldOf("count").forGetter(param0x -> param0x.value),
                        Codec.BOOL.fieldOf("add").orElse(false).forGetter(param0x -> param0x.add)
                    )
                )
                .apply(param0, SetItemCountFunction::new)
    );
    private final NumberProvider value;
    private final boolean add;

    private SetItemCountFunction(List<LootItemCondition> param0, NumberProvider param1, boolean param2) {
        super(param0);
        this.value = param1;
        this.add = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_COUNT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        int var0 = this.add ? param0.getCount() : 0;
        param0.setCount(Mth.clamp(var0 + this.value.getInt(param1), 0, param0.getMaxStackSize()));
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider param0) {
        return simpleBuilder(param1 -> new SetItemCountFunction(param1, param0, false));
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider param0, boolean param1) {
        return simpleBuilder(param2 -> new SetItemCountFunction(param2, param0, param1));
    }
}

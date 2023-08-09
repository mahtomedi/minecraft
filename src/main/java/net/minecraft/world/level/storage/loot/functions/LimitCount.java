package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction {
    public static final Codec<LimitCount> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0).and(IntRange.CODEC.fieldOf("limit").forGetter(param0x -> param0x.limiter)).apply(param0, LimitCount::new)
    );
    private final IntRange limiter;

    private LimitCount(List<LootItemCondition> param0, IntRange param1) {
        super(param0);
        this.limiter = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.LIMIT_COUNT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.limiter.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        int var0 = this.limiter.clamp(param1, param0.getCount());
        param0.setCount(var0);
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> limitCount(IntRange param0) {
        return simpleBuilder(param1 -> new LimitCount(param1, param0));
    }
}

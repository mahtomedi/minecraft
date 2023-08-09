package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetNbtFunction extends LootItemConditionalFunction {
    public static final Codec<SetNbtFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0).and(TagParser.AS_CODEC.fieldOf("tag").forGetter(param0x -> param0x.tag)).apply(param0, SetNbtFunction::new)
    );
    private final CompoundTag tag;

    private SetNbtFunction(List<LootItemCondition> param0, CompoundTag param1) {
        super(param0);
        this.tag = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_NBT;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        param0.getOrCreateTag().merge(this.tag);
        return param0;
    }

    @Deprecated
    public static LootItemConditionalFunction.Builder<?> setTag(CompoundTag param0) {
        return simpleBuilder(param1 -> new SetNbtFunction(param1, param0));
    }
}

package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction extends LootItemConditionalFunction {
    public static final Codec<SetInstrumentFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(TagKey.hashedCodec(Registries.INSTRUMENT).fieldOf("options").forGetter(param0x -> param0x.options))
                .apply(param0, SetInstrumentFunction::new)
    );
    private final TagKey<Instrument> options;

    private SetInstrumentFunction(List<LootItemCondition> param0, TagKey<Instrument> param1) {
        super(param0);
        this.options = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_INSTRUMENT;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        InstrumentItem.setRandom(param0, this.options, param1.getRandom());
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> param0) {
        return simpleBuilder(param1 -> new SetInstrumentFunction(param1, param0));
    }
}

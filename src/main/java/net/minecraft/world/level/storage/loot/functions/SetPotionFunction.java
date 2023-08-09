package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction extends LootItemConditionalFunction {
    public static final Codec<SetPotionFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(BuiltInRegistries.POTION.holderByNameCodec().fieldOf("id").forGetter(param0x -> param0x.potion))
                .apply(param0, SetPotionFunction::new)
    );
    private final Holder<Potion> potion;

    private SetPotionFunction(List<LootItemCondition> param0, Holder<Potion> param1) {
        super(param0);
        this.potion = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_POTION;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        PotionUtils.setPotion(param0, this.potion.value());
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setPotion(Potion param0) {
        return simpleBuilder(param1 -> new SetPotionFunction(param1, param0.builtInRegistryHolder()));
    }
}

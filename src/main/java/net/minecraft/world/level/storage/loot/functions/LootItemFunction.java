package net.minecraft.world.level.storage.loot.functions;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface LootItemFunction extends LootContextUser, BiFunction<ItemStack, LootContext, ItemStack> {
    LootItemFunctionType getType();

    static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> param0, Consumer<ItemStack> param1, LootContext param2) {
        return param3 -> param1.accept(param0.apply(param3, param2));
    }

    public interface Builder {
        LootItemFunction build();
    }
}

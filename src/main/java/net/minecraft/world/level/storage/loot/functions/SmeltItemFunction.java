package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SmeltItemFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();

    SmeltItemFunction(LootItemCondition[] param0) {
        super(param0);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.FURNACE_SMELT;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.isEmpty()) {
            return param0;
        } else {
            Optional<SmeltingRecipe> var0 = param1.getLevel()
                .getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(param0), param1.getLevel());
            if (var0.isPresent()) {
                ItemStack var1 = var0.get().getResultItem(param1.getLevel().registryAccess());
                if (!var1.isEmpty()) {
                    return var1.copyWithCount(param0.getCount());
                }
            }

            LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", param0);
            return param0;
        }
    }

    public static LootItemConditionalFunction.Builder<?> smelted() {
        return simpleBuilder(SmeltItemFunction::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SmeltItemFunction> {
        public SmeltItemFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            return new SmeltItemFunction(param2);
        }
    }
}

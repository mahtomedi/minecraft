package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyExplosionDecay extends LootItemConditionalFunction {
    private ApplyExplosionDecay(LootItemCondition[] param0) {
        super(param0);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.EXPLOSION_DECAY;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        Float var0 = param1.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (var0 != null) {
            Random var1 = param1.getRandom();
            float var2 = 1.0F / var0;
            int var3 = param0.getCount();
            int var4 = 0;

            for(int var5 = 0; var5 < var3; ++var5) {
                if (var1.nextFloat() <= var2) {
                    ++var4;
                }
            }

            param0.setCount(var4);
        }

        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> explosionDecay() {
        return simpleBuilder(ApplyExplosionDecay::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyExplosionDecay> {
        public ApplyExplosionDecay deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            return new ApplyExplosionDecay(param2);
        }
    }
}

package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNameFunction extends LootItemConditionalFunction {
    final CopyNameFunction.NameSource source;

    CopyNameFunction(LootItemCondition[] param0, CopyNameFunction.NameSource param1) {
        super(param0);
        this.source = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_NAME;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.source.param);
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        Object var0 = param1.getParamOrNull(this.source.param);
        if (var0 instanceof Nameable var1 && var1.hasCustomName()) {
            param0.setHoverName(var1.getDisplayName());
        }

        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> copyName(CopyNameFunction.NameSource param0) {
        return simpleBuilder(param1 -> new CopyNameFunction(param1, param0));
    }

    public static enum NameSource {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER),
        BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

        public final String name;
        public final LootContextParam<?> param;

        private NameSource(String param0, LootContextParam<?> param1) {
            this.name = param0;
            this.param = param1;
        }

        public static CopyNameFunction.NameSource getByName(String param0) {
            for(CopyNameFunction.NameSource var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            throw new IllegalArgumentException("Invalid name source " + param0);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNameFunction> {
        public void serialize(JsonObject param0, CopyNameFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("source", param1.source.name);
        }

        public CopyNameFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            CopyNameFunction.NameSource var0 = CopyNameFunction.NameSource.getByName(GsonHelper.getAsString(param0, "source"));
            return new CopyNameFunction(param2, var0);
        }
    }
}

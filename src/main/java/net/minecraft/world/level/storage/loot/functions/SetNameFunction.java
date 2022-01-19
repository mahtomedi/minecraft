package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SetNameFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    final Component name;
    @Nullable
    final LootContext.EntityTarget resolutionContext;

    SetNameFunction(LootItemCondition[] param0, @Nullable Component param1, @Nullable LootContext.EntityTarget param2) {
        super(param0);
        this.name = param1;
        this.resolutionContext = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_NAME;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
    }

    public static UnaryOperator<Component> createResolver(LootContext param0, @Nullable LootContext.EntityTarget param1) {
        if (param1 != null) {
            Entity var0 = param0.getParamOrNull(param1.getParam());
            if (var0 != null) {
                CommandSourceStack var1 = var0.createCommandSourceStack().withPermission(2);
                return param2 -> {
                    try {
                        return ComponentUtils.updateForEntity(var1, param2, var0, 0);
                    } catch (CommandSyntaxException var4) {
                        LOGGER.warn("Failed to resolve text component", (Throwable)var4);
                        return param2;
                    }
                };
            }
        }

        return param0x -> param0x;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (this.name != null) {
            param0.setHoverName(createResolver(param1, this.resolutionContext).apply(this.name));
        }

        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component param0) {
        return simpleBuilder(param1 -> new SetNameFunction(param1, param0, null));
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component param0, LootContext.EntityTarget param1) {
        return simpleBuilder(param2 -> new SetNameFunction(param2, param0, param1));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetNameFunction> {
        public void serialize(JsonObject param0, SetNameFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            if (param1.name != null) {
                param0.add("name", Component.Serializer.toJsonTree(param1.name));
            }

            if (param1.resolutionContext != null) {
                param0.add("entity", param2.serialize(param1.resolutionContext));
            }

        }

        public SetNameFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            Component var0 = Component.Serializer.fromJson(param0.get("name"));
            LootContext.EntityTarget var1 = GsonHelper.getAsObject(param0, "entity", null, param1, LootContext.EntityTarget.class);
            return new SetNameFunction(param2, var0, var1);
        }
    }
}

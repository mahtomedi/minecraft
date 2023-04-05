package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    final ResourceLocation name;

    FunctionReference(LootItemCondition[] param0, ResourceLocation param1) {
        super(param0);
        this.name = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.REFERENCE;
    }

    @Override
    public void validate(ValidationContext param0) {
        LootDataId<LootItemFunction> var0 = new LootDataId<>(LootDataType.MODIFIER, this.name);
        if (param0.hasVisitedElement(var0)) {
            param0.reportProblem("Function " + this.name + " is recursively called");
        } else {
            super.validate(param0);
            param0.resolver()
                .getElementOptional(var0)
                .ifPresentOrElse(
                    param2 -> param2.validate(param0.enterElement(".{" + this.name + "}", var0)),
                    () -> param0.reportProblem("Unknown function table called " + this.name)
                );
        }
    }

    @Override
    protected ItemStack run(ItemStack param0, LootContext param1) {
        LootItemFunction var0 = param1.getResolver().getElement(LootDataType.MODIFIER, this.name);
        if (var0 == null) {
            LOGGER.warn("Unknown function: {}", this.name);
            return param0;
        } else {
            LootContext.VisitedEntry<?> var1 = LootContext.createVisitedEntry(var0);
            if (param1.pushVisitedElement(var1)) {
                ItemStack var5;
                try {
                    var5 = var0.apply(param0, param1);
                } finally {
                    param1.popVisitedElement(var1);
                }

                return var5;
            } else {
                LOGGER.warn("Detected infinite loop in loot tables");
                return param0;
            }
        }
    }

    public static LootItemConditionalFunction.Builder<?> functionReference(ResourceLocation param0) {
        return simpleBuilder(param1 -> new FunctionReference(param1, param0));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<FunctionReference> {
        public void serialize(JsonObject param0, FunctionReference param1, JsonSerializationContext param2) {
            param0.addProperty("name", param1.name.toString());
        }

        public FunctionReference deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "name"));
            return new FunctionReference(param2, var0);
        }
    }
}

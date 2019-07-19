package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetAttributesFunction extends LootItemConditionalFunction {
    private final List<SetAttributesFunction.Modifier> modifiers;

    private SetAttributesFunction(LootItemCondition[] param0, List<SetAttributesFunction.Modifier> param1) {
        super(param0);
        this.modifiers = ImmutableList.copyOf(param1);
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        Random var0 = param1.getRandom();

        for(SetAttributesFunction.Modifier var1 : this.modifiers) {
            UUID var2 = var1.id;
            if (var2 == null) {
                var2 = UUID.randomUUID();
            }

            EquipmentSlot var3 = var1.slots[var0.nextInt(var1.slots.length)];
            param0.addAttributeModifier(var1.attribute, new AttributeModifier(var2, var1.name, (double)var1.amount.getFloat(var0), var1.operation), var3);
        }

        return param0;
    }

    static class Modifier {
        private final String name;
        private final String attribute;
        private final AttributeModifier.Operation operation;
        private final RandomValueBounds amount;
        @Nullable
        private final UUID id;
        private final EquipmentSlot[] slots;

        private Modifier(
            String param0, String param1, AttributeModifier.Operation param2, RandomValueBounds param3, EquipmentSlot[] param4, @Nullable UUID param5
        ) {
            this.name = param0;
            this.attribute = param1;
            this.operation = param2;
            this.amount = param3;
            this.id = param5;
            this.slots = param4;
        }

        public JsonObject serialize(JsonSerializationContext param0) {
            JsonObject var0 = new JsonObject();
            var0.addProperty("name", this.name);
            var0.addProperty("attribute", this.attribute);
            var0.addProperty("operation", operationToString(this.operation));
            var0.add("amount", param0.serialize(this.amount));
            if (this.id != null) {
                var0.addProperty("id", this.id.toString());
            }

            if (this.slots.length == 1) {
                var0.addProperty("slot", this.slots[0].getName());
            } else {
                JsonArray var1 = new JsonArray();

                for(EquipmentSlot var2 : this.slots) {
                    var1.add(new JsonPrimitive(var2.getName()));
                }

                var0.add("slot", var1);
            }

            return var0;
        }

        public static SetAttributesFunction.Modifier deserialize(JsonObject param0, JsonDeserializationContext param1) {
            String var0 = GsonHelper.getAsString(param0, "name");
            String var1 = GsonHelper.getAsString(param0, "attribute");
            AttributeModifier.Operation var2 = operationFromString(GsonHelper.getAsString(param0, "operation"));
            RandomValueBounds var3 = GsonHelper.getAsObject(param0, "amount", param1, RandomValueBounds.class);
            UUID var4 = null;
            EquipmentSlot[] var5;
            if (GsonHelper.isStringValue(param0, "slot")) {
                var5 = new EquipmentSlot[]{EquipmentSlot.byName(GsonHelper.getAsString(param0, "slot"))};
            } else {
                if (!GsonHelper.isArrayNode(param0, "slot")) {
                    throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
                }

                JsonArray var6 = GsonHelper.getAsJsonArray(param0, "slot");
                var5 = new EquipmentSlot[var6.size()];
                int var8 = 0;

                for(JsonElement var9 : var6) {
                    var5[var8++] = EquipmentSlot.byName(GsonHelper.convertToString(var9, "slot"));
                }

                if (var5.length == 0) {
                    throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
                }
            }

            if (param0.has("id")) {
                String var11 = GsonHelper.getAsString(param0, "id");

                try {
                    var4 = UUID.fromString(var11);
                } catch (IllegalArgumentException var121) {
                    throw new JsonSyntaxException("Invalid attribute modifier id '" + var11 + "' (must be UUID format, with dashes)");
                }
            }

            return new SetAttributesFunction.Modifier(var0, var1, var2, var3, var5, var4);
        }

        private static String operationToString(AttributeModifier.Operation param0) {
            switch(param0) {
                case ADDITION:
                    return "addition";
                case MULTIPLY_BASE:
                    return "multiply_base";
                case MULTIPLY_TOTAL:
                    return "multiply_total";
                default:
                    throw new IllegalArgumentException("Unknown operation " + param0);
            }
        }

        private static AttributeModifier.Operation operationFromString(String param0) {
            switch(param0) {
                case "addition":
                    return AttributeModifier.Operation.ADDITION;
                case "multiply_base":
                    return AttributeModifier.Operation.MULTIPLY_BASE;
                case "multiply_total":
                    return AttributeModifier.Operation.MULTIPLY_TOTAL;
                default:
                    throw new JsonSyntaxException("Unknown attribute modifier operation " + param0);
            }
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetAttributesFunction> {
        public Serializer() {
            super(new ResourceLocation("set_attributes"), SetAttributesFunction.class);
        }

        public void serialize(JsonObject param0, SetAttributesFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            JsonArray var0 = new JsonArray();

            for(SetAttributesFunction.Modifier var1 : param1.modifiers) {
                var0.add(var1.serialize(param2));
            }

            param0.add("modifiers", var0);
        }

        public SetAttributesFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            JsonArray var0 = GsonHelper.getAsJsonArray(param0, "modifiers");
            List<SetAttributesFunction.Modifier> var1 = Lists.newArrayListWithExpectedSize(var0.size());

            for(JsonElement var2 : var0) {
                var1.add(SetAttributesFunction.Modifier.deserialize(GsonHelper.convertToJsonObject(var2, "modifier"), param1));
            }

            if (var1.isEmpty()) {
                throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
            } else {
                return new SetAttributesFunction(param2, var1);
            }
        }
    }
}

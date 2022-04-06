package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetAttributesFunction extends LootItemConditionalFunction {
    final List<SetAttributesFunction.Modifier> modifiers;

    SetAttributesFunction(LootItemCondition[] param0, List<SetAttributesFunction.Modifier> param1) {
        super(param0);
        this.modifiers = ImmutableList.copyOf(param1);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_ATTRIBUTES;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.modifiers.stream().flatMap(param0 -> param0.amount.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        RandomSource var0 = param1.getRandom();

        for(SetAttributesFunction.Modifier var1 : this.modifiers) {
            UUID var2 = var1.id;
            if (var2 == null) {
                var2 = UUID.randomUUID();
            }

            EquipmentSlot var3 = Util.getRandom(var1.slots, var0);
            param0.addAttributeModifier(var1.attribute, new AttributeModifier(var2, var1.name, (double)var1.amount.getFloat(param1), var1.operation), var3);
        }

        return param0;
    }

    public static SetAttributesFunction.ModifierBuilder modifier(String param0, Attribute param1, AttributeModifier.Operation param2, NumberProvider param3) {
        return new SetAttributesFunction.ModifierBuilder(param0, param1, param2, param3);
    }

    public static SetAttributesFunction.Builder setAttributes() {
        return new SetAttributesFunction.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetAttributesFunction.Builder> {
        private final List<SetAttributesFunction.Modifier> modifiers = Lists.newArrayList();

        protected SetAttributesFunction.Builder getThis() {
            return this;
        }

        public SetAttributesFunction.Builder withModifier(SetAttributesFunction.ModifierBuilder param0) {
            this.modifiers.add(param0.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetAttributesFunction(this.getConditions(), this.modifiers);
        }
    }

    static class Modifier {
        final String name;
        final Attribute attribute;
        final AttributeModifier.Operation operation;
        final NumberProvider amount;
        @Nullable
        final UUID id;
        final EquipmentSlot[] slots;

        Modifier(String param0, Attribute param1, AttributeModifier.Operation param2, NumberProvider param3, EquipmentSlot[] param4, @Nullable UUID param5) {
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
            var0.addProperty("attribute", Registry.ATTRIBUTE.getKey(this.attribute).toString());
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
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(param0, "attribute"));
            Attribute var2 = Registry.ATTRIBUTE.get(var1);
            if (var2 == null) {
                throw new JsonSyntaxException("Unknown attribute: " + var1);
            } else {
                AttributeModifier.Operation var3 = operationFromString(GsonHelper.getAsString(param0, "operation"));
                NumberProvider var4 = GsonHelper.getAsObject(param0, "amount", param1, NumberProvider.class);
                UUID var5 = null;
                EquipmentSlot[] var6;
                if (GsonHelper.isStringValue(param0, "slot")) {
                    var6 = new EquipmentSlot[]{EquipmentSlot.byName(GsonHelper.getAsString(param0, "slot"))};
                } else {
                    if (!GsonHelper.isArrayNode(param0, "slot")) {
                        throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
                    }

                    JsonArray var7 = GsonHelper.getAsJsonArray(param0, "slot");
                    var6 = new EquipmentSlot[var7.size()];
                    int var9 = 0;

                    for(JsonElement var10 : var7) {
                        var6[var9++] = EquipmentSlot.byName(GsonHelper.convertToString(var10, "slot"));
                    }

                    if (var6.length == 0) {
                        throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
                    }
                }

                if (param0.has("id")) {
                    String var12 = GsonHelper.getAsString(param0, "id");

                    try {
                        var5 = UUID.fromString(var12);
                    } catch (IllegalArgumentException var131) {
                        throw new JsonSyntaxException("Invalid attribute modifier id '" + var12 + "' (must be UUID format, with dashes)");
                    }
                }

                return new SetAttributesFunction.Modifier(var0, var2, var3, var4, var6, var5);
            }
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

    public static class ModifierBuilder {
        private final String name;
        private final Attribute attribute;
        private final AttributeModifier.Operation operation;
        private final NumberProvider amount;
        @Nullable
        private UUID id;
        private final Set<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);

        public ModifierBuilder(String param0, Attribute param1, AttributeModifier.Operation param2, NumberProvider param3) {
            this.name = param0;
            this.attribute = param1;
            this.operation = param2;
            this.amount = param3;
        }

        public SetAttributesFunction.ModifierBuilder forSlot(EquipmentSlot param0) {
            this.slots.add(param0);
            return this;
        }

        public SetAttributesFunction.ModifierBuilder withUuid(UUID param0) {
            this.id = param0;
            return this;
        }

        public SetAttributesFunction.Modifier build() {
            return new SetAttributesFunction.Modifier(this.name, this.attribute, this.operation, this.amount, this.slots.toArray(new EquipmentSlot[0]), this.id);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetAttributesFunction> {
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

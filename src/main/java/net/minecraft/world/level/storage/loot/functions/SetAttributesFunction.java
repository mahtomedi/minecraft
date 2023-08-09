package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetAttributesFunction extends LootItemConditionalFunction {
    public static final Codec<SetAttributesFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(ExtraCodecs.nonEmptyList(SetAttributesFunction.Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(param0x -> param0x.modifiers))
                .apply(param0, SetAttributesFunction::new)
    );
    private final List<SetAttributesFunction.Modifier> modifiers;

    SetAttributesFunction(List<LootItemCondition> param0, List<SetAttributesFunction.Modifier> param1) {
        super(param0);
        this.modifiers = List.copyOf(param1);
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
            UUID var2 = var1.id.orElseGet(UUID::randomUUID);
            EquipmentSlot var3 = Util.getRandom(var1.slots, var0);
            param0.addAttributeModifier(
                var1.attribute.value(), new AttributeModifier(var2, var1.name, (double)var1.amount.getFloat(param1), var1.operation), var3
            );
        }

        return param0;
    }

    public static SetAttributesFunction.ModifierBuilder modifier(
        String param0, Holder<Attribute> param1, AttributeModifier.Operation param2, NumberProvider param3
    ) {
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

    static record Modifier(
        String name, Holder<Attribute> attribute, AttributeModifier.Operation operation, NumberProvider amount, List<EquipmentSlot> slots, Optional<UUID> id
    ) {
        private static final Codec<List<EquipmentSlot>> SLOTS_CODEC = ExtraCodecs.nonEmptyList(
            Codec.either(EquipmentSlot.CODEC, EquipmentSlot.CODEC.listOf())
                .xmap(param0 -> param0.map(List::of, Function.identity()), param0 -> param0.size() == 1 ? Either.left(param0.get(0)) : Either.right(param0))
        );
        public static final Codec<SetAttributesFunction.Modifier> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.STRING.fieldOf("name").forGetter(SetAttributesFunction.Modifier::name),
                        BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(SetAttributesFunction.Modifier::attribute),
                        AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(SetAttributesFunction.Modifier::operation),
                        NumberProviders.CODEC.fieldOf("amount").forGetter(SetAttributesFunction.Modifier::amount),
                        SLOTS_CODEC.fieldOf("slot").forGetter(SetAttributesFunction.Modifier::slots),
                        ExtraCodecs.strictOptionalField(UUIDUtil.STRING_CODEC, "id").forGetter(SetAttributesFunction.Modifier::id)
                    )
                    .apply(param0, SetAttributesFunction.Modifier::new)
        );
    }

    public static class ModifierBuilder {
        private final String name;
        private final Holder<Attribute> attribute;
        private final AttributeModifier.Operation operation;
        private final NumberProvider amount;
        private Optional<UUID> id = Optional.empty();
        private final Set<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);

        public ModifierBuilder(String param0, Holder<Attribute> param1, AttributeModifier.Operation param2, NumberProvider param3) {
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
            this.id = Optional.of(param0);
            return this;
        }

        public SetAttributesFunction.Modifier build() {
            return new SetAttributesFunction.Modifier(this.name, this.attribute, this.operation, this.amount, List.copyOf(this.slots), this.id);
        }
    }
}

package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetLoreFunction extends LootItemConditionalFunction {
    public static final Codec<SetLoreFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        Codec.BOOL.fieldOf("replace").orElse(false).forGetter(param0x -> param0x.replace),
                        ComponentSerialization.CODEC.listOf().fieldOf("lore").forGetter(param0x -> param0x.lore),
                        ExtraCodecs.strictOptionalField(LootContext.EntityTarget.CODEC, "entity").forGetter(param0x -> param0x.resolutionContext)
                    )
                )
                .apply(param0, SetLoreFunction::new)
    );
    private final boolean replace;
    private final List<Component> lore;
    private final Optional<LootContext.EntityTarget> resolutionContext;

    public SetLoreFunction(List<LootItemCondition> param0, boolean param1, List<Component> param2, Optional<LootContext.EntityTarget> param3) {
        super(param0);
        this.replace = param1;
        this.lore = List.copyOf(param2);
        this.resolutionContext = param3;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_LORE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext.<Set<LootContextParam<?>>>map(param0 -> Set.of(param0.getParam())).orElseGet(Set::of);
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        ListTag var0 = this.getLoreTag(param0, !this.lore.isEmpty());
        if (var0 != null) {
            if (this.replace) {
                var0.clear();
            }

            UnaryOperator<Component> var1 = SetNameFunction.createResolver(param1, this.resolutionContext.orElse(null));
            this.lore.stream().map(var1).map(Component.Serializer::toJson).map(StringTag::valueOf).forEach(var0::add);
        }

        return param0;
    }

    @Nullable
    private ListTag getLoreTag(ItemStack param0, boolean param1) {
        CompoundTag var0;
        if (param0.hasTag()) {
            var0 = param0.getTag();
        } else {
            if (!param1) {
                return null;
            }

            var0 = new CompoundTag();
            param0.setTag(var0);
        }

        CompoundTag var3;
        if (var0.contains("display", 10)) {
            var3 = var0.getCompound("display");
        } else {
            if (!param1) {
                return null;
            }

            var3 = new CompoundTag();
            var0.put("display", var3);
        }

        if (var3.contains("Lore", 9)) {
            return var3.getList("Lore", 8);
        } else if (param1) {
            ListTag var6 = new ListTag();
            var3.put("Lore", var6);
            return var6;
        } else {
            return null;
        }
    }

    public static SetLoreFunction.Builder setLore() {
        return new SetLoreFunction.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetLoreFunction.Builder> {
        private boolean replace;
        private Optional<LootContext.EntityTarget> resolutionContext = Optional.empty();
        private final ImmutableList.Builder<Component> lore = ImmutableList.builder();

        public SetLoreFunction.Builder setReplace(boolean param0) {
            this.replace = param0;
            return this;
        }

        public SetLoreFunction.Builder setResolutionContext(LootContext.EntityTarget param0) {
            this.resolutionContext = Optional.of(param0);
            return this;
        }

        public SetLoreFunction.Builder addLine(Component param0) {
            this.lore.add(param0);
            return this;
        }

        protected SetLoreFunction.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetLoreFunction(this.getConditions(), this.replace, this.lore.build(), this.resolutionContext);
        }
    }
}

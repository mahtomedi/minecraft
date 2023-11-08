package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger extends SimpleCriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
    @Override
    public Codec<ItemDurabilityTrigger.TriggerInstance> codec() {
        return ItemDurabilityTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, ItemStack param1, int param2) {
        this.trigger(param0, param2x -> param2x.matches(param1, param2));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, MinMaxBounds.Ints durability, MinMaxBounds.Ints delta
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ItemDurabilityTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(ItemDurabilityTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(ItemDurabilityTrigger.TriggerInstance::item),
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "durability", MinMaxBounds.Ints.ANY)
                            .forGetter(ItemDurabilityTrigger.TriggerInstance::durability),
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "delta", MinMaxBounds.Ints.ANY)
                            .forGetter(ItemDurabilityTrigger.TriggerInstance::delta)
                    )
                    .apply(param0, ItemDurabilityTrigger.TriggerInstance::new)
        );

        public static Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(Optional<ItemPredicate> param0, MinMaxBounds.Ints param1) {
            return changedDurability(Optional.empty(), param0, param1);
        }

        public static Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(
            Optional<ContextAwarePredicate> param0, Optional<ItemPredicate> param1, MinMaxBounds.Ints param2
        ) {
            return CriteriaTriggers.ITEM_DURABILITY_CHANGED
                .createCriterion(new ItemDurabilityTrigger.TriggerInstance(param0, param1, param2, MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ItemStack param0, int param1) {
            if (this.item.isPresent() && !this.item.get().matches(param0)) {
                return false;
            } else if (!this.durability.matches(param0.getMaxDamage() - param1)) {
                return false;
            } else {
                return this.delta.matches(param0.getDamageValue() - param1);
            }
        }
    }
}

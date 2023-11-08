package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
    @Override
    public Codec<InventoryChangeTrigger.TriggerInstance> codec() {
        return InventoryChangeTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, Inventory param1, ItemStack param2) {
        int var0 = 0;
        int var1 = 0;
        int var2 = 0;

        for(int var3 = 0; var3 < param1.getContainerSize(); ++var3) {
            ItemStack var4 = param1.getItem(var3);
            if (var4.isEmpty()) {
                ++var1;
            } else {
                ++var2;
                if (var4.getCount() >= var4.getMaxStackSize()) {
                    ++var0;
                }
            }
        }

        this.trigger(param0, param1, param2, var0, var1, var2);
    }

    private void trigger(ServerPlayer param0, Inventory param1, ItemStack param2, int param3, int param4, int param5) {
        this.trigger(param0, param5x -> param5x.matches(param1, param2, param3, param4, param5));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, InventoryChangeTrigger.TriggerInstance.Slots slots, List<ItemPredicate> items)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<InventoryChangeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(InventoryChangeTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(
                                InventoryChangeTrigger.TriggerInstance.Slots.CODEC, "slots", InventoryChangeTrigger.TriggerInstance.Slots.ANY
                            )
                            .forGetter(InventoryChangeTrigger.TriggerInstance::slots),
                        ExtraCodecs.strictOptionalField(ItemPredicate.CODEC.listOf(), "items", List.of())
                            .forGetter(InventoryChangeTrigger.TriggerInstance::items)
                    )
                    .apply(param0, InventoryChangeTrigger.TriggerInstance::new)
        );

        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate.Builder... param0) {
            return hasItems(Stream.of(param0).map(ItemPredicate.Builder::build).toArray(param0x -> new ItemPredicate[param0x]));
        }

        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate... param0) {
            return CriteriaTriggers.INVENTORY_CHANGED
                .createCriterion(
                    new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of(param0))
                );
        }

        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemLike... param0) {
            ItemPredicate[] var0 = new ItemPredicate[param0.length];

            for(int var1 = 0; var1 < param0.length; ++var1) {
                var0[var1] = new ItemPredicate(
                    Optional.empty(),
                    Optional.of(HolderSet.direct(param0[var1].asItem().builtInRegistryHolder())),
                    MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY,
                    List.of(),
                    List.of(),
                    Optional.empty(),
                    Optional.empty()
                );
            }

            return hasItems(var0);
        }

        public boolean matches(Inventory param0, ItemStack param1, int param2, int param3, int param4) {
            if (!this.slots.matches(param2, param3, param4)) {
                return false;
            } else if (this.items.isEmpty()) {
                return true;
            } else if (this.items.size() != 1) {
                List<ItemPredicate> var0 = new ObjectArrayList<>(this.items);
                int var1 = param0.getContainerSize();

                for(int var2 = 0; var2 < var1; ++var2) {
                    if (var0.isEmpty()) {
                        return true;
                    }

                    ItemStack var3 = param0.getItem(var2);
                    if (!var3.isEmpty()) {
                        var0.removeIf(param1x -> param1x.matches(var3));
                    }
                }

                return var0.isEmpty();
            } else {
                return !param1.isEmpty() && this.items.get(0).matches(param1);
            }
        }

        public static record Slots(MinMaxBounds.Ints occupied, MinMaxBounds.Ints full, MinMaxBounds.Ints empty) {
            public static final Codec<InventoryChangeTrigger.TriggerInstance.Slots> CODEC = RecordCodecBuilder.create(
                param0 -> param0.group(
                            ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "occupied", MinMaxBounds.Ints.ANY)
                                .forGetter(InventoryChangeTrigger.TriggerInstance.Slots::occupied),
                            ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "full", MinMaxBounds.Ints.ANY)
                                .forGetter(InventoryChangeTrigger.TriggerInstance.Slots::full),
                            ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "empty", MinMaxBounds.Ints.ANY)
                                .forGetter(InventoryChangeTrigger.TriggerInstance.Slots::empty)
                        )
                        .apply(param0, InventoryChangeTrigger.TriggerInstance.Slots::new)
            );
            public static final InventoryChangeTrigger.TriggerInstance.Slots ANY = new InventoryChangeTrigger.TriggerInstance.Slots(
                MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY
            );

            public boolean matches(int param0, int param1, int param2) {
                if (!this.full.matches(param0)) {
                    return false;
                } else if (!this.empty.matches(param1)) {
                    return false;
                } else {
                    return this.occupied.matches(param2);
                }
            }
        }
    }
}

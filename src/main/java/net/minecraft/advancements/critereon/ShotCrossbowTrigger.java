package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ShotCrossbowTrigger extends SimpleCriterionTrigger<ShotCrossbowTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("shot_crossbow");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ShotCrossbowTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ItemPredicate> var0 = ItemPredicate.fromJson(param0.get("item"));
        return new ShotCrossbowTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, ItemStack param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ItemPredicate> item;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<ItemPredicate> param1) {
            super(ShotCrossbowTrigger.ID, param0);
            this.item = param1;
        }

        public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(Optional<ItemPredicate> param0) {
            return new ShotCrossbowTrigger.TriggerInstance(Optional.empty(), param0);
        }

        public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(ItemLike param0) {
            return new ShotCrossbowTrigger.TriggerInstance(Optional.empty(), ItemPredicate.Builder.item().of(param0).build());
        }

        public boolean matches(ItemStack param0) {
            return this.item.isEmpty() || this.item.get().matches(param0);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.item.ifPresent(param1 -> var0.add("item", param1.serializeToJson()));
            return var0;
        }
    }
}

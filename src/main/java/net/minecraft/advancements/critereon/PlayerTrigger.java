package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
    final ResourceLocation id;

    public PlayerTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public PlayerTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        return new PlayerTrigger.TriggerInstance(this.id, param1);
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0, param0x -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ResourceLocation param0, EntityPredicate.Composite param1) {
            super(param0, param1);
        }

        public static PlayerTrigger.TriggerInstance located(LocationPredicate param0) {
            return new PlayerTrigger.TriggerInstance(
                CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.wrap(EntityPredicate.Builder.entity().located(param0).build())
            );
        }

        public static PlayerTrigger.TriggerInstance located(EntityPredicate param0) {
            return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.wrap(param0));
        }

        public static PlayerTrigger.TriggerInstance sleptInBed() {
            return new PlayerTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, EntityPredicate.Composite.ANY);
        }

        public static PlayerTrigger.TriggerInstance raidWon() {
            return new PlayerTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, EntityPredicate.Composite.ANY);
        }

        public static PlayerTrigger.TriggerInstance avoidVibration() {
            return new PlayerTrigger.TriggerInstance(CriteriaTriggers.AVOID_VIBRATION.id, EntityPredicate.Composite.ANY);
        }

        public static PlayerTrigger.TriggerInstance tick() {
            return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.id, EntityPredicate.Composite.ANY);
        }

        public static PlayerTrigger.TriggerInstance walkOnBlockWithEquipment(Block param0, Item param1) {
            return located(
                EntityPredicate.Builder.entity()
                    .equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(param1).build()).build())
                    .steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(param0).build()).build())
                    .build()
            );
        }
    }
}

package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
    public PlayerTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        return new PlayerTrigger.TriggerInstance(param1);
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0, param0x -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(Optional<ContextAwarePredicate> param0) {
            super(param0);
        }

        public static Criterion<PlayerTrigger.TriggerInstance> located(LocationPredicate.Builder param0) {
            return CriteriaTriggers.LOCATION
                .createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().located(param0)))));
        }

        public static Criterion<PlayerTrigger.TriggerInstance> located(EntityPredicate.Builder param0) {
            return CriteriaTriggers.LOCATION.createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(param0.build()))));
        }

        public static Criterion<PlayerTrigger.TriggerInstance> located(Optional<EntityPredicate> param0) {
            return CriteriaTriggers.LOCATION.createCriterion(new PlayerTrigger.TriggerInstance(EntityPredicate.wrap(param0)));
        }

        public static Criterion<PlayerTrigger.TriggerInstance> sleptInBed() {
            return CriteriaTriggers.SLEPT_IN_BED.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
        }

        public static Criterion<PlayerTrigger.TriggerInstance> raidWon() {
            return CriteriaTriggers.RAID_WIN.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
        }

        public static Criterion<PlayerTrigger.TriggerInstance> avoidVibration() {
            return CriteriaTriggers.AVOID_VIBRATION.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
        }

        public static Criterion<PlayerTrigger.TriggerInstance> tick() {
            return CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
        }

        public static Criterion<PlayerTrigger.TriggerInstance> walkOnBlockWithEquipment(Block param0, Item param1) {
            return located(
                EntityPredicate.Builder.entity()
                    .equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(param1)))
                    .steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(param0)))
            );
        }
    }
}

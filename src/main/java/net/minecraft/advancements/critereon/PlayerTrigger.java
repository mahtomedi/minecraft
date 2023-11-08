package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
    @Override
    public Codec<PlayerTrigger.TriggerInstance> codec() {
        return PlayerTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0, param0x -> true);
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<PlayerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(PlayerTrigger.TriggerInstance::player)
                    )
                    .apply(param0, PlayerTrigger.TriggerInstance::new)
        );

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

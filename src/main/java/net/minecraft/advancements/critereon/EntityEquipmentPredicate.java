package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;

public record EntityEquipmentPredicate(
    Optional<ItemPredicate> head,
    Optional<ItemPredicate> chest,
    Optional<ItemPredicate> legs,
    Optional<ItemPredicate> feet,
    Optional<ItemPredicate> mainhand,
    Optional<ItemPredicate> offhand
) {
    public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "head").forGetter(EntityEquipmentPredicate::head),
                    ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "chest").forGetter(EntityEquipmentPredicate::chest),
                    ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "legs").forGetter(EntityEquipmentPredicate::legs),
                    ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "feet").forGetter(EntityEquipmentPredicate::feet),
                    ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "mainhand").forGetter(EntityEquipmentPredicate::mainhand),
                    ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "offhand").forGetter(EntityEquipmentPredicate::offhand)
                )
                .apply(param0, EntityEquipmentPredicate::new)
    );
    public static final EntityEquipmentPredicate CAPTAIN = new EntityEquipmentPredicate(
        ItemPredicate.Builder.item().of(Items.WHITE_BANNER).hasNbt(Raid.getLeaderBannerInstance().getTag()).build(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );

    static Optional<EntityEquipmentPredicate> of(
        Optional<ItemPredicate> param0,
        Optional<ItemPredicate> param1,
        Optional<ItemPredicate> param2,
        Optional<ItemPredicate> param3,
        Optional<ItemPredicate> param4,
        Optional<ItemPredicate> param5
    ) {
        return param0.isEmpty() && param1.isEmpty() && param2.isEmpty() && param3.isEmpty() && param4.isEmpty() && param5.isEmpty()
            ? Optional.empty()
            : Optional.of(new EntityEquipmentPredicate(param0, param1, param2, param3, param4, param5));
    }

    public boolean matches(@Nullable Entity param0) {
        if (param0 instanceof LivingEntity var0) {
            if (this.head.isPresent() && !this.head.get().matches(var0.getItemBySlot(EquipmentSlot.HEAD))) {
                return false;
            } else if (this.chest.isPresent() && !this.chest.get().matches(var0.getItemBySlot(EquipmentSlot.CHEST))) {
                return false;
            } else if (this.legs.isPresent() && !this.legs.get().matches(var0.getItemBySlot(EquipmentSlot.LEGS))) {
                return false;
            } else if (this.feet.isPresent() && !this.feet.get().matches(var0.getItemBySlot(EquipmentSlot.FEET))) {
                return false;
            } else if (this.mainhand.isPresent() && !this.mainhand.get().matches(var0.getItemBySlot(EquipmentSlot.MAINHAND))) {
                return false;
            } else {
                return !this.offhand.isPresent() || this.offhand.get().matches(var0.getItemBySlot(EquipmentSlot.OFFHAND));
            }
        } else {
            return false;
        }
    }

    public static class Builder {
        private Optional<ItemPredicate> head = Optional.empty();
        private Optional<ItemPredicate> chest = Optional.empty();
        private Optional<ItemPredicate> legs = Optional.empty();
        private Optional<ItemPredicate> feet = Optional.empty();
        private Optional<ItemPredicate> mainhand = Optional.empty();
        private Optional<ItemPredicate> offhand = Optional.empty();

        public static EntityEquipmentPredicate.Builder equipment() {
            return new EntityEquipmentPredicate.Builder();
        }

        public EntityEquipmentPredicate.Builder head(ItemPredicate.Builder param0) {
            this.head = param0.build();
            return this;
        }

        public EntityEquipmentPredicate.Builder chest(ItemPredicate.Builder param0) {
            this.chest = param0.build();
            return this;
        }

        public EntityEquipmentPredicate.Builder legs(ItemPredicate.Builder param0) {
            this.legs = param0.build();
            return this;
        }

        public EntityEquipmentPredicate.Builder feet(ItemPredicate.Builder param0) {
            this.feet = param0.build();
            return this;
        }

        public EntityEquipmentPredicate.Builder mainhand(ItemPredicate.Builder param0) {
            this.mainhand = param0.build();
            return this;
        }

        public EntityEquipmentPredicate.Builder offhand(ItemPredicate.Builder param0) {
            this.offhand = param0.build();
            return this;
        }

        public Optional<EntityEquipmentPredicate> build() {
            return EntityEquipmentPredicate.of(this.head, this.chest, this.legs, this.feet, this.mainhand, this.offhand);
        }
    }
}

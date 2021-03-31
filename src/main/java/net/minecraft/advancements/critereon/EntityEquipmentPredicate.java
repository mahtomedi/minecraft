package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;

public class EntityEquipmentPredicate {
    public static final EntityEquipmentPredicate ANY = new EntityEquipmentPredicate(
        ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY
    );
    public static final EntityEquipmentPredicate CAPTAIN = new EntityEquipmentPredicate(
        ItemPredicate.Builder.item().of(Items.WHITE_BANNER).hasNbt(Raid.getLeaderBannerInstance().getTag()).build(),
        ItemPredicate.ANY,
        ItemPredicate.ANY,
        ItemPredicate.ANY,
        ItemPredicate.ANY,
        ItemPredicate.ANY
    );
    private final ItemPredicate head;
    private final ItemPredicate chest;
    private final ItemPredicate legs;
    private final ItemPredicate feet;
    private final ItemPredicate mainhand;
    private final ItemPredicate offhand;

    public EntityEquipmentPredicate(
        ItemPredicate param0, ItemPredicate param1, ItemPredicate param2, ItemPredicate param3, ItemPredicate param4, ItemPredicate param5
    ) {
        this.head = param0;
        this.chest = param1;
        this.legs = param2;
        this.feet = param3;
        this.mainhand = param4;
        this.offhand = param5;
    }

    public boolean matches(@Nullable Entity param0) {
        if (this == ANY) {
            return true;
        } else if (!(param0 instanceof LivingEntity)) {
            return false;
        } else {
            LivingEntity var0 = (LivingEntity)param0;
            if (!this.head.matches(var0.getItemBySlot(EquipmentSlot.HEAD))) {
                return false;
            } else if (!this.chest.matches(var0.getItemBySlot(EquipmentSlot.CHEST))) {
                return false;
            } else if (!this.legs.matches(var0.getItemBySlot(EquipmentSlot.LEGS))) {
                return false;
            } else if (!this.feet.matches(var0.getItemBySlot(EquipmentSlot.FEET))) {
                return false;
            } else if (!this.mainhand.matches(var0.getItemBySlot(EquipmentSlot.MAINHAND))) {
                return false;
            } else {
                return this.offhand.matches(var0.getItemBySlot(EquipmentSlot.OFFHAND));
            }
        }
    }

    public static EntityEquipmentPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "equipment");
            ItemPredicate var1 = ItemPredicate.fromJson(var0.get("head"));
            ItemPredicate var2 = ItemPredicate.fromJson(var0.get("chest"));
            ItemPredicate var3 = ItemPredicate.fromJson(var0.get("legs"));
            ItemPredicate var4 = ItemPredicate.fromJson(var0.get("feet"));
            ItemPredicate var5 = ItemPredicate.fromJson(var0.get("mainhand"));
            ItemPredicate var6 = ItemPredicate.fromJson(var0.get("offhand"));
            return new EntityEquipmentPredicate(var1, var2, var3, var4, var5, var6);
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            var0.add("head", this.head.serializeToJson());
            var0.add("chest", this.chest.serializeToJson());
            var0.add("legs", this.legs.serializeToJson());
            var0.add("feet", this.feet.serializeToJson());
            var0.add("mainhand", this.mainhand.serializeToJson());
            var0.add("offhand", this.offhand.serializeToJson());
            return var0;
        }
    }

    public static class Builder {
        private ItemPredicate head = ItemPredicate.ANY;
        private ItemPredicate chest = ItemPredicate.ANY;
        private ItemPredicate legs = ItemPredicate.ANY;
        private ItemPredicate feet = ItemPredicate.ANY;
        private ItemPredicate mainhand = ItemPredicate.ANY;
        private ItemPredicate offhand = ItemPredicate.ANY;

        public static EntityEquipmentPredicate.Builder equipment() {
            return new EntityEquipmentPredicate.Builder();
        }

        public EntityEquipmentPredicate.Builder head(ItemPredicate param0) {
            this.head = param0;
            return this;
        }

        public EntityEquipmentPredicate.Builder chest(ItemPredicate param0) {
            this.chest = param0;
            return this;
        }

        public EntityEquipmentPredicate.Builder legs(ItemPredicate param0) {
            this.legs = param0;
            return this;
        }

        public EntityEquipmentPredicate.Builder feet(ItemPredicate param0) {
            this.feet = param0;
            return this;
        }

        public EntityEquipmentPredicate.Builder mainhand(ItemPredicate param0) {
            this.mainhand = param0;
            return this;
        }

        public EntityEquipmentPredicate.Builder offhand(ItemPredicate param0) {
            this.offhand = param0;
            return this;
        }

        public EntityEquipmentPredicate build() {
            return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.mainhand, this.offhand);
        }
    }
}

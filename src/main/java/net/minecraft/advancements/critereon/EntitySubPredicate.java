package net.minecraft.advancements.critereon;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public interface EntitySubPredicate {
    EntitySubPredicate ANY = new EntitySubPredicate() {
        @Override
        public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
            return true;
        }

        @Override
        public JsonObject serializeCustomData() {
            return new JsonObject();
        }

        @Override
        public EntitySubPredicate.Type type() {
            return EntitySubPredicate.Types.ANY;
        }
    };

    static EntitySubPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "type_specific");
            String var1 = GsonHelper.getAsString(var0, "type", null);
            if (var1 == null) {
                return ANY;
            } else {
                EntitySubPredicate.Type var2 = EntitySubPredicate.Types.TYPES.get(var1);
                if (var2 == null) {
                    throw new JsonSyntaxException("Unknown sub-predicate type: " + var1);
                } else {
                    return var2.deserialize(var0);
                }
            }
        } else {
            return ANY;
        }
    }

    boolean matches(Entity var1, ServerLevel var2, @Nullable Vec3 var3);

    JsonObject serializeCustomData();

    default JsonElement serialize() {
        if (this.type() == EntitySubPredicate.Types.ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = this.serializeCustomData();
            String var1 = EntitySubPredicate.Types.TYPES.inverse().get(this.type());
            var0.addProperty("type", var1);
            return var0;
        }
    }

    EntitySubPredicate.Type type();

    static EntitySubPredicate variant(CatVariant param0) {
        return EntitySubPredicate.Types.CAT.createPredicate(param0);
    }

    static EntitySubPredicate variant(FrogVariant param0) {
        return EntitySubPredicate.Types.FROG.createPredicate(param0);
    }

    public interface Type {
        EntitySubPredicate deserialize(JsonObject var1);
    }

    public static final class Types {
        public static final EntitySubPredicate.Type ANY = param0 -> EntitySubPredicate.ANY;
        public static final EntitySubPredicate.Type LIGHTNING = LighthingBoltPredicate::fromJson;
        public static final EntitySubPredicate.Type FISHING_HOOK = FishingHookPredicate::fromJson;
        public static final EntitySubPredicate.Type PLAYER = PlayerPredicate::fromJson;
        public static final EntitySubPredicate.Type SLIME = SlimePredicate::fromJson;
        public static final EntityVariantPredicate<CatVariant> CAT = EntityVariantPredicate.create(
            BuiltInRegistries.CAT_VARIANT, param0 -> param0 instanceof Cat var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<FrogVariant> FROG = EntityVariantPredicate.create(
            BuiltInRegistries.FROG_VARIANT, param0 -> param0 instanceof Frog var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<Axolotl.Variant> AXOLOTL = EntityVariantPredicate.create(
            Axolotl.Variant.CODEC, param0 -> param0 instanceof Axolotl var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<Boat.Type> BOAT = EntityVariantPredicate.create(
            Boat.Type.CODEC, param0 -> param0 instanceof Boat var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<Fox.Type> FOX = EntityVariantPredicate.create(
            Fox.Type.CODEC, param0 -> param0 instanceof Fox var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<MushroomCow.MushroomType> MOOSHROOM = EntityVariantPredicate.create(
            MushroomCow.MushroomType.CODEC, param0 -> param0 instanceof MushroomCow var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<Holder<PaintingVariant>> PAINTING = EntityVariantPredicate.create(
            BuiltInRegistries.PAINTING_VARIANT.holderByNameCodec(),
            param0 -> param0 instanceof Painting var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<Rabbit.Variant> RABBIT = EntityVariantPredicate.create(
            Rabbit.Variant.CODEC, param0 -> param0 instanceof Rabbit var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<Variant> HORSE = EntityVariantPredicate.create(
            Variant.CODEC, param0 -> param0 instanceof Horse var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<Llama.Variant> LLAMA = EntityVariantPredicate.create(
            Llama.Variant.CODEC, param0 -> param0 instanceof Llama var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<VillagerType> VILLAGER = EntityVariantPredicate.create(
            BuiltInRegistries.VILLAGER_TYPE.byNameCodec(),
            param0 -> param0 instanceof VillagerDataHolder var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<Parrot.Variant> PARROT = EntityVariantPredicate.create(
            Parrot.Variant.CODEC, param0 -> param0 instanceof Parrot var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<TropicalFish.Pattern> TROPICAL_FISH = EntityVariantPredicate.create(
            TropicalFish.Pattern.CODEC, param0 -> param0 instanceof TropicalFish var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final BiMap<String, EntitySubPredicate.Type> TYPES = ImmutableBiMap.<String, EntitySubPredicate.Type>builder()
            .put("any", ANY)
            .put("lightning", LIGHTNING)
            .put("fishing_hook", FISHING_HOOK)
            .put("player", PLAYER)
            .put("slime", SLIME)
            .put("cat", CAT.type())
            .put("frog", FROG.type())
            .put("axolotl", AXOLOTL.type())
            .put("boat", BOAT.type())
            .put("fox", FOX.type())
            .put("mooshroom", MOOSHROOM.type())
            .put("painting", PAINTING.type())
            .put("rabbit", RABBIT.type())
            .put("horse", HORSE.type())
            .put("llama", LLAMA.type())
            .put("villager", VILLAGER.type())
            .put("parrot", PARROT.type())
            .put("tropical_fish", TROPICAL_FISH.type())
            .buildOrThrow();
    }
}

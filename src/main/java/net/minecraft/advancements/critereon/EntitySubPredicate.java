package net.minecraft.advancements.critereon;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.frog.Frog;
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
            Registry.CAT_VARIANT, param0 -> param0 instanceof Cat var0 ? Optional.of(var0.getCatVariant()) : Optional.empty()
        );
        public static final EntityVariantPredicate<FrogVariant> FROG = EntityVariantPredicate.create(
            Registry.FROG_VARIANT, param0 -> param0 instanceof Frog var0 ? Optional.of(var0.getVariant()) : Optional.empty()
        );
        public static final BiMap<String, EntitySubPredicate.Type> TYPES = ImmutableBiMap.of(
            "any", ANY, "lightning", LIGHTNING, "fishing_hook", FISHING_HOOK, "player", PLAYER, "slime", SLIME, "cat", CAT.type(), "frog", FROG.type()
        );
    }
}

package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public class LighthingBoltPredicate {
    public static final LighthingBoltPredicate ANY = new LighthingBoltPredicate(MinMaxBounds.Ints.ANY, EntityPredicate.ANY);
    private static final String BLOCKS_SET_ON_FIRE_KEY = "blocks_set_on_fire";
    private static final String ENTITY_STRUCK_KEY = "entity_struck";
    private final MinMaxBounds.Ints blocksSetOnFire;
    private final EntityPredicate entityStruck;

    private LighthingBoltPredicate(MinMaxBounds.Ints param0, EntityPredicate param1) {
        this.blocksSetOnFire = param0;
        this.entityStruck = param1;
    }

    public static LighthingBoltPredicate blockSetOnFire(MinMaxBounds.Ints param0) {
        return new LighthingBoltPredicate(param0, EntityPredicate.ANY);
    }

    public static LighthingBoltPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "lightning");
            return new LighthingBoltPredicate(MinMaxBounds.Ints.fromJson(var0.get("blocks_set_on_fire")), EntityPredicate.fromJson(var0.get("entity_struck")));
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            var0.add("blocks_set_on_fire", this.blocksSetOnFire.serializeToJson());
            var0.add("entity_struck", this.entityStruck.serializeToJson());
            return var0;
        }
    }

    public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
        if (this == ANY) {
            return true;
        } else if (!(param0 instanceof LightningBolt)) {
            return false;
        } else {
            LightningBolt var0 = (LightningBolt)param0;
            return this.blocksSetOnFire.matches(var0.getBlocksSetOnFire())
                && (this.entityStruck == EntityPredicate.ANY || var0.getHitEntities().anyMatch(param2x -> this.entityStruck.matches(param1, param2, param2x)));
        }
    }
}

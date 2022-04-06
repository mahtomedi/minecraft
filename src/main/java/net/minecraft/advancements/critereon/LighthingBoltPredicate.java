package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public class LighthingBoltPredicate implements EntitySubPredicate {
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

    public static LighthingBoltPredicate fromJson(JsonObject param0) {
        return new LighthingBoltPredicate(MinMaxBounds.Ints.fromJson(param0.get("blocks_set_on_fire")), EntityPredicate.fromJson(param0.get("entity_struck")));
    }

    @Override
    public JsonObject serializeCustomData() {
        JsonObject var0 = new JsonObject();
        var0.add("blocks_set_on_fire", this.blocksSetOnFire.serializeToJson());
        var0.add("entity_struck", this.entityStruck.serializeToJson());
        return var0;
    }

    @Override
    public EntitySubPredicate.Type type() {
        return EntitySubPredicate.Types.LIGHTNING;
    }

    @Override
    public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
        if (!(param0 instanceof LightningBolt)) {
            return false;
        } else {
            LightningBolt var0 = (LightningBolt)param0;
            return this.blocksSetOnFire.matches(var0.getBlocksSetOnFire())
                && (this.entityStruck == EntityPredicate.ANY || var0.getHitEntities().anyMatch(param2x -> this.entityStruck.matches(param1, param2, param2x)));
        }
    }
}

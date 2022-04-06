package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

public class SlimePredicate implements EntitySubPredicate {
    private final MinMaxBounds.Ints size;

    private SlimePredicate(MinMaxBounds.Ints param0) {
        this.size = param0;
    }

    public static SlimePredicate sized(MinMaxBounds.Ints param0) {
        return new SlimePredicate(param0);
    }

    public static SlimePredicate fromJson(JsonObject param0) {
        MinMaxBounds.Ints var0 = MinMaxBounds.Ints.fromJson(param0.get("size"));
        return new SlimePredicate(var0);
    }

    @Override
    public JsonObject serializeCustomData() {
        JsonObject var0 = new JsonObject();
        var0.add("size", this.size.serializeToJson());
        return var0;
    }

    @Override
    public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
        return param0 instanceof Slime var0 ? this.size.matches(var0.getSize()) : false;
    }

    @Override
    public EntitySubPredicate.Type type() {
        return EntitySubPredicate.Types.SLIME;
    }
}

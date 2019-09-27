package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;

public class LightPredicate {
    public static final LightPredicate ANY = new LightPredicate(MinMaxBounds.Ints.ANY);
    private final MinMaxBounds.Ints composite;

    private LightPredicate(MinMaxBounds.Ints param0) {
        this.composite = param0;
    }

    public boolean matches(ServerLevel param0, BlockPos param1) {
        if (this == ANY) {
            return true;
        } else if (!param0.isLoaded(param1)) {
            return false;
        } else {
            return this.composite.matches(param0.getMaxLocalRawBrightness(param1));
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            var0.add("light", this.composite.serializeToJson());
            return var0;
        }
    }

    public static LightPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "light");
            MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(var0.get("light"));
            return new LightPredicate(var1);
        } else {
            return ANY;
        }
    }
}

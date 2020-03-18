package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.fishing.FishingHook;

public class FishingHookPredicate {
    public static final FishingHookPredicate ANY = new FishingHookPredicate(false);
    private boolean inOpenWater;

    private FishingHookPredicate(boolean param0) {
        this.inOpenWater = param0;
    }

    public static FishingHookPredicate inOpenWater(boolean param0) {
        return new FishingHookPredicate(param0);
    }

    public static FishingHookPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "fishing_hook");
            JsonElement var1 = var0.get("in_open_water");
            return var1 != null ? new FishingHookPredicate(GsonHelper.convertToBoolean(var1, "in_open_water")) : ANY;
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            var0.add("in_open_water", new JsonPrimitive(this.inOpenWater));
            return var0;
        }
    }

    public boolean matches(Entity param0) {
        if (this == ANY) {
            return true;
        } else if (!(param0 instanceof FishingHook)) {
            return false;
        } else {
            FishingHook var0 = (FishingHook)param0;
            return this.inOpenWater == var0.isOpenWaterFishing();
        }
    }
}

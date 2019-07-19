package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class DistancePredicate {
    public static final DistancePredicate ANY = new DistancePredicate(
        MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY
    );
    private final MinMaxBounds.Floats x;
    private final MinMaxBounds.Floats y;
    private final MinMaxBounds.Floats z;
    private final MinMaxBounds.Floats horizontal;
    private final MinMaxBounds.Floats absolute;

    public DistancePredicate(
        MinMaxBounds.Floats param0, MinMaxBounds.Floats param1, MinMaxBounds.Floats param2, MinMaxBounds.Floats param3, MinMaxBounds.Floats param4
    ) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.horizontal = param3;
        this.absolute = param4;
    }

    public static DistancePredicate horizontal(MinMaxBounds.Floats param0) {
        return new DistancePredicate(MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, param0, MinMaxBounds.Floats.ANY);
    }

    public static DistancePredicate vertical(MinMaxBounds.Floats param0) {
        return new DistancePredicate(MinMaxBounds.Floats.ANY, param0, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY);
    }

    public boolean matches(double param0, double param1, double param2, double param3, double param4, double param5) {
        float var0 = (float)(param0 - param3);
        float var1 = (float)(param1 - param4);
        float var2 = (float)(param2 - param5);
        if (!this.x.matches(Mth.abs(var0)) || !this.y.matches(Mth.abs(var1)) || !this.z.matches(Mth.abs(var2))) {
            return false;
        } else if (!this.horizontal.matchesSqr((double)(var0 * var0 + var2 * var2))) {
            return false;
        } else {
            return this.absolute.matchesSqr((double)(var0 * var0 + var1 * var1 + var2 * var2));
        }
    }

    public static DistancePredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "distance");
            MinMaxBounds.Floats var1 = MinMaxBounds.Floats.fromJson(var0.get("x"));
            MinMaxBounds.Floats var2 = MinMaxBounds.Floats.fromJson(var0.get("y"));
            MinMaxBounds.Floats var3 = MinMaxBounds.Floats.fromJson(var0.get("z"));
            MinMaxBounds.Floats var4 = MinMaxBounds.Floats.fromJson(var0.get("horizontal"));
            MinMaxBounds.Floats var5 = MinMaxBounds.Floats.fromJson(var0.get("absolute"));
            return new DistancePredicate(var1, var2, var3, var4, var5);
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            var0.add("x", this.x.serializeToJson());
            var0.add("y", this.y.serializeToJson());
            var0.add("z", this.z.serializeToJson());
            var0.add("horizontal", this.horizontal.serializeToJson());
            var0.add("absolute", this.absolute.serializeToJson());
            return var0;
        }
    }
}

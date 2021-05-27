package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class DistancePredicate {
    public static final DistancePredicate ANY = new DistancePredicate(
        MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY
    );
    private final MinMaxBounds.Doubles x;
    private final MinMaxBounds.Doubles y;
    private final MinMaxBounds.Doubles z;
    private final MinMaxBounds.Doubles horizontal;
    private final MinMaxBounds.Doubles absolute;

    public DistancePredicate(
        MinMaxBounds.Doubles param0, MinMaxBounds.Doubles param1, MinMaxBounds.Doubles param2, MinMaxBounds.Doubles param3, MinMaxBounds.Doubles param4
    ) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.horizontal = param3;
        this.absolute = param4;
    }

    public static DistancePredicate horizontal(MinMaxBounds.Doubles param0) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, param0, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate vertical(MinMaxBounds.Doubles param0) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, param0, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate absolute(MinMaxBounds.Doubles param0) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, param0);
    }

    public boolean matches(double param0, double param1, double param2, double param3, double param4, double param5) {
        float var0 = (float)(param0 - param3);
        float var1 = (float)(param1 - param4);
        float var2 = (float)(param2 - param5);
        if (!this.x.matches((double)Mth.abs(var0)) || !this.y.matches((double)Mth.abs(var1)) || !this.z.matches((double)Mth.abs(var2))) {
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
            MinMaxBounds.Doubles var1 = MinMaxBounds.Doubles.fromJson(var0.get("x"));
            MinMaxBounds.Doubles var2 = MinMaxBounds.Doubles.fromJson(var0.get("y"));
            MinMaxBounds.Doubles var3 = MinMaxBounds.Doubles.fromJson(var0.get("z"));
            MinMaxBounds.Doubles var4 = MinMaxBounds.Doubles.fromJson(var0.get("horizontal"));
            MinMaxBounds.Doubles var5 = MinMaxBounds.Doubles.fromJson(var0.get("absolute"));
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
